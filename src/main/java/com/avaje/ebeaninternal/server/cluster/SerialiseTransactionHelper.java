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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * Mechanism to convert RemoteTransactionEvent to/from byte[] content.
 */
public abstract class SerialiseTransactionHelper {

    private final PacketWriter packetWriter;

    public SerialiseTransactionHelper() {
        packetWriter = new PacketWriter(Integer.MAX_VALUE);
    }

    public abstract SpiEbeanServer getEbeanServer(String serverName);

    /**
     * Convert the RemoteTransactionEvent to byte[] content.
     */
    public DataHolder createDataHolder(RemoteTransactionEvent transEvent) throws IOException {

        List<Packet> packetList = packetWriter.write(transEvent);
        if (packetList.size() != 1) {
            throw new RuntimeException("Always expecting 1 Packet but got " + packetList.size());
        }
        byte[] data = packetList.get(0).getBytes();
        return new DataHolder(data);
    }

    /**
     * Convert the byte[] content to RemoteTransactionEvent.
     */
    public RemoteTransactionEvent read(DataHolder dataHolder) throws IOException {

        ByteArrayInputStream bi = new ByteArrayInputStream(dataHolder.getData());
        DataInputStream dataInput = new DataInputStream(bi);

        Packet header = Packet.readHeader(dataInput);

        SpiEbeanServer server = getEbeanServer(header.getServerName());

        PacketTransactionEvent tranEventPacket = PacketTransactionEvent.forRead(header, server);
        tranEventPacket.read(dataInput);

        return tranEventPacket.getEvent();

    }
}
