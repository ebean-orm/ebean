package com.avaje.ebeaninternal.server.loadcontext;

import java.util.List;

public interface DLoadList<T> {

	int add(T e);

	/**
	 * Return the next batch of entries from the top.
	 */
	List<T> getNextBatch(int batchSize);

	void removeEntry(int position);

	/**
	 * Return the batch of entries based on the position and batch size.
	 */
	List<T> getLoadBatch(int position, int batchSize);

}