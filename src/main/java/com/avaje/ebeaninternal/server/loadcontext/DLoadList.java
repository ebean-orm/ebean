package com.avaje.ebeaninternal.server.loadcontext;

import java.util.List;

public interface DLoadList<T> {

	public int add(T e);

	/**
	 * Return the next batch of entries from the top.
	 */
	public abstract List<T> getNextBatch(int batchSize);

	public void removeEntry(int position);

	/**
	 * Return the batch of entries based on the position and batch size.
	 */
	public List<T> getLoadBatch(int position, int batchSize);

}