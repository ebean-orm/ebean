package com.avaje.ebeaninternal.server.cluster.mcast;

import java.util.HashMap;
import java.util.Map;

public class OutgoingPacketsAcked {

    private long minimumGotAllPacketId;

    private Map<String, GroupMemberAck> recievedByMap = new HashMap<String, GroupMemberAck>();

    public int getGroupSize() {
        synchronized (this) {
            return recievedByMap.size();
        }
    }

    public long getMinimumGotAllPacketId() {
        synchronized (this) {
            return minimumGotAllPacketId;
        }
    }
    
    public void removeMember(String groupMember){
        synchronized (this) {
            recievedByMap.remove(groupMember);
            resetGotAllMin();
        }
    }

    private boolean resetGotAllMin() {
        
        long tempMin = Long.MAX_VALUE;
        
        for (GroupMemberAck groupMemAck : recievedByMap.values()) {
            long memberMin = groupMemAck.getGotAllPacketId();
            if (memberMin < tempMin) {
                tempMin = memberMin;
            }
        }
        
        if (tempMin != minimumGotAllPacketId) {
            minimumGotAllPacketId = tempMin;
            return true;
        } else {
            return false;
        }
    }
    
    public long receivedAck(String groupMember, MessageAck ack) {
        
        synchronized (this) {
            
            boolean checkMin = false;
            
            GroupMemberAck groupMemberAck = recievedByMap.get(groupMember);
            if (groupMemberAck == null) {
                groupMemberAck = new GroupMemberAck();
                groupMemberAck.setIfBigger(ack.getGotAllPacketId());
                recievedByMap.put(groupMember, groupMemberAck);
                checkMin = true;
            } else {
                checkMin = groupMemberAck.getGotAllPacketId() == minimumGotAllPacketId; 
                groupMemberAck.setIfBigger(ack.getGotAllPacketId());
            }
            
            boolean minChanged = false;
            
            if (checkMin || minimumGotAllPacketId == 0){                
                minChanged = resetGotAllMin();
            }
            
            return minChanged ? minimumGotAllPacketId : 0;
        }
    }

    private static class GroupMemberAck {

        private long gotAllPacketId;

        private GroupMemberAck() {
        }

        private long getGotAllPacketId() {
            return gotAllPacketId;
        }

        private void setIfBigger(long newGotAll) {
            if (newGotAll > gotAllPacketId) {
                gotAllPacketId = newGotAll;
            }
        }
    }
}
