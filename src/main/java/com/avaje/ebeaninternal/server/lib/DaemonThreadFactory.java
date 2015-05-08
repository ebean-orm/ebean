package com.avaje.ebeaninternal.server.lib;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory for Daemon threads.
 * <p>
 * Daemon threads do not stop a JVM stopping. If an application only has Daemon
 * threads left it will shutdown.
 * </p>
 * <p>
 * In using Daemon threads you need to either not care about being interrupted
 * on shutdown or register with the JVM shutdown hook to perform a nice shutdown
 * of the daemon threads etc.
 * </p>
 * 
 * @author rbygrave
 */
public class DaemonThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    
    private final ThreadGroup group;
    
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    
    private final String namePrefix;

    public DaemonThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix != null ? namePrefix : "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {

        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

        t.setDaemon(true);

        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }

        return t;
    }
}