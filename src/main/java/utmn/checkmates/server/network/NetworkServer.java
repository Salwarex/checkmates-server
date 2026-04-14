package utmn.checkmates.server.network;

import utmn.checkmates.server.network.packet.PacketHandler;
import utmn.checkmates.server.network.packet.PacketType;
import utmn.checkmates.server.network.packet.input.SessionPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.*;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

public class NetworkServer{
    private final int port;
    private volatile boolean stopFlag = false;

    private final ConcurrentMap<String, PlayerConnection> sessions = new ConcurrentHashMap<>();

    private final ExecutorService clientPool = Executors.newVirtualThreadPerTaskExecutor();

    public NetworkServer(int port) {
        this.port = port;
    }

    public boolean sendToPlayer(String playerName, InetAddress address, OutputPacket packet) {
        PlayerConnection session = sessions.get(address.toString() + "@" + playerName);
        if (session == null || !session.isActive()) return false;
        try {
            session.sendPacket(packet);
            return true;
        } catch (IOException e) {
            removeSession(session);
            return false;
        }
    }

    public void broadcast(OutputPacket packet, InetAddress excludeAddress) {
        for (PlayerConnection session : sessions.values()) {
            if (excludeAddress != null && session.getAddress().equals(excludeAddress)) continue;
            if (!session.isActive()) {
                removeSession(session);
                continue;
            }
            try {
                session.sendPacket(packet);
            } catch (IOException e) {
                removeSession(session);
            }
        }
    }

    private void removeSession(PlayerConnection session) {
        sessions.remove(session.key(), session);
        session.close();
    }


    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logger.log(this.getClass().getSimpleName(), "run", "Сервер запущен на порту " + port);

            while (!stopFlag) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    //todo: нужно сделать обработку запроса S0001, стартующую подключение к серверу
                    String playerName = "player_" + clientSocket.getInetAddress().getHostAddress();

                    PlayerConnection connection = new PlayerConnection(
                            clientSocket.getInetAddress(),
                            playerName,
                            clientSocket
                    );

                    sessions.put(connection.key(), connection);
                    clientPool.submit(() -> handleSession(connection));

                } catch (IOException e) {
                    if (!stopFlag) Logger.err("Ошибка accept: " + e.getMessage());
                }
            }
        } catch (BindException e) {
            Logger.err("Порт " + port + " уже занят");
        } catch (IOException e) {
            Logger.err("Ошибка сервера: " + e.getMessage());
        }
    }

    //работает в отдельном потоке для каждого клиента
    private void handleSession(PlayerConnection connection) {
        InetAddress address = connection.getAddress();
        Logger.log(this.getClass().getSimpleName(), "session", "Начата сессия: " + connection.key());

        try {
            //пока сессия активна, читает входящие сообщения
            while (connection.isActive() && !stopFlag) {
                InputMessage msg = connection.readNext();
                if (msg == null) {
                    //клиент отключился или ошибка чтения
                    break;
                }

                Logger.log(this.getClass().getSimpleName(), "handleSession",
                        "Получено: type=%d, json=%s от %s".formatted(msg.type, msg.json, address));

                //получение ответа
                OutputPacket outputPacket = PacketHandler.handle(
                        address,
                        InetAddress.getByName("127.0.0.1"),
                        msg.type,
                        msg.json
                );

                //Логика отправки ответа
                List<InetAddress> destAddresses = outputPacket.getDestinationAddresses();

                if (destAddresses == null || destAddresses.isEmpty()) {
                    if (outputPacket instanceof SessionPacket) {
                        broadcast(outputPacket, address); // в рамках сессии
                    } else {
                        broadcast(outputPacket, null); // всем //todo: поменять
                    }
                } else if (destAddresses.size() == 1 && destAddresses.getFirst().equals(address)) {
                    connection.sendPacket(outputPacket); // ответ отправителю
                } else { //по конкретному адресу
                    for (InetAddress dest : destAddresses) {
                        for (PlayerConnection session : sessions.values()) {
                            if (session.getAddress().equals(dest)) {
                                session.sendPacket(outputPacket);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(this.getClass().getSimpleName(), "session",
                    "Ошибка в сессии " + connection.key() + ": " + e.getMessage());
        } finally {
            removeSession(connection);
        }
    }

    public void stop() {
        stopFlag = true;
        for (PlayerConnection session : sessions.values()) {
            session.close();
        }
        sessions.clear();

        // Останавливаем пул
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        Logger.log(this.getClass().getSimpleName(), "stop", "Сервер остановлен");
    }

    public static class PlayerConnection implements AutoCloseable{
        private final InetAddress address;
        private final String playerName;
        private Socket clientSocket;
        private InputStream rawIn;
        private BufferedReader in;
        private BufferedOutputStream out;
        private volatile boolean active = true;

        public PlayerConnection(InetAddress address, String playerName) {
            this.address = address;
            this.playerName = playerName;
        }

        public PlayerConnection(InetAddress address, String playerName, Socket clientSocket) throws IOException {
            this.address = address;
            this.playerName = playerName;
            setClientSocket(clientSocket);
        }

        public String key(){
            return playerName + "@" + address.toString();
        }

        public InetAddress getAddress() {
            return address;
        }

        public String getPlayerName() {
            return playerName;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        public InputStream getRawIn() {
            return rawIn;
        }

        public BufferedOutputStream getOut() {
            return out;
        }

        public void setClientSocket(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.rawIn = clientSocket.getInputStream();
            this.in = new BufferedReader(new InputStreamReader(rawIn, StandardCharsets.UTF_8));
            this.out = new BufferedOutputStream(clientSocket.getOutputStream());
        }

        public InputMessage readNext() throws IOException {
            if (!active || in == null) return null;

            int typeInt = rawIn.read();
            if (typeInt == -1) return null;

            String json = in.readLine();
            if (json == null) return null;

            return new InputMessage((byte) typeInt, json);
        }

        public boolean isActive() {
            return active && clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
        }

        public void sendPacket(OutputPacket packet) throws IOException {
            if (!active) throw new IOException("Сессия неактивна");
            synchronized (out) {
                byte typeOut = PacketType.getByClass(packet.getClass()).getValue();
                String jsonOut = packet.toJson();
                out.write(typeOut);
                out.write(jsonOut.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.flush();
            }
        }

        @Override
        public void close() {
            active = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                Logger.err("Ошибка закрытия сессии " + key() + ": " + e.getMessage());
            }
            Logger.log("PlayerConnection", "close", "Сессия закрыта: " + key());
        }
    }

    public static class InputMessage {
        public final byte type;
        public final String json;
        public InputMessage(byte type, String json) { this.type = type; this.json = json; }
    }
}
