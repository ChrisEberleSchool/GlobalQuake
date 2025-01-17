package gqserver.api.packets.earthquake;

import gqserver.api.Packet;
import gqserver.api.data.cluster.ClusterData;
import gqserver.api.data.earthquake.HypocenterData;
import gqserver.api.data.earthquake.advanced.AdvancedHypocenterData;

public record HypocenterDataPacket(HypocenterData data, AdvancedHypocenterData advancedHypocenterData, ClusterData clusterData) implements Packet {

}
