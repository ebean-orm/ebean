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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends simple MailMessages via smtp. 
 */
public class MailSender implements Runnable {
    
	private static final Logger logger = Logger.getLogger(MailSender.class.getName());
	
    int traceLevel = 0;
    
    Socket sserver;
    String server;

    BufferedReader in;

    OutputStreamWriter out;
    
    MailMessage message;

    MailListener listener = null;

    private static final int SMTP_PORT = 25;

    /**
     * Create for a given mail server.
     */
    public MailSender(String server) {
        this.server = server;
    }
    
    /**
     * Set the listener to handle MessageEvents.
     */
    public void setMailListener(MailListener listener){
        this.listener = listener;
    }

    /**
     * Send the message.
     */
    public void run() {
        send(message);
    }

    /**
     * Send the message in a background thread.
     */
    public void sendInBackground(MailMessage message){
        this.message = message;
        Thread thread = new Thread(this);
        thread.start();
    }

    
    /**
     * Send the message in the current thread.
     */
    public void send(MailMessage message){
        try {
            Iterator<MailAddress> i = message.getRecipientList();
            while (i.hasNext()) {
                MailAddress recipientAddress = (MailAddress) i.next();
                sserver = new Socket(server, SMTP_PORT);
                send(message, sserver, recipientAddress);
                sserver.close(); 
                
                if (listener != null){
                    MailEvent event = new MailEvent(message, null);
                    listener.handleEvent(event);
                }
            }
        } catch (Exception ex) {
            if (listener != null){
                MailEvent event = new MailEvent(message, ex);
                listener.handleEvent(event);
            } else {
            	logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void send(MailMessage message, Socket sserver, MailAddress recipientAddress) throws IOException {

        	// A bit convoluted, but doesn't depend on DNS in any way...
            InetAddress localhost = sserver.getLocalAddress();
            String localaddress = localhost.getHostAddress();
            MailAddress sender = message.getSender();
            message.setCurrentRecipient(recipientAddress);

            // Mandatory header fields, Date and From
            if (message.getHeader("Date") == null){
                message.addHeader("Date", new java.util.Date().toString());
            }
            if (message.getHeader("From") == null){
                message.addHeader("From", sender.getAlias() + " <" + sender.getEmailAddress() + ">");
            }

            //if (message.getHeader("From") == null){
                message.addHeader("To", recipientAddress.getAlias() + " <" + recipientAddress.getEmailAddress() + ">");
            //}
            
            out = new OutputStreamWriter(sserver.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sserver.getInputStream()));
            String sintro = readln();
            if (!sintro.startsWith("220")) { // 220
            	logger.fine("SmtpSender: intro==" + sintro);
                return;
            }

            writeln("EHLO " + localaddress);
            if (!expect250()) {
                return;
            }

            writeln("MAIL FROM:<" + sender.getEmailAddress() + ">");
            if (!expect250()) {
                return;
            }
            writeln("RCPT TO:<" + recipientAddress.getEmailAddress() + ">");
            if (!expect250()){
                return;
            }
            writeln("DATA");
            while (true) { // may be multiple 250 replies pending from server
                String line = readln();
                if (line.startsWith("3"))
                    break; // ready to send
                if (!line.startsWith("2")) {
                    logger.fine("SmtpSender.send reponse to DATA: " + line);
                    return;
                }
            }
            Iterator<String> hi = message.getHeaderFields();
            while (hi.hasNext()) {
                String key = (String) hi.next();
                writeln(key + ": " + message.getHeader(key));
            }
            writeln(""); // end of header;
            Iterator<String> e = message.getBodyLines();
            while (e.hasNext()) {
                String bline = (String) e.next();
                if (bline.startsWith(".")) {
                    bline = "." + bline;
                }
                writeln(bline);
            }
            writeln(".");
            expect250();
            writeln("QUIT");

    }

    private boolean expect250() throws IOException {
        String line = readln();
        if (!line.startsWith("2")) {
            logger.info("SmtpSender.expect250: " + line);
            return false;
        }
        return true;
    }

    private void writeln(String s) throws IOException {
        if (traceLevel > 2){
            logger.fine("From client: " + s);
        }
        out.write(s + "\r\n");
        out.flush();
    }

    private String readln() throws IOException {
        String line = in.readLine();
        if (traceLevel > 1){
            logger.fine("From server: " + line);
        }
        return line;
    }

    
    /**
     * Set the trace level.
     */
    public void setTraceLevel(int traceLevel){
        this.traceLevel = traceLevel;
    }

	/**
	 * Return the hostname of the local machine.
	 */
	public String getLocalHostName() { 
		try {  
			InetAddress ipaddress = InetAddress.getLocalHost();  
			String localHost = ipaddress.getHostName();
			if (localHost == null) {
				return "localhost"; 
			} else {
				return localHost;
			}
		} catch (UnknownHostException e) {  
			return "localhost";  
		}  
	} 
}
