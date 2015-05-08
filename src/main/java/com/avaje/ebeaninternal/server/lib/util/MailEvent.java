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
