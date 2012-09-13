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
package com.avaje.ebeaninternal.server.cluster;

import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;


/**
 * Sends messages to the cluster members.
 */
public interface ClusterBroadcast {

    /**
     * Inform the other cluster members that this instance has come online and
     * start any listeners etc.
     */
    public void startup(ClusterManager clusterManager);

    /**
     * Inform the other cluster members that this instance is leaving and
     * shutdown any listeners.
     */
    public void shutdown();
    
    /**
     * Send a transaction event to all the members of the cluster.
     */
    public void broadcast(RemoteTransactionEvent remoteTransEvent);

}
