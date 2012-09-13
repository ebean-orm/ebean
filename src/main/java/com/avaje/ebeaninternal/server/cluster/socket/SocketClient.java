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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The client side of the socket clustering.
 */
class SocketClient {

    private static final Logger logger = Logger.getLogger(SocketClient.class.getName());
    
    private final InetSocketAddress address;
    
    private final String hostPort;
    
    private boolean online;

    private Socket socket;
    private OutputStream os;
    private ObjectOutputStream oos;
    
    /**
     * Construct with an IP address and port.
     */
    public SocketClient(InetSocketAddress address) {
        this.address = address;
        this.hostPort = address.getHostName()+":"+address.getPort();
    }

    public String getHostPort() {
        return hostPort;
    }
    
    public int getPort() {
        return address.getPort();
    }
    
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) throws IOException {
        if (online){
            setOnline();
        } else {
            disconnect();
        }
    }

    
    /**
     * Set whether the client is thought to be online.
     */
    private void setOnline() throws IOException {
        connect();
        this.online = true;
    }
      
    public void reconnect() throws IOException {
        disconnect();
        connect();
    }
    
    private void connect() throws IOException {
        if (socket != null){
            throw new IllegalStateException("Already got a socket connection?");
        }
        Socket s = new Socket();
        s.setKeepAlive(true);
        s.connect(address);
        
        this.socket = s;
        this.os = socket.getOutputStream();
    }
    
    public void disconnect() {
        this.online = false;
        if (socket != null){
            
            try {
                socket.close();
            } catch (IOException e) {
                String msg = "Error disconnecting from Cluster member "+hostPort;
                logger.log(Level.INFO, msg, e);
            }
            
            os = null;
            oos = null;
            socket = null;
        }
    }
    
    public boolean register(SocketClusterMessage registerMsg) {
        
        try {
            setOnline();
            send(registerMsg);
            return true;
        } catch (IOException e) {
            disconnect();
            return false;
        }
    }
    
    public boolean send(SocketClusterMessage msg) throws IOException {

        if (online){
            writeObject(msg);
            return true;
            
        } else {
            return false;
        }
        
    }
    
    private void writeObject(Object object) throws IOException {
        if (oos == null){
            this.oos = new ObjectOutputStream(os);
        }
        oos.writeObject(object);
        oos.flush();        
    }
    

    
}
