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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A simple test message that can be sent via smtp.
 */
public class MailMessage {
    
//    /**
//     * The subject text.
//     */
//    String subject;

    /**
     * The body content.
     */
    ArrayList<String> bodylines; 

    /**
     * The sender email address.
     */
    MailAddress senderAddress;

    /**
     * The headers.
     */
    HashMap<String,String> header = new HashMap<String, String>();

    /**
     * the recipient of the email.
     */
    MailAddress currentRecipient;
    
    /**
     * The list of recipients.
     */
    ArrayList<MailAddress> recipientList = new ArrayList<MailAddress>();
    
    /**
     * Create the message.
     */
    public MailMessage() {
        bodylines = new ArrayList<String>();
    }

    /**
     * Set the current recipient.
     */
    public void setCurrentRecipient(MailAddress currentRecipient){
        this.currentRecipient = currentRecipient;
    }

    /**
     * Return the current recipient.
     */
    public MailAddress getCurrentRecipient() {
        return currentRecipient;
    }

    /**
     * Add a recipient.
     */
    public void addRecipient(String alias, String emailAddress){
        recipientList.add(new MailAddress(alias, emailAddress));
    }
    
    /**
     * Set the sender details.
     */
    public void setSender(String alias, String senderEmail){
        this.senderAddress = new MailAddress(alias, senderEmail);
    }
    /**
     * Return the sender address.
     */
    public MailAddress getSender() {
        return senderAddress;
    }

    /**
     * Return the recipient list.
     */
    public Iterator<MailAddress> getRecipientList() {
        return recipientList.iterator();
    }

    /**
     * add a header to the message.
     */
    public void addHeader(String key, String val) {
        header.put(key, val);
    }

    /**
     * Set the subject text.
     */
    public void setSubject(String subject){
        addHeader("Subject", subject);
    }

    /**
     * Return the subject text.
     */
    public String getSubject() {
        return getHeader("Subject");
    }

    /**
     * Add text to the body.
     */
    public void addBodyLine(String line) {
        bodylines.add(line);
    }

    /**
     * Return the body text.
     */
    public Iterator<String> getBodyLines() {
        return bodylines.iterator();
    }

    /**
     * Return the headers.
     */
    public Iterator<String> getHeaderFields() {
        return header.keySet().iterator();
    }

    /**
     * Return a given header.
     */
    public String getHeader(String key) {
        return header.get(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("Sender: " + senderAddress + "\tRecipient: " + recipientList + "\n");
        Iterator<String> hi = header.keySet().iterator();
        while (hi.hasNext()) {
            String key = hi.next();
            String hline = key + ": " + header.get(key) + "\n";
            sb.append(hline);
        }
        sb.append("\n");
        Iterator<String> e = bodylines.iterator();
        while (e.hasNext()) {
            sb.append(e.next()).append("\n");
        }
        return sb.toString();
    }
}



