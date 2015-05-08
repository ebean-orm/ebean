package com.avaje.ebeaninternal.server.cluster.mcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the sending of Packets via DatagramPacket.
 * 
 * @author rbygrave
 */
public class McastSender {

    private static final Logger logger = LoggerFactory.getLogger(McastSender.class);

    private final int port;

    private final InetAddress inetAddress;

    private final DatagramSocket sock;

    private final InetSocketAddress sendAddr;

    private final String senderHostPort;

    
    public McastSender(int port, String address, int sendPort, String sendAddress) {

        try {
            this.port = port;
            this.inetAddress = InetAddress.getByName(address);

            InetAddress sendInetAddress = null;
            if (sendAddress != null) {
                sendInetAddress = InetAddress.getByName(sendAddress);
            } else {
                sendInetAddress = InetAddress.getLocalHost();
            }

            if (sendPort > 0) {
                this.sock = new DatagramSocket(sendPort, sendInetAddress);
            } else {
                this.sock = new DatagramSocket(new InetSocketAddress(sendInetAddress, 0));
            }

            String msg = "Cluster Multicast Sender on["+sendInetAddress.getHostAddress()+":"+sock.getLocalPort()+"]";
            logger.info(msg);

            this.sendAddr = new InetSocketAddress(sendInetAddress, sock.getLocalPort());
            this.senderHostPort = sendInetAddress.getHostAddress()+":"+sock.getLocalPort();
            
        } catch (Exception e) {
            String msg = "McastSender port:" + port + " sendPort:" + sendPort + " " + address;
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Return the send Address so that if we have loopback messages we can
     * detect if they where sent by this local sender and hence should be
     * ignored.
     */
    public InetSocketAddress getAddress() {
        return sendAddr;
    }

    /**
     * Return the Host and Port of the sender. This is used to uniquely identify
     * this instance in the cluster.
     */
    public String getSenderHostPort() {
        return senderHostPort;
    }

    /**
     * Send the packet.
     */
    public int sendPacket(Packet packet) throws IOException {

        byte[] pktBytes = packet.getBytes();

        if (logger.isDebugEnabled()){
            logger.debug("OUTGOING packet: " + packet.getPacketId() + " size:" + pktBytes.length);
        }

        if (pktBytes.length > 65507){
            logger.warn("OUTGOING packet: " + packet.getPacketId() + " size:" + pktBytes.length
                    +" likely to be truncated using UDP with a MAXIMUM length of 65507");
        }
        
        DatagramPacket pack = new DatagramPacket(pktBytes, pktBytes.length, inetAddress, port);
        sock.send(pack);
        
        return pktBytes.length;
    }

    /**
     * Send the list of Packets.
     */
    public int sendPackets(List<Packet> packets) throws IOException {

        int totalBytes = 0;
        for (int i = 0; i < packets.size(); i++) {
            totalBytes += sendPacket(packets.get(i));
        }
        return totalBytes;
    }
    
}
