package com.avaje.ebeaninternal.server.lib.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
    public List<MailAddress> getRecipientList() {
        return recipientList;
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
    public List<String> getBodyLines() {
        return bodylines;
    }

    /**
     * Return the headers.
     */
    public Collection<String> getHeaderFields() {
        return header.keySet();
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
        for (String key : header.keySet()) {
            String hline = key + ": " + header.get(key) + "\n";
            sb.append(hline);
        }
        sb.append("\n");
        for (String line : bodylines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}



