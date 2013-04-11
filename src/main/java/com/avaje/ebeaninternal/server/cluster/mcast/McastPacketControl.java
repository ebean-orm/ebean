package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.Packet;
import com.avaje.ebeaninternal.server.cluster.PacketMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps co-ordinate Packet information between the McastListener and the
 * McastClusterManager.
 * 
 * @author rbygrave
 */
public class McastPacketControl {

    private static final Logger logger = LoggerFactory.getLogger(McastPacketControl.class);

    private final String localSenderHostPort;

    private final McastClusterManager owner;

    private final HashSet<String> groupMembers = new HashSet<String>();

    private final OutgoingPacketsAcked outgoingPacketsAcked = new OutgoingPacketsAcked();

    private final IncomingPacketsProcessed incomingPacketsProcessed;

    public McastPacketControl(McastClusterManager owner, String localSenderHostPort, int maxResendIncoming) {
        this.owner = owner;
        this.localSenderHostPort = localSenderHostPort;
        this.incomingPacketsProcessed = new IncomingPacketsProcessed(maxResendIncoming);
    }

    /**
     * Handle special case where cluster doesn't have any members and we don't
     * get any responses. Need to tell the sender side that the group size is 0.
     */
    protected void onListenerTimeout() {
        if (groupMembers.size() == 0) {
            owner.fromListenerTimeoutNoMembers();
        }
    }

    protected void processMessagesPacket(String senderHostPort, Packet header, DataInput dataInput, 
            long totalPacketsReceived, long totalBytesReceived, long totalTransEventsReceived) throws IOException {

        PacketMessages packetMessages = PacketMessages.forRead(header);
        packetMessages.read(dataInput);
        List<Message> messages = packetMessages.getMessages();

        if (logger.isTraceEnabled()) {
            logger.trace("INCOMING Messages " + messages);
        }
        // messages are for all nodes in the cluster so
        // we need to filter looking for messages pertaining
        // to this (senderHostPort)

        MessageControl control = null;
        MessageAck ack = null;
        MessageResend resend = null;

        // filter for relevant messages to this node
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.isControlMessage()) {
                // any 'control' message is interesting
                control = (MessageControl) message;

            } else if (localSenderHostPort.equals(message.getToHostPort())) {
                if (message instanceof MessageAck) {
                    ack = (MessageAck) message;
                } else if (message instanceof MessageResend) {
                    resend = (MessageResend) message;
                } else {
                    logger.error("Expecting a MessageAck or MessageResend but got a "
                      + message.getClass().getName());
                }
            }
        }

        if (control != null) {
            if (control.getControlType() == MessageControl.TYPE_LEAVE) {
                groupMembers.remove(senderHostPort);
                logger.info("Cluster member leaving [" + senderHostPort + "] " + groupMembers.size()
                        + " other members left");
                outgoingPacketsAcked.removeMember(senderHostPort);
                incomingPacketsProcessed.removeMember(senderHostPort);
            } else {
                groupMembers.add(senderHostPort);
            }
        }

        long newMin = 0;
        if (ack != null) {
            newMin = outgoingPacketsAcked.receivedAck(senderHostPort, ack);
        }

        if (newMin > 0 || control != null || resend != null) {
            int groupSize = groupMembers.size();
            // synchronised on the managerThread
            owner.fromListener(newMin, control, resend, groupSize, 
                    totalPacketsReceived, totalBytesReceived, totalTransEventsReceived);
        }
    }

    /**
     * Return true if we should process this packet. Return false if we have
     * already processed the packet.
     */
    public boolean isProcessPacket(String memberKey, long packetId) {

        return incomingPacketsProcessed.isProcessPacket(memberKey, packetId);
    }

    public AckResendMessages getAckResendMessages(IncomingPacketsLastAck lastAck) {

        return incomingPacketsProcessed.getAckResendMessages(lastAck);
    }

}
