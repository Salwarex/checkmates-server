package utmn.checkmates.server.utility;

import utmn.checkmates.server.network.packet.output.OutputPacket;

import java.util.List;

public class FormatUtils {
    public static String listOutputs(List<OutputPacket> outputPackets){
        StringBuilder result = new StringBuilder();
        for(OutputPacket packet : outputPackets){
            result.append(packet.getClass().getSimpleName() + packet.toJson() + ",");
        }
        return result.toString();
    }
}
