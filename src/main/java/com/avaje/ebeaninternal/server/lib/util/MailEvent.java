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
package com.avaje.ebeaninternal.server.lib.util;

/**
 * Represents the success or failure of a mail send.
 */
public class MailEvent {


    /**
     * The error indicating a send failure.
     */
    Throwable error;
    
    /**
     * The message that was sent.
     */
    MailMessage message;
    
    
    /**
     * The message send failed with an error.
     */
    public MailEvent(MailMessage message, Throwable error){
        this.message = message;
        this.error = error;
    }
    
    /**
     * The message that we attempted to send.
     */
    public MailMessage getMailMessage() {
        return message;
    }
    
    /**
     * Returns true if the message was sent successfully. 
     */
    public boolean wasSuccessful() {
        return (error == null);
    }
    
    /**
     * The error indicating the send failed.
     */
    public Throwable getError() {
        return error;
    }

}
