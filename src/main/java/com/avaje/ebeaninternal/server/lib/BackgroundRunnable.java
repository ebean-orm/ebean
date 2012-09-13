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
package com.avaje.ebeaninternal.server.lib;

/**
 * Wraps a Runnable that is registed with BackgroundThread.
 * @see BackgroundThread
 */
public class BackgroundRunnable {

    /**
     * The task to run.
     */
    Runnable runnable;
    
    /**
     * The frequency to run the task.
     */
    int freqInSecs;
    
    /**
     * The number of times the task has run.
     */
    int runCount = 0;

    /**
     * The total time taken to run the task.
     */
    long totalRunTime = 0;
    
    /**
     * The start time the task was started.
     */
    long startTimeTemp;
    
    long startAfter;
    
    /**
     * Used to disable/enable a task.
     */
    boolean isActive = true;

    public BackgroundRunnable(Runnable runnable, int freqInSecs){
    	this(runnable, freqInSecs, System.currentTimeMillis()+1000*(freqInSecs+10));
    }
    
    public BackgroundRunnable(Runnable runnable, int freqInSecs, long startAfter){
        this.runnable = runnable;
        this.freqInSecs = freqInSecs;
        this.startAfter = startAfter;
    }

    /**
     * Return true if this can be run now.
     * <p>
     * This is used to stop jobs firing immediately.
     * </p>
     */
    public boolean runNow(long now){
    	return now > startAfter;
    }
    
    /**
     * Returns true if the task is currently enabled.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set this to false to stop this task from running.
     * Useful to temporarily disable a particular task.
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Mark the start time of a task run.
     */
    protected void runStart() {
        startTimeTemp = System.currentTimeMillis();
    }
    
    /**
     * Mark the end time of a task run.
     */
    protected void runEnd(){
        runCount++;
        long exeTime = System.currentTimeMillis() - startTimeTemp;
        totalRunTime = totalRunTime + exeTime; 
    }
    
    /**
     * Return the number of times this task was run.
     */
    public int getRunCount() {
        return runCount;
    }
    
    /**
     * Return the average time this task takes to run.
     */
    public long getAverageRunTime() {
        if (runCount == 0){
            return 0;
        }
        return totalRunTime/runCount;
    }
    
    /**
     * Return the frequency in seconds that this task runs.
     */
    public int getFreqInSecs() {
        return freqInSecs;
    }
    
    /**
     * Set the frequency in seconds that this task runs.
     */
    public void setFreqInSecs(int freqInSecs) {
        this.freqInSecs = freqInSecs;
    }
    
    /**
     * Return the underlying runnable.
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Set the underlying runnable.
     */
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(runnable.getClass().getName());
        sb.append(" freq:").append(freqInSecs);
        sb.append(" count:").append(getRunCount());
        sb.append(" avgTime:").append(getAverageRunTime());
        sb.append("]");
        return sb.toString();
    }
}
