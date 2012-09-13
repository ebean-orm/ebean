/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
        
        long tempMin;
        if (recievedByMap.isEmpty()){
            //System.out.println("       --  -- -- -- "+recievedByMap.isEmpty());
            tempMin = Long.MAX_VALUE;
        } else {
            tempMin = Long.MAX_VALUE;
        }
        
        for (GroupMemberAck groupMemAck : recievedByMap.values()) {
            long memberMin = groupMemAck.getGotAllPacketId();
            if (memberMin < tempMin){
                //System.out.println("                                -- new tmpMin "+memberMin);
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
                //System.out.println("                               -- new groupMemberAck");
                groupMemberAck = new GroupMemberAck();
                groupMemberAck.setIfBigger(ack.getGotAllPacketId());
                recievedByMap.put(groupMember, groupMemberAck);
                checkMin = true;
            } else {
                checkMin = groupMemberAck.getGotAllPacketId() == minimumGotAllPacketId; 
                //System.out.println("                               -- existing groupMemberAck, checkMin:"+checkMin+" "+groupMemberAck.getGotAllPacketId());
                groupMemberAck.setIfBigger(ack.getGotAllPacketId());
            }
            
            boolean minChanged = false;
            
            //System.out.println("               -- checkMin:"+checkMin+" minimumGotAllPacketId:"+minimumGotAllPacketId);
            if (checkMin || minimumGotAllPacketId == 0){
                
                minChanged = resetGotAllMin();
                //System.out.println("               -- minChanged:"+minChanged+" minimumGotAllPacketId:"+minimumGotAllPacketId);
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
