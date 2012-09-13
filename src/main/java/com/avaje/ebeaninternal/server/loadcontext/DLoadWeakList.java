/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.loadcontext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DLoadWeakList<T> implements DLoadList<T> {

	private static final Logger logger = Logger.getLogger(DLoadWeakList.class.getName());

	protected final ArrayList<WeakReference<T>> list = new ArrayList<WeakReference<T>>();

	protected int removedFromTop;

	protected DLoadWeakList() {

	}

	public int add(T e) {
		synchronized (this) {
			int i = list.size();
			list.add(new WeakReference<T>(e));
			return i;
		}
	}

	public void removeEntry(int position) {
		synchronized (this) {
			WeakReference<T> wref = list.get(position);
			if (wref == null) {
				logger.log(Level.WARNING, "removeEntry found no WeakReference for position[" + position + "]");
			} else {
				// just set the entry to null
				list.set(position, null);
				T object = wref.get();
				if (object == null) {
					logger.log(Level.WARNING, "removeEntry found no Object held by WeakReference for position[" + position + "]");
				}
			}
		    if (position == removedFromTop) {
		    	removedFromTop++;
		    }
		}
	}

	public List<T> getNextBatch(int batchSize) {
		if (removedFromTop >= list.size()){
			return new ArrayList<T>(0);
		}
		return getLoadBatch(removedFromTop, batchSize, true);
	}
	
	public List<T> getLoadBatch(int position, int batchSize) {
		return getLoadBatch(position, batchSize, false);
	}
	
	private List<T> getLoadBatch(int position, int batchSize, boolean ignoreMissing) {

		synchronized (this) {
			if (batchSize < 1) {
				throw new RuntimeException("batchSize " + batchSize + " < 1 ??!!");
			}

			ArrayList<T> batch = new ArrayList<T>();

			if (!addObjectToBatchAt(batch, position) && !ignoreMissing) {
				String msg = "getLoadBatch position[" + position + "] didn't find a bean in the list?";
				throw new IllegalStateException(msg);
			}
			
			for (int i = position; i < list.size(); i++) {
				addObjectToBatchAt(batch, i);
				if (batch.size() == batchSize) {
					// found enough beans going forward
					return batch;
				}
			}

			// search the front of the list to fill our batch
			for (int i = removedFromTop; i < position; i++) {
				addObjectToBatchAt(batch, i);
				if (batch.size() == batchSize) {
					// found enough beans going forward from start of list
					return batch;
				}
			}

			return batch;
		}
	}

	private boolean addObjectToBatchAt(ArrayList<T> batch, int i) {
		
		boolean found = false;
	    WeakReference<T> wref = list.get(i);
	    if (wref != null) {
	    	T object = wref.get();
	    	if (object == null) {
	    		logger.log(Level.WARNING, "Bean is null from weak reference");
	    	} else {
	    		found = true;
	    		batch.add(object);
	    	}
	    	// set it to null saying we have loaded this one
	    	list.set(i, null);
	    }
	    if (i == removedFromTop) {
	    	removedFromTop++;
	    }
	    return found;
    }

}
