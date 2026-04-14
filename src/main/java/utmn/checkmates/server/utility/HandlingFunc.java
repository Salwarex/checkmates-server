package utmn.checkmates.server.utility;

import utmn.checkmates.server.network.packet.HandlingException;
import utmn.checkmates.server.network.packet.input.InputPacket;
import utmn.checkmates.server.network.packet.output.OutputPacket;

@FunctionalInterface
public interface HandlingFunc {
    OutputPacket handle(InputPacket packet) throws HandlingException;
}
