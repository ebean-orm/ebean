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

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cluster.Packet;
import com.avaje.ebeaninternal.server.cluster.PacketTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

/**
 * Listens for Incoming packets.
 * 
 * @author rbygrave
 */
public class McastListener implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(McastListener.class);

    private final McastClusterManager owner;
    
    private final McastPacketControl packetControl;
    
    private final MulticastSocket sock;

    private final Thread listenerThread;

    private final String localSenderHostPort;

    private final InetAddress group;

    private DatagramPacket pack;

    private byte[] receiveBuffer;

    private volatile boolean shutdown;
    private volatile boolean shutdownComplete;
    
    private long totalPacketsReceived;
    private long totalBytesReceived;
    private long totalTxnEventsReceived;
    
    public McastListener(McastClusterManager owner, McastPacketControl packetControl, int port, String address, 
            int bufferSize, int timeout, String localSenderHostPort, 
            boolean disableLoopback, int ttl, InetAddress mcastBindAddress) {

        this.owner = owner;
        this.packetControl = packetControl;
        this.localSenderHostPort = localSenderHostPort;
        this.receiveBuffer = new byte[bufferSize];
        this.listenerThread = new Thread(this, "EbeanClusterMcastListener");

        String msg = "Cluster Multicast Listening address["+address+"] port["+port+"] disableLoopback["+disableLoopback+"]";
        if (ttl >= 0){
            msg +=" ttl["+ttl+"]";
        }
        if (mcastBindAddress != null){
            msg += " mcastBindAddress["+mcastBindAddress+"]";
        }
        logger.info(msg);

        try {
            this.group = InetAddress.getByName(address);
            this.sock = new MulticastSocket(port);
            this.sock.setSoTimeout(timeout);

            if (disableLoopback){
                sock.setLoopbackMode(true);
            }
            
            if (mcastBindAddress != null) {
                // bind to a specific interface
                sock.setInterface(mcastBindAddress);
            }
            
            if (ttl >= 0) {
                sock.setTimeToLive(ttl);
            }
            sock.setReuseAddress(true);
            pack = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            sock.joinGroup(group);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startListening() {
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
        
        logger.info("Cluster Multicast Listener up and joined Group");
    }

    /**
     * Shutdown this listener.
     */
    public void shutdown() {
        
        shutdown = true;
        synchronized (listenerThread) {
            try {
                // wait max 20 seconds 
                listenerThread.wait(20000);                
            } catch (InterruptedException e) {
                logger.info("InterruptedException:"+e);
            }
        }
        
        if (!shutdownComplete){
            String msg = "WARNING: Shutdown of McastListener did not complete?";
            System.err.println(msg);
            logger.warn(msg);
        }
        
        try {
            sock.leaveGroup(group);
        } catch (IOException e) {
            // send to syserr in case logging already shutdown 
            e.printStackTrace();
            String msg = "Error leaving Multicast group";
            logger.info(msg, e);
        }
        try {
            sock.close();
        } catch (Exception e) {
            // send to syserr in case logging already shutdown 
            e.printStackTrace();
            String msg = "Error closing Multicast socket";
            logger.info(msg, e);
        }
    }
    
    public void run() {
        while (!shutdown) {
            try {
                pack.setLength(receiveBuffer.length);
                sock.receive(pack);

                InetSocketAddress senderAddr = (InetSocketAddress)pack.getSocketAddress();

                String senderHostPort = senderAddr.getAddress().getHostAddress()+":"+senderAddr.getPort();                    
                
                if (senderHostPort.equals(localSenderHostPort)){
                    if (logger.isTraceEnabled()){
                        logger.info("Ignoring message as sent by localSender: "+localSenderHostPort);
                    }
                } else {
                                               
                    byte[] data = pack.getData();
                    
                    
                    ByteArrayInputStream bi = new ByteArrayInputStream(data);
                    DataInputStream dataInput = new DataInputStream(bi);
                    
                    ++totalPacketsReceived;
                    totalBytesReceived += pack.getLength();
                    
                    Packet header = Packet.readHeader(dataInput);
                    
                    long packetId = header.getPacketId();
                    boolean ackMsg = packetId == 0;
                    
                    boolean processThisPacket = ackMsg || packetControl.isProcessPacket(senderHostPort, header.getPacketId());
                    
                    if (!processThisPacket){
                        if (logger.isTraceEnabled()){
                            logger.info("Already processed packet: "+header.getPacketId()+" type:"+header.getPacketType()+" len:"+data.length);
                        }
                    } else {
                        if (logger.isTraceEnabled()){
                            logger.info("Incoming packet:"+header.getPacketId()+" type:"+header.getPacketType()+" len:"+data.length);
                        }    
                        processPacket(senderHostPort, header, dataInput);                            
                    }
                }
                
            } catch (java.net.SocketTimeoutException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("timeout", e);
                }
                packetControl.onListenerTimeout();
                
            } catch (IOException e) {
                logger.info("error ?", e);
            } 
        }
        
        shutdownComplete = true;
        
        synchronized (listenerThread) {
            listenerThread.notifyAll();
        }
    }

    protected void processPacket(String senderHostPort, Packet header, DataInput dataInput) {
        try {
            switch (header.getPacketType()) {
            case Packet.TYPE_MESSAGES:
                packetControl.processMessagesPacket(senderHostPort, header, dataInput, 
                        totalPacketsReceived, totalBytesReceived, totalTxnEventsReceived);
                break;
                
            case Packet.TYPE_TRANSEVENT:
                ++totalTxnEventsReceived;
                processTransactionEventPacket(header, dataInput);
                break;
                
            default:
                String msg = "Unknown Packet type:" + header.getPacketType();
                logger.error(msg);
                break;
            }
        } catch (IOException e) {
            // need to ask to get this packet resent...
            String msg = "Error reading Packet " + header.getPacketId() + " type:" + header.getPacketType();
            logger.error(msg, e);
        }
    }
    
    private void processTransactionEventPacket(Packet header, DataInput dataInput) throws IOException {

        SpiEbeanServer server = owner.getEbeanServer(header.getServerName());

        PacketTransactionEvent tranEventPacket = PacketTransactionEvent.forRead(header, server);
        tranEventPacket.read(dataInput);

        server.remoteTransactionEvent(tranEventPacket.getEvent());
    }


}
