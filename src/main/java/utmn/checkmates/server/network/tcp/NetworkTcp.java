package utmn.checkmates.server.network.tcp;

import utmn.checkmates.server.network.packet.PacketType;
import utmn.checkmates.server.network.packet.output.OutputPacket;
import utmn.checkmates.server.utility.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NetworkTcp {
    static InputMessage readNext(InputStream rawIn, BufferedReader in) throws IOException {
        //
        Logger.log("NetworkTcp", "readNext",
                "Ожидается чтение пакета...");
        //

        if (in == null) {
            Logger.log("NetworkTcp", "readNext",
                    "IN = NULL");
            return null;
        }

        int typeInt = rawIn.read();
        if (typeInt == -1) {
            Logger.log("NetworkTcp", "readNext",
                    "TYPEINT = -1");
            return null;
        }

        String json = in.readLine();
        if (json == null) {
            Logger.log("NetworkTcp", "readNext",
                    "JSON = null");
            return null;
        }

        //
        Logger.log("NetworkTcp", "readNext",
                "Пакет успешно прочитан: Тип: %d, JSON: %s".formatted(typeInt, json));
        //

        return new InputMessage((byte) typeInt, json);
    }

    public static InputMessage readNext(SessionConnection connection) throws IOException {
        return readNext(connection.getRawIn(), connection.getIn());
    }

    static void sendPacket(BufferedOutputStream out, OutputPacket packet) throws IOException {
        //
        Logger.log("NetworkTcp", "sendPacket",
                "Ожидается отправка пакета...");
        //

        if(out == null) return;
        synchronized (out) {
            byte typeOut = PacketType.getByClass(packet.getClass()).getValue();
            String jsonOut = packet.toJson();
            out.write(typeOut);
            out.write(jsonOut.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
            out.flush();
        }

        //
        Logger.log("NetworkTcp", "sendPacket",
                "Пакет был успешно отправлен.");
        //
    }

    public static void sendPacket(SessionConnection connection, OutputPacket packet) throws IOException {
        sendPacket(connection.getOut(), packet);
    }

    public static class InputMessage {
        public final byte type;
        public final String json;
        public InputMessage(byte type, String json) { this.type = type; this.json = json; }
    }
}
