/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.cluster.socket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cluster.ClusterBroadcast;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.cluster.DataHolder;
import com.avaje.ebeaninternal.server.cluster.SerialiseTransactionHelper;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * Broadcast messages across the cluster using sockets. 
 */
public class SocketClusterBroadcast implements ClusterBroadcast {

	private static final Logger logger = Logger.getLogger(SocketClusterBroadcast.class.getName());
	
	private final SocketClient local;
    
	private final HashMap<String,SocketClient> clientMap;
        
    private final SocketClusterListener listener;
	
    private SocketClient[] members;

    private ClusterManager clusterManager;

    private final TxnSerialiseHelper txnSerialiseHelper = new TxnSerialiseHelper();    
    
    private final AtomicInteger txnOutgoing = new AtomicInteger();
    private final AtomicInteger txnIncoming = new AtomicInteger();
    
    
	public SocketClusterBroadcast( ){
	    
        String localHostPort = GlobalProperties.get("ebean.cluster.local", null);
        String members = GlobalProperties.get("ebean.cluster.members", null);

        logger.info("Clustering using Sockets local["+localHostPort+"] members["+members+"]");
        
        this.local = new SocketClient(parseFullName(localHostPort));
        this.clientMap = new HashMap<String, SocketClient>();
        
        String[] memArray = StringHelper.delimitedToArray(members, ",", false);
        for (int i = 0; i < memArray.length; i++) {
            InetSocketAddress member = parseFullName(memArray[i]);
            SocketClient client = new SocketClient(member);
            if (!local.getHostPort().equalsIgnoreCase(client.getHostPort())) {
                // don't add the local one ...
                clientMap.put(client.getHostPort(), client);
            }
        }
        
        this.members = clientMap.values().toArray(new SocketClient[clientMap.size()]);
        this.listener = new SocketClusterListener(this, local.getPort());
	}
	
	/**
	 * Return the current status of this instance.
	 */
	public SocketClusterStatus getStatus() {
	    
	    // count of online members
	    int currentGroupSize = 0;
        for (int i = 0; i < members.length; i++) {
            if (members[i].isOnline()) {
                ++currentGroupSize;
            }            
        }
	    int txnIn = txnIncoming.get();
	    int txnOut = txnOutgoing.get();
	    
	    return new SocketClusterStatus(currentGroupSize, txnIn, txnOut);
	}
		
	public void startup(ClusterManager clusterManager) {
	    
	    this.clusterManager = clusterManager;
        try {
            listener.startListening();
            register();

        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    public void shutdown() {
	    deregister();
	    listener.shutdown();
	}
		
    /**
     * Register with all the other members of the Cluster.
     */
    private void register() {

        SocketClusterMessage h = SocketClusterMessage.register(local.getHostPort(), true);
        
        for (int i = 0; i < members.length; i++) {
            boolean online = members[i].register(h);
            
            String msg = "Cluster Member ["+members[i].getHostPort()+"] online["+online+"]";
            logger.info(msg);
        }
    }

    protected void setMemberOnline(String fullName, boolean online) throws IOException {
        synchronized (clientMap) {
            String msg = "Cluster Member ["+fullName+"] online["+online+"]";
            logger.info(msg);
            SocketClient member = clientMap.get(fullName);
            member.setOnline(online);
        }
    }

    private void send(SocketClient client, SocketClusterMessage msg) {

        try {
            // alternative would be to connect/disconnect here
            // but prefer to use keepalive 
            client.send(msg);
            
        } catch (Exception ex){
            logger.log(Level.SEVERE, "Error sending message", ex);
            try {
                client.reconnect();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error trying to reconnect", ex);
            }
        }
    }

    /**
     * Send the payload to all the members of the cluster.
     */
    public void broadcast(RemoteTransactionEvent remoteTransEvent) {
    	try {
    	    
    	    txnOutgoing.incrementAndGet();
            DataHolder dataHolder = txnSerialiseHelper.createDataHolder(remoteTransEvent);
            SocketClusterMessage msg = SocketClusterMessage.transEvent(dataHolder);
            broadcast(msg);
    	} catch (Exception e){
    	    String msg = "Error sending RemoteTransactionEvent "+remoteTransEvent+" to cluster members.";
    	    logger.log(Level.SEVERE, msg, e);
    	}
    }

    protected void broadcast(SocketClusterMessage msg) {
        
        for (int i = 0; i < members.length; i++) {
            send(members[i], msg);
        }
    }
    
    /**
     * Leave the cluster.
     */
    private void deregister() {
        
        SocketClusterMessage h = SocketClusterMessage.register(local.getHostPort(), false);
        broadcast(h);
        
        for (int i = 0; i < members.length; i++) {
            members[i].disconnect();
        }
    }

    /**
     * Process a Cluster message.
     */
    protected boolean process(SocketConnection request) throws IOException, ClassNotFoundException {

        try {
            SocketClusterMessage h = (SocketClusterMessage)request.readObject();
            
            if (h.isRegisterEvent()){
                setMemberOnline(h.getRegisterHost(), h.isRegister());
            
            } else {
                txnIncoming.incrementAndGet();
                DataHolder dataHolder = h.getDataHolder();
                RemoteTransactionEvent transEvent = txnSerialiseHelper.read(dataHolder);
                transEvent.run();
            }
            
            if (h.isRegisterEvent() && !h.isRegister()){
                // instance shutting down  
                return true;
            } else {
                return false;
            }
        } catch (InterruptedIOException e) {
            String msg = "Timeout waiting for message";
            logger.log(Level.INFO, msg, e);
            try {
                request.disconnect();
            } catch (IOException ex){
                logger.log(Level.INFO, "Error disconnecting after timeout", ex);    
            }
            return true;
        }
    }
    

    /**
     * Parse a host:port into a InetSocketAddress.
     */
    private InetSocketAddress parseFullName(String hostAndPort) {
        
        try {
            hostAndPort = hostAndPort.trim();
            int colonPos = hostAndPort.indexOf(":");
            if (colonPos == -1) {
                String msg = "No colon \":\" in "+hostAndPort;
                throw new IllegalArgumentException(msg);
            }
            String host = hostAndPort.substring(0, colonPos);
            String sPort = hostAndPort.substring(colonPos + 1, hostAndPort.length());
            int port = Integer.parseInt(sPort);
            
            return new InetSocketAddress(host, port);
            
        } catch (Exception ex){
            throw new RuntimeException("Error parsing ["+hostAndPort+"] for the form [host:port]", ex);
        }
    }
    
    class TxnSerialiseHelper extends SerialiseTransactionHelper {

        @Override
        public SpiEbeanServer getEbeanServer(String serverName) {
            return (SpiEbeanServer)clusterManager.getServer(serverName);
        }
    }
}
