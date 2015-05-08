package com.avaje.ebeaninternal.server.cluster.mcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For Incoming Packets remembers the packets we have received and processed.
 * <p>
 * This determines the gotAllPoint per cluster member and identifies missing
 * packets (gap between gotAllPoint and gotMaxPoint).
 * </p>
 * <p>
 * This information is used by the managerThread so send ACK's for messages we
 * have received and RESEND messages to fill the missing packets we have
 * detected.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class IncomingPacketsProcessed {

    private final ConcurrentHashMap<String, GotAllPoint> mapByMember = new ConcurrentHashMap<String, GotAllPoint>();

    private final int maxResendIncoming;
    
    public IncomingPacketsProcessed(int maxResendIncoming) {
        this.maxResendIncoming = maxResendIncoming;
    }
    
    public void removeMember(String memberKey) {
        mapByMember.remove(memberKey);
    }

    /**
     * Return true if we should process this packet. Return false if we have
     * already processed the packet.
     */
    public boolean isProcessPacket(String memberKey, long packetId) {

        GotAllPoint memberPackets = getMemberPackets(memberKey);
        return memberPackets.processPacket(packetId);
    }

    /**
     * Build the list of ACK and RESEND messages that we should send out
     * to the other members of the cluster.
     */
    public AckResendMessages getAckResendMessages(IncomingPacketsLastAck lastAck) {

        // Called by the McastClusterBroadcast manager thread

        AckResendMessages response = new AckResendMessages();

        for (GotAllPoint member : mapByMember.values()) {

            MessageAck lastAckMessage = lastAck.getLastAck(member.getMemberKey());

            member.addAckResendMessages(response, lastAckMessage);
        }

        return response;
    }

    private GotAllPoint getMemberPackets(String memberKey) {

        // This method is only called single threaded
        // by the listener thread so I'm happy that this
        // put into mapByMember is ok.
        GotAllPoint memberGotAllPoint = mapByMember.get(memberKey);
        if (memberGotAllPoint == null) {
            memberGotAllPoint = new GotAllPoint(memberKey, maxResendIncoming);
            mapByMember.put(memberKey, memberGotAllPoint);
        }
        return memberGotAllPoint;
    }

    /**
     * Keeps track of packets received from a particular member of the cluster.
     * <p>
     * It notes the packetIds of the packets received and uses those to maintain
     * the 'gotAllPoint'. The 'gotAllPoint' is the packetId which we know we
     * received all the previous packets.
     * </p>
     */
    public static class GotAllPoint {

        private static final Logger logger = LoggerFactory.getLogger(GotAllPoint.class);
        
        private final String memberKey;
        private final int maxResendIncoming;

        private long gotAllPoint;

        private long gotMaxPoint;

        /**
         * Packets received out of order.
         */
        private ArrayList<Long> outOfOrderList = new ArrayList<Long>();

        private HashMap<Long,Integer> resendCountMap = new HashMap<Long,Integer>();

        public GotAllPoint(String memberKey, int maxResendIncoming) {
            this.memberKey = memberKey;
            this.maxResendIncoming = maxResendIncoming;
        }

        /**
         * Add ACK and RESEND messages if required.
         */
        public void addAckResendMessages(AckResendMessages response, MessageAck lastAckMessage) {

            synchronized (this) {
                if (lastAckMessage != null && lastAckMessage.getGotAllPacketId() >= gotAllPoint) {
                    // nothing has changed
                } else {
                    // ACK that we have got every packet up to gotAllPoint
                    response.add(new MessageAck(memberKey, gotAllPoint));
                }

                if (getMissingPacketCount() > 0) {
                    // Ask for these Packets to be RESENT
                    List<Long> missingPackets = getMissingPackets();
                    response.add(new MessageResend(memberKey, missingPackets));
                }
            }
        }

        public String getMemberKey() {
            return memberKey;
        }

        public long getGotAllPoint() {
            synchronized (this) {
                return gotAllPoint;
            }
        }

        public long getGotMaxPoint() {
            synchronized (this) {
                return gotMaxPoint;
            }
        }

        private int getMissingPacketCount() {
            if (gotMaxPoint <= gotAllPoint) {
                if (!resendCountMap.isEmpty()) {
                    resendCountMap.clear();
                }
                return 0;
            }
            return (int) (gotMaxPoint - gotAllPoint) - outOfOrderList.size();
        }

        public List<Long> getMissingPackets() {

            synchronized (this) {
                ArrayList<Long> missingList = new ArrayList<Long>();

                // this is not particularly efficient but expecting
                // the outOfOrderList to be relatively small

                boolean lostPacket = false;
                
                for (long i = gotAllPoint + 1; i < gotMaxPoint; i++) {
                    Long packetId = Long.valueOf(i);
                    if (!outOfOrderList.contains(packetId)) {
                        if (incrementResendCount(packetId)) {
                            // request this packet be resent
                            missingList.add(packetId);
                        } else {
                            lostPacket = true;    
                        }
                    }
                }
                
                if (lostPacket){
                    checkOutOfOrderList();
                }

                return missingList;
            }
        }
                
        /**
         * Return true if this packet has not yet exceeded the maxResendCount.
         */
        private boolean incrementResendCount(Long packetId){
            Integer resendCount = resendCountMap.get(packetId);
            if (resendCount != null){
                int i = resendCount.intValue() + 1;
                if (i > maxResendIncoming){
                    // we are going to give up trying to get this packet now
                    logger.warn("Exceeded maxResendIncoming["+maxResendIncoming+"] for packet["+packetId+"]. Giving up on requesting it.");
                    resendCountMap.remove(packetId);
                    outOfOrderList.add(packetId);
                    return false;
                }
                resendCount = Integer.valueOf(i);
                resendCountMap.put(packetId, resendCount);
            } else {
                resendCountMap.put(packetId, ONE);   
            }
            return true;
        }
        
        private static final Integer ONE = Integer.valueOf(1);

        public boolean processPacket(long packetId) {
            synchronized (this) {
                
                if (gotAllPoint == 0) {
                    gotAllPoint = packetId;
                    return true;
                }
                if (packetId <= gotAllPoint) {
                    // already processed this packet
                    return false;
                }
                
                if (!resendCountMap.isEmpty()){
                    resendCountMap.remove(Long.valueOf(packetId));
                }
                
                if (packetId == gotAllPoint + 1) {
                    gotAllPoint = packetId;
                } else {
                    if (packetId > gotMaxPoint) {
                        gotMaxPoint = packetId;
                    }
                    outOfOrderList.add(Long.valueOf(packetId));
                }
                checkOutOfOrderList();
                return true;
            }
        }

        private void checkOutOfOrderList() {

            if (outOfOrderList.size() == 0) {
                return;
            }

            boolean continueCheck;
            do {
                continueCheck = false;
                long nextPoint = gotAllPoint + 1;

                Iterator<Long> it = outOfOrderList.iterator();
                while (it.hasNext()) {
                    Long id = it.next();
                    if (id.longValue() == nextPoint) {
                        // we found the next one in the outOfOrderList
                        it.remove();
                        gotAllPoint = nextPoint;
                        continueCheck = true;
                        break;
                    }
                }
            } while (continueCheck);

        }

    }

    
     
}
