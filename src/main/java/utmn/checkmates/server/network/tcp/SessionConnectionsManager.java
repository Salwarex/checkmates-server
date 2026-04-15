package utmn.checkmates.server.network.tcp;

import utmn.checkmates.server.game.session.Session;
import utmn.checkmates.server.network.packet.PacketHandler;
import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.input.ServerConnectionPacket;
import utmn.checkmates.server.network.packet.input.SessionPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class SessionConnectionsManager {
    private final NetworkServer networkServer;

    private int current = 0;
    private final ConcurrentMap<Integer, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Socket, SessionConnection> connections = new ConcurrentHashMap<>();

    private final ExecutorService clientPool = Executors.newVirtualThreadPerTaskExecutor();

    private final ConnectionChecker connectionChecker;

    public SessionConnectionsManager(NetworkServer networkServer) {
        this.networkServer = networkServer;
        this.connectionChecker = new ConnectionChecker(networkServer);
        clientPool.submit(connectionChecker);
    }

    public boolean ping(Socket clientSocket){
        if(!connections.containsKey(clientSocket)) return false;
        connectionChecker.update(connections.get(clientSocket));
        return true;
    }

    public SessionConnection openSessionConnection(ServerConnectionPacket connectionPacket, Socket clientSocket, InputStream rawIn, BufferedReader in, BufferedOutputStream out)
            throws IOException {
        Session session = sessions.get(connectionPacket.getSessionId());//обработать возможные ошибки

        SessionConnection connection = new SessionConnection();
        connection.setSession(session);
        connection.setClientSocket(clientSocket);
        connection.setRawIn(rawIn);
        connection.setIn(in);
        connection.setOut(out);
        connection.setActive(true);
        connection.setPlayerName(connectionPacket.getPlayerName());

        session.add(connection);
        connections.put(clientSocket, connection);

        connectionChecker.update(connection);

        clientPool.submit(() -> handleConnection(connection));
        return connection;
    }

    public void closeSessionConnection(Socket socket){
        SessionConnection connection = connections.get(socket);
        Logger.out("Игрок %s (%s:%d) отключается..."
                .formatted(connection.getPlayerName(), connection.getAddress(), connection.getClientSocket().getPort()));

        Session session = connection.getSession();
        session.remove(connection);
        connections.remove(socket, connection);
        connection.close();
    }

    public Session createSession(){
        Session session = new Session(current);
        sessions.put(current, session);
        current++;
        return session;
    }

    public ConcurrentMap<Integer, Session> getSessions() {
        return sessions;
    }

    public ConcurrentMap<Socket, SessionConnection> getConnections() {
        return connections;
    }

    public void stopConnections(){
        for (SessionConnection session : connections.values()) {
            session.close();
        }
        connections.clear();

        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void broadcast(Session session, OutputPacket packet, InetAddress exclude){
        session.broadcast(packet, exclude);
    }

    private void broadcast(OutputPacket packet){
        try{
            for(SessionConnection connection : connections.values()){
                NetworkTcp.sendPacket(connection, packet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NetworkServer getNetworkServer() {
        return networkServer;
    }

    //работает в отдельном потоке для каждого клиента
    private void handleConnection(SessionConnection connection) {
        InetAddress address = connection.getAddress();
        Logger.log(this.getClass().getSimpleName(), "session", "Начата сессия: " + connection.key());

        try {
            //пока сессия активна, читает входящие сообщения
            while (connection.isActive() && !networkServer.isStopFlag()) {
                NetworkTcp.InputMessage msg = NetworkTcp.readNext(connection);
                if (msg == null) {
                    //клиент отключился или ошибка чтения
                    break;
                }

                Logger.log(this.getClass().getSimpleName(), "handleSession",
                        "Получено: type=%d, json=%s от %s".formatted(msg.type, msg.json, address));

                PacketHandler.PacketSet set = PacketHandler.handle(
                        connection.getClientSocket(),
                        msg.type,
                        msg.json
                );

                //получение ответа
                List<OutputPacket> outputPackets = set.getOutput();
                InputPacket inputPacket = set.getInput();

                //Логика отправки ответа
                for(OutputPacket outputPacket: outputPackets){
                    List<InetAddress> destAddresses = outputPacket.getDestinationAddresses();

                    if (destAddresses == null || destAddresses.isEmpty()) {
                        if (outputPacket instanceof SessionPacket sessionPacket) {
                            broadcast(sessions.get(sessionPacket.getSessionId()), outputPacket, null); // в рамках сессии
                        } else {
                            broadcast(outputPacket); // всем
                        }
                    } else if (destAddresses.size() == 1 && destAddresses.getFirst().equals(address)) {
                        connection.sendPacket(outputPacket); // ответ отправителю
                    } else { //по конкретному адресу
                        for (InetAddress dest : destAddresses) {
                            for (SessionConnection session : connections.values()) {
                                if (session.getAddress().equals(dest)) {
                                    session.sendPacket(outputPacket);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(this.getClass().getSimpleName(), "session",
                    "Ошибка в сессии " + connection.key() + ": " + e.getMessage());
        } finally {
            closeSessionConnection(connection.getClientSocket());
        }
    }
}
