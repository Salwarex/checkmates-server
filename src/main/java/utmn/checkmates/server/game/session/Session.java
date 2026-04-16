package utmn.checkmates.server.game.session;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.network.tcp.SessionConnection;
import utmn.checkmates.server.utility.FormatUtils;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Session implements Closeable {
    private final int sessionId;
    private final ConcurrentMap<String, SessionConnection> connections = new ConcurrentHashMap<>();

    //позор? :О
    private final ConcurrentMap<Integer, String> idKeys = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> keysId = new ConcurrentHashMap<>();
    private int current = 0;

    private GameState gameState;

    public Session(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public ConcurrentMap<String, SessionConnection> getConnections() {
        return connections;
    }

    public void add(SessionConnection connection){
        if(connections.containsKey(connection.key())) return;
        connections.put(connection.key(), connection);

        int id = current;
        current++;
        idKeys.put(id, connection.key());
        keysId.put(connection.key(), id);

        //
        Logger.log(this.getClass().getSimpleName(), "add",
                "Подключение %s:%d (%s) добавлено к сессии #%d"
                        .formatted(connection.getAddress(), connection.getClientSocket().getPort(), connection.getPlayerName(),
                                sessionId));
        //
    }

    public SessionConnection get(String key){
        if(!connections.containsKey(key)){
            return connections.get(key);
        }
        return null;
    }

    public SessionConnection getById(int id){
        String key = idKeys.get(id);
        return connections.get(key);
    }

    public int getId(SessionConnection connection){
        String key = connection.key();

        return keysId.getOrDefault(key, -1);
    }

    public int getId(String key){
        //
        Logger.log(this.getClass().getSimpleName(), "getId",
                "Получение id. Указанный ключ: %s, Доступные ключи: [%s]"
                        .formatted(key, FormatUtils.listString(keysId.keySet().stream().toList())));
        //
        return keysId.get(key);
    }

    public int nextId(){
        return current;
    }

    public SessionConnection get(InetAddress address, String playerName){
        return get(key(address, playerName));
    }

    public String key(InetAddress address, String playerName){
        return playerName + "@" + address.toString();
    }

    public void remove(SessionConnection session) {
        String key = session.key();
        connections.remove(key, session);
        int id = keysId.get(key);
        keysId.remove(key);
        idKeys.remove(id);
    }


    public boolean sendToPlayer(String playerName, InetAddress address, OutputPacket packet) {
        SessionConnection session = connections.get(address.toString() + "@" + playerName);
        if (session == null || !session.isActive()) return false;
        try {
            session.sendPacket(packet);
            return true;
        } catch (IOException e) {
            remove(session);
            return false;
        }
    }

    public void broadcast(OutputPacket packet, InetAddress excludeAddress) {
        for (SessionConnection session : connections.values()) {
            if (session.getAddress().equals(excludeAddress)) continue;
            if (!session.isActive()) {
                remove(session);
                continue;
            }
            try {
                session.sendPacket(packet);
            } catch (IOException e) {
                remove(session);
            }
        }
    }

    public SessionDto getDto(){
        return new SessionDto(sessionId, "Room #%d".formatted(sessionId), connections.size() < 2);
    }

    @Override
    public void close() throws IOException {
        for(SessionConnection connection : connections.values()){
            Application.getServer().getConnectionsManager().closeSessionConnection(connection.getClientSocket());
            connections.clear();
            idKeys.clear();
            keysId.clear();
        }
    }
}
