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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Represents a relatively small independent message.
 * <p>
 * In general terms we break up a potentially large object like
 * RemoteTransactionEvent into many smaller BinaryMessages. This is so that if
 * they don't all fit on a single Packet we can easily break them up and put
 * them on multiple packets.
 * </p>
 * <p>
 * Also note that for the Multicast approach a Packet will generally contain
 * many messages each directed to different members of the cluster. So it would
 * be common for many Ack, Resend and Control messages to all be contained in a
 * single packet.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class BinaryMessage {

    public static final int TYPE_MSGCONTROL = 0;
    public static final int TYPE_BEANIUD = 1;
    public static final int TYPE_TABLEIUD = 2;
    public static final int TYPE_BEANDELTA = 3;
    public static final int TYPE_BEANPATHUPDATE = 4;
    public static final int TYPE_INDEX_INVALIDATE = 6;
    public static final int TYPE_INDEX = 7;
    public static final int TYPE_MSGACK = 8;
    public static final int TYPE_MSGRESEND = 9;

    private final ByteArrayOutputStream buffer;
    private final DataOutputStream os;
    private byte[] bytes;

    /**
     * Create with an estimated buffer size.
     */
    public BinaryMessage(int bufSize) {
        this.buffer = new ByteArrayOutputStream(bufSize);
        this.os = new DataOutputStream(buffer);
    }

    /**
     * Return the DataOutputStream to write content to.
     */
    public DataOutputStream getOs() {
        return os;
    }

    /**
     * Return all the content as a byte array.
     */
    public byte[] getByteArray() {
        if (bytes == null) {
            bytes = buffer.toByteArray();
        }
        return bytes;
    }
}
