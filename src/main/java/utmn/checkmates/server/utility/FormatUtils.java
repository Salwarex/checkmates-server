package utmn.checkmates.server.utility;

import utmn.checkmates.server.network.packet.output.OutputPacket;

import java.net.InetAddress;
import java.util.List;

public class FormatUtils {
    public static String listOutputs(List<OutputPacket> outputPackets){
        StringBuilder result = new StringBuilder();
        for(OutputPacket packet : outputPackets){
            result.append("%s to %s %s;".formatted(packet.getClass().getSimpleName(), listAddresses(packet.getDestinationAddresses()), packet.toJson()));
        }
        return result.toString();
    }

    public static String listAddresses(List<InetAddress> addresses){
        StringBuilder result = new StringBuilder();
        for(InetAddress addr : addresses){
            result.append(addr).append(", ");
        }
        return result.toString();
    }
}
