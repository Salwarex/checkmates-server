package utmn.checkmates.server.network.tcp;

import utmn.checkmates.server.utility.logger.Logger;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnectionChecker implements Runnable{
    private final NetworkServer server;
    private Map<SessionConnection, Long> lastTimes = new HashMap<>();
    private boolean active = true;
    private final static long MS_DELAY = 30L * 1000L;
    private final static long TIMEOUT = MS_DELAY * 2;

    public ConnectionChecker(NetworkServer server){
        this.server = server;
    }

    public void update(SessionConnection connection){
        long millis = System.currentTimeMillis();
        if(lastTimes.containsKey(connection)) lastTimes.replace(connection, millis);
        else lastTimes.put(connection, millis);

        Logger.log(this.getClass().getSimpleName(), "update", "%s (%s:%d) подал сигнал жизни! Таймер отсрочен до: %d"
                .formatted(connection.getPlayerName(), connection.getAddress(), connection.getClientSocket().getPort(), millis));
    }

    public void softDisconnection(SessionConnection connection){
        //
        Logger.log(this.getClass().getSimpleName(), "softDisconnection",
                "Клиент %s (%s:%d) мягко отключается"
                        .formatted(connection.getPlayerName(), connection.getAddress(), connection.getClientSocket().getPort()));
        //
        lastTimes.remove(connection);
    }

    @Override
    public void run() {
        while (active){
            int deadSessions = 0;
            Map<SessionConnection, Long> afterCheck = new HashMap<>();

            for(SessionConnection connection : lastTimes.keySet()){
                if(System.currentTimeMillis() - lastTimes.get(connection) >= TIMEOUT){
                    deadSessions++;
                    Socket socket = connection.getClientSocket();

                    Logger.wrn("Превышено время ожидания запроса к %s:%d (Игрок %s)"
                            .formatted(socket.getInetAddress(), socket.getPort(), connection.getPlayerName()));

                    server.getConnectionsManager().closeSessionConnection(socket);
                }else{
                    afterCheck.put(connection, lastTimes.get(connection));
                }
            }

            lastTimes = afterCheck;

            Logger.log(this.getClass().getSimpleName(), "run", "Закрыто мёртвых соединений: " + deadSessions);

            try {
                Thread.sleep(MS_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop(){
        this.active = false;
    }
}
