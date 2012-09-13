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
package com.avaje.ebeaninternal.server.lib.thread;


/**
 * Used internally by the ThreadPool to wrap a Runnable that is
 * going to be run.
 * 
 * <p>Used to maintains some useful times about the Runnable in terms of when
 * it was queued and then eventually run.</p>
 */
public class Work {

	/**
	 * Create a Runnable Work.
	 */
	public Work(Runnable runnable) {
		this.runnable = runnable;
	}
	
	/**
	 * Return the associated Runnable object.
	 */
	public Runnable getRunnable() {
		return runnable;
	}
	
	/**
	 * Return the time this work actually started.
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the time this work actually started.
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Return the time this entered the queue.
	 */
	public long getEnterQueueTime() {
		return enterQueueTime;
	}
	
	/**
	 * Set the time this entered the queue.
	 */
	public void setEnterQueueTime(long enterQueueTime) {
		this.enterQueueTime = enterQueueTime;
	}

	/**
	 * Return the time this left the queue.
	 */
	public long getExitQueueTime() {
		return exitQueueTime;
	}
	
	/**
	 * Set the time this work left the queue.
	 */
	public void setExitQueueTime(long exitQueueTime) {
		this.exitQueueTime = exitQueueTime;
	}

	/**
	 * The same as getDescription().
	 */
	public String toString() {
		return getDescription();
	}

	/**
	 * Return a description of this work.
	 */
	public String getDescription() {
		
		StringBuffer sb = new StringBuffer();
        sb.append("Work[");
		if (runnable != null){
			sb.append(runnable.toString());
		}
        sb.append("]");
		return sb.toString();
	}

	private Runnable runnable;
	
	private long exitQueueTime;
	private long enterQueueTime;
	private long startTime;

}; 
