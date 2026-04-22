package utmn.checkmates.server.network.tcp;

import utmn.checkmates.server.game.session.Player;
import utmn.checkmates.server.game.session.Session;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SessionConnection implements AutoCloseable{
    private Session session;
    private InetAddress address;
    private Player player;
    private Socket clientSocket;
    private InputStream rawIn;
    private BufferedReader in;
    private BufferedOutputStream out;
    private volatile boolean active = true;

    public SessionConnection() {
    }

    public SessionConnection(Session session, Socket socket) throws IOException {
        //
        Logger.log(this.getClass().getSimpleName(), "Constructor",
                "Создаётся новое соединение с сессией.");
        //

        setClientSocket(socket);
        this.session = session;
        this.address = socket.getInetAddress();
        this.player = new Player("Player from %s".formatted(socket.getInetAddress()));
    }

    public SessionConnection(Session session, Socket socket, String playerName) throws IOException {
        //
        Logger.log(this.getClass().getSimpleName(), "Constructor",
                "Создаётся новое соединение с сессией.");
        //

        setClientSocket(socket);
        this.session = session;
        this.address = socket.getInetAddress();
        this.player = new Player(playerName);
    }

    public Session getSession() {
        return session;
    }

    public String key(){
        return player.getPlayerName() + "@" + address.toString();
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getPlayerName() {
        return player.getPlayerName();
    }

    public Player getPlayer(){
        return player;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public InputStream getRawIn() {
        return rawIn;
    }

    public BufferedReader getIn() {
        return in;
    }

    public BufferedOutputStream getOut() {
        return out;
    }

    public void setClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.address = clientSocket.getInetAddress();
    }

    public void sendPacket(OutputPacket packet) throws IOException {
        if (!active) throw new IOException("Сессия неактивна");
        NetworkTcp.sendPacket(out, packet);
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPlayerName(String playerName) {
        this.player.setPlayerName(playerName);
    }

    public void setRawIn(InputStream rawIn) {
        this.rawIn = rawIn;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public void setOut(BufferedOutputStream out) {
        this.out = out;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active && clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void setPlayer(Player player) {
        this.player = player;
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
        Logger.log("SessionConnection", "close", "Сессия закрыта: " + key());
    }
}
