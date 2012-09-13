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
package com.avaje.ebeaninternal.server.cluster;

import java.io.DataInput;
import java.io.IOException;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.TransactionEventTable.TableIUD;
import com.avaje.ebeaninternal.server.transaction.BeanDelta;
import com.avaje.ebeaninternal.server.transaction.BeanPersistIds;
import com.avaje.ebeaninternal.server.transaction.IndexEvent;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * A Packet holding TransactionEvent data.
 * <p>
 * Due to the hard limit for UDP packet sizes a RemoteTransactionEvent
 * is actually broken up into smaller messages.
 * </p>
 * @author rbygrave
 */
public class PacketTransactionEvent extends Packet {

    private final SpiEbeanServer server;
    
    private final RemoteTransactionEvent event;

    public static PacketTransactionEvent forWrite(long packetId, long timestamp, String serverName) throws IOException {
        return new PacketTransactionEvent(true, packetId, timestamp, serverName);
    }
    
    private PacketTransactionEvent(boolean write, long packetId, long timestamp, String serverName) throws IOException {
        super(write, TYPE_TRANSEVENT, packetId, timestamp, serverName);
        this.server = null;
        this.event = null;
    }

    private PacketTransactionEvent(Packet header, SpiEbeanServer server) throws IOException {
        super(false, TYPE_TRANSEVENT, header.packetId, header.timestamp, header.serverName);
        this.server = server;
        this.event = new RemoteTransactionEvent(server);
    }

    public static PacketTransactionEvent forRead(Packet header, SpiEbeanServer server) throws IOException {
        return new PacketTransactionEvent(header, server);
    }
 
    public RemoteTransactionEvent getEvent() {
        return event;
    }

    protected void readMessage(DataInput dataInput, int msgType) throws IOException {
        
        switch (msgType) {
        case BinaryMessage.TYPE_BEANIUD:
            event.addBeanPersistIds(BeanPersistIds.readBinaryMessage(server, dataInput));
            break;
            
        case BinaryMessage.TYPE_TABLEIUD:
            event.addTableIUD(TableIUD.readBinaryMessage(dataInput));
            break;

        case BinaryMessage.TYPE_BEANDELTA:
            event.addBeanDelta(BeanDelta.readBinaryMessage(server, dataInput));
            break;

        case BinaryMessage.TYPE_INDEX:
            event.addIndexEvent(IndexEvent.readBinaryMessage(dataInput));
            break;

        default:
            throw new RuntimeException("Invalid Transaction msgType "+msgType);
        }
    }
    
}
