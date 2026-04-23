package utmn.checkmates.server.network.tcp;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.network.packet.PacketHandler;
import utmn.checkmates.server.network.packet.PacketType;
import utmn.checkmates.server.network.packet.input.CreateSessionPacket;
import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.input.ServerConnectionPacket;
import utmn.checkmates.server.network.packet.input.SessionPacket;
import utmn.checkmates.server.network.packet.output.ClientConnectionPacket;
import utmn.checkmates.server.network.packet.output.ExceptionPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.FormatUtils;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Основной класс, ответственный за приём, делегацию и отправку данных через TCP-сокеты.
 *
 * @version 1
 * @since v0.9
 * @author Виталий Трифонов
 */
public class NetworkServer{
    private final int port;
    private volatile boolean stopFlag = false;

    private final SessionConnectionsManager scm = new SessionConnectionsManager(this);

    private final ExecutorService pool;

    public NetworkServer(int port) {
        pool = Application.getPool();
        this.port = port;
    }

    /**
     * Открывающий метод
     *
     * <p>
     * Данный метод отвечает за запуск серверного сокета и основного цикла программы, обрабатывающего входящие соединения.
     * Сокет каждого из соединений проверяется на существование в пулах. Если не существует - то он ожидает приём
     * сообщения подключения S0001, иначе - выкидывает ошибку. В дальнейшем, вся входящая информация о сокетах будет получаться
     * и обрабатываться в SessionConnectionsManagers в отдельных виртуальных потоках.
     * </p>
     *
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logger.log(this.getClass().getSimpleName(), "run", "Сервер запущен на порту " + port);

            while (!stopFlag) {
                try {
                    Socket clientSocket = serverSocket.accept(); //блокирующая операция

                    //отправляем в асинхронку, чтобы принимать остальные подключения
                    pool.submit(() -> {
                        try {
                            handleAcceptedSocket(clientSocket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    if (!stopFlag) Logger.err("Ошибка accept: " + e.getMessage());
                }
            }
        } catch (BindException e) {
            //
            Logger.log(this.getClass().getSimpleName(), "run",
                    "Не удалось создать сокет. Причина: %s ".formatted(e.toString()));
            //
            Logger.err("Порт " + port + " уже занят");
        } catch (IOException e) {
            //
            Logger.log(this.getClass().getSimpleName(), "run",
                    "Возникла ошибка при обработке запроса. Причина: %s ".formatted(e.toString()));
            //
            Logger.err("Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleAcceptedSocket(Socket clientSocket) throws IOException {
        Logger.log(this.getClass().getSimpleName(), "run", "Запрос на открытие соединения с %s:%d"
                .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort()));

        if(!scm.getConnections().containsKey(clientSocket)){
            InputStream rawIn = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, StandardCharsets.UTF_8));
            BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

            NetworkTcp.InputMessage message = NetworkTcp.readNext(rawIn, in);
            if(message == null) return;
            PacketHandler.PacketSet set = PacketHandler.handle(clientSocket, message.type, message.json);

            InputPacket inputPacket = set.getInput();
            List<OutputPacket> outputPackets = set.getOutput();

            if(inputPacket instanceof SessionPacket){
                if(inputPacket instanceof ServerConnectionPacket connectionPacket){
                    //
                    Logger.log(this.getClass().getSimpleName(), "run",
                            "Получен запрос на подключение к серверу от %s:%d: JSON: %s "
                                    .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort(), inputPacket.toJson()));
                    //

                    SessionConnection connection = scm.openSessionConnection(connectionPacket, clientSocket, rawIn, in, out);
                    int id = connection.getSession().getId(connection);

                    for(OutputPacket packet : outputPackets){
                        if(packet instanceof ClientConnectionPacket clientConnection){
                            clientConnection.setClientId(id);
                            clientConnection.setColor((byte) connection.getPlayer().getColor());
                            NetworkTcp.sendPacket(out, clientConnection);
                            continue;
                        }
                        NetworkTcp.sendPacket(out,packet);
                    }
                } else{
                    //
                    Logger.log(this.getClass().getSimpleName(), "run",
                            "Совершена попытка внесессионного взаимодействия через сессионный запрос от %s:%d: Тип сообщения: %s; JSON: %s "
                                    .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort(),
                                            PacketType.getByClass(inputPacket.getClass()), inputPacket.toJson()));
                    //

                    ExceptionPacket packet = new ExceptionPacket(
                            List.of(clientSocket), 6,
                            "Отсутствует подключение к сессии! Для подключения используйте запрос SERVER_CONNECTION(S0001).");
                    NetworkTcp.sendPacket(out, packet);
                    out.close();

                    //
                    Logger.log(this.getClass().getSimpleName(), "run",
                            "Отправлен ответ %s:%d: Тип сообщения: %s; JSON: %s "
                                    .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort(),
                                            PacketType.getByClass(packet.getClass()), packet.toJson()));
                    //
                }
            }else{
                //
                Logger.log(this.getClass().getSimpleName(), "run",
                        "Обработка внесессионного запроса от %s:%d: Тип сообщения: %s; JSON: %s "
                        .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort(),
                                PacketType.getByClass(inputPacket.getClass()), inputPacket.toJson()));
                //
                for(OutputPacket packet : outputPackets){
                    NetworkTcp.sendPacket(out, packet);
                }
                out.close();
                //
                Logger.log(this.getClass().getSimpleName(), "run",
                        "Отправлен ответ на внесессионный запрос от %s:%d: Сообщения: %s "
                                .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort(),
                                        FormatUtils.listOutputs(outputPackets)));
                //
            }
        }else{
            Logger.log(this.getClass().getSimpleName(), "run", "Не удалось открыть соединение с %s:%d: Сокет уже используется."
                    .formatted(clientSocket.getInetAddress().toString(), clientSocket.getPort()));
        }
    }

    public void stop() {
        stopFlag = true;
        scm.stopConnections();
        Logger.log(this.getClass().getSimpleName(), "stop", "Сервер остановлен");
    }

    public int getPort() {
        return port;
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public SessionConnectionsManager getConnectionsManager() {
        return scm;
    }
}
