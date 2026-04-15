package utmn.checkmates.server.game.session;

import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.network.tcp.SessionConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Session {
    private final int sessionId;
    private final ConcurrentMap<String, SessionConnection> connections = new ConcurrentHashMap<>();

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
    }

    public SessionConnection get(String key){
        if(!connections.containsKey(key)){
            connections.get(key);
        }
        return null;
    }

    public SessionConnection get(InetAddress address, String playerName){
        return get("%s@%s".formatted(address.toString(), playerName));
    }

    public void replace(SessionConnection connection){
        if(connections.containsKey(connection.key())){
            connections.replace(connection.key(), connection);
        }
        else{
            add(connection);
        }
    }

    public void remove(SessionConnection session) {
        connections.remove(session.key(), session);
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
}
