package com.avaje.ebeaninternal.server.cluster.socket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.server.lib.thread.ThreadPool;

/**
 * Serverside multithreaded socket listener. Accepts connections and dispatches
 * them to an appropriate handler.
 * <p>
 * This is designed as a single port listener, where part of the connection
 * protocol determines which service the client is requesting (rather than a
 * port per service).
 * </p>
 * <p>
 * It has its own daemon background thread that handles the accept() loop on the
 * ServerSocket.
 * </p>
 */
class SocketClusterListener implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SocketClusterListener.class);
	
    /**
     * The port the SocketListener uses.
     */
	private final int port;

    /**
     * The length of the socket accept timeout.
     */
    private final int listenTimeout = 60000;

    /**
     * The server socket used to listen for requests.
     */
    private final ServerSocket serverListenSocket;

    /**
     * The listening thread.
     */
    private final Thread listenerThread;

    /**
     * The pool of threads that actually do the parsing execution of requests.
     */
    private final ThreadPool threadPool;

    private final SocketClusterBroadcast owner;

    /**
     * shutting down flag.
     */
    boolean doingShutdown;

    /**
     * Whether the listening thread is busy assigning a request to a thread.
     */
    boolean isActive;

    /**
     * Construct with a given thread pool name.
     */
    public SocketClusterListener(SocketClusterBroadcast owner, int port) {
        this.owner = owner;
        this.threadPool = ThreadPool.createThreadPool("EbeanCluster");
        this.port = port;
        
        try {
            this.serverListenSocket = new ServerSocket(port);
            this.serverListenSocket.setSoTimeout(listenTimeout);
            this.listenerThread = new Thread(this, "EbeanClusterListener");
            
        } catch (IOException e){
            String msg = "Error starting cluster socket listener on port "+port;
            throw new RuntimeException(msg,e);
        }
    }

    /**
     * Returns the port the listener is using.
     */
    public int getPort() {
        return port;
    }

    /**
     * Start listening for requests.
     */
    public void startListening() throws IOException {
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();
    }
    
    /**
     * Shutdown this listener.
     */
    public void shutdown() {
        doingShutdown = true;
        try {
            if (isActive) {
                synchronized (listenerThread) {
                    try {
                        listenerThread.wait(1000);
                    } catch (InterruptedException e) {
                        // OK to ignore as expected to Interrupt for shutdown.
                        ;
                    }
                }
            }
            listenerThread.interrupt();
            serverListenSocket.close();
        } catch (IOException e) {
        	logger.error("Error shutting down listener", e);
        }
        
        threadPool.shutdown();
    }
    
    /**
     * This is a runnable and so this must be public. Don't call this externally
     * but rather call the startListening() method.
     */
    public void run() {
        // run in loop until doingShutdown is true...
        while (!doingShutdown) {
            try {
                synchronized (listenerThread) {
                    Socket clientSocket = serverListenSocket.accept();

                    isActive = true;
                    
                    Runnable request = new RequestProcessor(owner, clientSocket);
                    threadPool.assign(request, true);

                    isActive = false;
                }
            } catch (SocketException e) {
                if (doingShutdown) {
                    String msg = "doingShutdown and accept threw:"+ e.getMessage();
                    logger.info(msg);

                } else {
                	logger.error(null, e);
                }

            } catch (InterruptedIOException e) {
                // this will happen when the server is very quiet.
                // that is, no requests
                logger.debug("Possibly expected due to accept timeout?" + e.getMessage());

            } catch (IOException e) {
                // log it and continue in the loop...
            	logger.error(null, e);
            }
        }
    }

}
