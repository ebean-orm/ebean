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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The client side of a TCP Sockect connection.
 */
class SocketConnection {

    /**
     * The object underlying objectOutputStream. 
     */
    ObjectOutputStream oos;
    
    /**
     * The underlying ObjectInputStream.
     */
    ObjectInputStream ois;
    
    /**
     * The underlying inputStream.
     */
    InputStream is;
    
    /**
     * The underlying outputStream.
     */
    OutputStream os;
    
    /**
     * The underlying socket.
     */
    Socket socket;
   
    /**
     * Create for a given Socket.
     */
    public SocketConnection(Socket socket) throws IOException {
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
        this.socket = socket;
    }
    
    /**
     * Disconnect from the server. 
     */
    public void disconnect() throws IOException {
        os.flush();
        socket.close();
    }
    
    /**
     * Flush the outputStream.
     */
    public void flush() throws IOException {
        os.flush();
    }

    /**
     * Read an object from the object input stream.
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        return getObjectInputStream().readObject();
    }

    /**
     * Write an object to the object output stream.
     */
    public ObjectOutputStream writeObject(Object object) throws IOException {
        ObjectOutputStream oos = getObjectOutputStream(); 
        oos.writeObject(object);
        return oos;
    }
    
    /**
     * Get the object output stream.
     */
    public ObjectOutputStream getObjectOutputStream() throws IOException {
        if (oos == null){
            oos = new ObjectOutputStream(os);
        }
        return oos;
    }
    
    /**
     * Get the object input stream.
     */
    public ObjectInputStream getObjectInputStream() throws IOException {
        if (ois == null){
        	ois = new ObjectInputStream(is);
        }
        return ois;
    }
    
    
    /**
     * Set the ObjectInputStream to use.
     */
    public void setObjectInputStream(ObjectInputStream ois) {
		this.ois = ois;
	}

    /**
     * Set the ObjectOutputStream to use.
     */
	public void setObjectOutputStream(ObjectOutputStream oos) {
		this.oos = oos;
	}

	/**
     * Return the underlying input stream.
     */
    public InputStream getInputStream() throws IOException  {
        return is;
    }
    
    /**
     * Return the underlying output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        return os;
    }

}
