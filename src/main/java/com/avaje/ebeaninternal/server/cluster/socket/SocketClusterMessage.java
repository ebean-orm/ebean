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

import java.io.Serializable;

import com.avaje.ebeaninternal.server.cluster.DataHolder;
import com.avaje.ebeaninternal.server.cluster.Packet;

/**
 * The messages broadcast around the cluster.
 */
public class SocketClusterMessage implements Serializable {

    private static final long serialVersionUID = 2993350408394934473L;
    
    private final String registerHost;

    private final boolean register;
    
    private final DataHolder dataHolder;
    
    public static SocketClusterMessage register(String registerHost, boolean register){
        return new SocketClusterMessage(registerHost, register);
    }

    public static SocketClusterMessage transEvent(DataHolder transEvent){
        return new SocketClusterMessage(transEvent);
    }
    
    public static SocketClusterMessage packet(Packet packet){
        DataHolder d = new DataHolder(packet.getBytes());
        return new SocketClusterMessage(d);
    }
    
    /**
     * Used to construct a Child AttributeMap.
     */
    private SocketClusterMessage(String registerHost, boolean register) {
        this.registerHost = registerHost;
        this.register = register;
        this.dataHolder = null;
    }
    
    private SocketClusterMessage(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
        this.registerHost = null;
        this.register = false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (registerHost != null){
            sb.append("register ");
            sb.append(register);
            sb.append(" ");
            sb.append(registerHost);
        } else {
            sb.append("transEvent ");
        }
        return sb.toString();
    }
    
    public boolean isRegisterEvent() {
        return registerHost != null;
    }

    public String getRegisterHost() {
        return registerHost;
    }

    public boolean isRegister() {
        return register;
    }

    public DataHolder getDataHolder() {
        return dataHolder;
    }
   
}
