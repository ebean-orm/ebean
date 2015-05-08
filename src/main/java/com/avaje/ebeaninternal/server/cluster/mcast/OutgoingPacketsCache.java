package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.avaje.ebeaninternal.server.cluster.Packet;

/**
 * Cache of the outgoing packets.
 * <p>
 * These are held until we receive ACKs from the other members of the cluster to
 * say they have received the packets.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class OutgoingPacketsCache {

    private final Map<Long, Packet> packetMap = new TreeMap<Long, Packet>();

    public int size() {
        return packetMap.size();
    }

    public Packet getPacket(Long packetId) {
        return packetMap.get(packetId);
    }

    public String toString() {
        return packetMap.keySet().toString();
    }

    /**
     * Remove the packet when we give up trying to send it out.
     */
    public void remove(Packet packet) {
        packetMap.remove(packet.getPacketId());
    }

    public void registerPackets(List<Packet> packets) {
        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            packetMap.put(p.getPacketId(), p);
        }
    }

    public int trimAll() {
        int size = packetMap.size();
        packetMap.clear();
        return size;
    }

    public void trimAcknowledgedMessages(long minAcked) {
        Iterator<Long> it = packetMap.keySet().iterator();
        while (it.hasNext()) {
            Long pktId = it.next();
            if (minAcked >= pktId.longValue()) {
                it.remove();
            }
        }
    }

}
