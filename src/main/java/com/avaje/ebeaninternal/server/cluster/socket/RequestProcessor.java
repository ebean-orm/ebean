package com.avaje.ebeaninternal.server.cluster.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This parses and dispatches a request to the appropriate handler.
 * <p>
 * Looks up the appropriate RequestHandler 
 * and then gets it to process the Client request.<P>
 * </p>
 * Note that this is a Runnable because it is assigned to the ThreadPool.
 */
class RequestProcessor implements Runnable {

	private static final Logger logger = Logger.getLogger(RequestProcessor.class.getName());
	
    private final Socket clientSocket;
    
    private final SocketClusterBroadcast owner;
    
    /**
	 *  Create including the Listener (used to lookup the Request Handler) and
	 *  the socket itself. 
	 */
	public RequestProcessor(SocketClusterBroadcast owner, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.owner = owner;
	}
	
	/**
	 *  This will parse out the command.  Lookup the appropriate Handler and 
	 *  pass the information to the handler for processing.
	 *  <P>Dev Note: the command parsing is processed here so that it is preformed
	 *  by the assigned thread rather than the listeners thread.</P>
	 */
	public void run() {
		try {
			SocketConnection sc = new SocketConnection(clientSocket);
			
			while(true){
			    if (owner.process(sc)) {
			        // got the offline message or timeout
			        break;
			    }
			}
			sc.disconnect();
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, null, e);
		} catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, null, e);
        }
	}


}; 
