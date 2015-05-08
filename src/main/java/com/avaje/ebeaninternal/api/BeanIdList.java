package com.avaje.ebeaninternal.api;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

/**
 * Wrapper of the list of Id's adding support for background fetching
 * future object. 
 * 
 * @author rbygrave
 */
public class BeanIdList {

	private final List<Object> idList;
	
	private boolean hasMore = true;
	
	private FutureTask<Integer> fetchFuture;
	
	public BeanIdList(List<Object> idList) {
		this.idList = idList;
	}
	
	/**
	 * Return true if the fetch is continuing in a background thread.
	 */
	public boolean isFetchingInBackground() {
		return fetchFuture != null;
	}

	/**
	 * Set the FutureTask that is continuing the fetch in a background thread.
	 */
	public void setBackgroundFetch(FutureTask<Integer> fetchFuture) {
		this.fetchFuture = fetchFuture;
	}

	/**
	 * Wait for the background fetching to complete with a timeout.
	 */
	public void backgroundFetchWait(long wait, TimeUnit timeUnit) {
		if (fetchFuture != null){
			try {
				fetchFuture.get(wait, timeUnit);
			} catch (Exception e) {
				throw new PersistenceException(e);
			} 		
		}
	}
	
	/**
	 * Wait for the background fetching to complete.
	 */
	public void backgroundFetchWait() {
		if (fetchFuture != null){
			try {
				fetchFuture.get();
			} catch (Exception e) {
				throw new PersistenceException(e);
			} 		
		}
	}

	/**
	 * Add an Id to the list.
	 */
	public void add(Object id){
		idList.add(id);
	}

	/**
	 * Return the list of Id's.
	 */
	public List<Object> getIdList() {
		return idList;
	}

	/**
	 * Return true if max rows was hit and there is more rows to fetch.
	 */
	public boolean isHasMore() {
		return hasMore;
	}

	/**
	 * Set to true when max rows is hit and there are more rows to fetch.
	 */
	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
	
}
