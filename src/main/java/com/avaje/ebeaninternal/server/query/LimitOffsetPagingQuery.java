/**
 * Copyright (C) 2009  Robin Bygrave
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
package com.avaje.ebeaninternal.server.query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.persistence.PersistenceException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Page;
import com.avaje.ebean.PagingList;
import com.avaje.ebeaninternal.api.Monitor;
import com.avaje.ebeaninternal.api.SpiQuery;

public class LimitOffsetPagingQuery<T> implements PagingList<T> {

	private transient EbeanServer server;
	
	private final SpiQuery<T> query;
	
	private final List<LimitOffsetPage<T>> pages = new ArrayList<LimitOffsetPage<T>>();
	
	private final Monitor monitor = new Monitor();
	
	private final int pageSize;

	private boolean fetchAhead = true;
	
	private Future<Integer> futureRowCount;
	
	public LimitOffsetPagingQuery(EbeanServer server, SpiQuery<T> query, int pageSize) {
		this.query = query;
		this.pageSize = pageSize;
		this.server = server;
	}
	
	public EbeanServer getServer() {
		return server;
	}

	public void setServer(EbeanServer server) {
		this.server = server;
	}

	public SpiQuery<T> getSpiQuery() {
		return query;
	}
	
	public PagingList<T> setFetchAhead(boolean fetchAhead) {
		this.fetchAhead = fetchAhead;
		return this;
	}
	
	public List<T> getAsList() {
		return new LimitOffsetList<T>(this);
	}

	public Future<Integer> getFutureRowCount() {
		synchronized (monitor) {
			if (futureRowCount == null){
				futureRowCount = server.findFutureRowCount(query, null);
			}
			return futureRowCount;
		}
	}

	private LimitOffsetPage<T> internalGetPage(int i){
		synchronized (monitor) {
			int ps = pages.size();
			if (ps <= i){
				for (int j = ps; j <= i; j++) {
					LimitOffsetPage<T> p = new LimitOffsetPage<T>(j, this);
					pages.add(p);
				}
			} 
			return pages.get(i);
		}
	}
	
	protected void fetchAheadIfRequired(int pageIndex){
		synchronized (monitor) {
			// Already checked in LimitOffsetPage that there is another page
			if (fetchAhead){
				// fetchAhead is turned on so get the next page and trigger query
				LimitOffsetPage<T> nextPage = internalGetPage(pageIndex + 1);
				nextPage.getFutureList();
			}
		}
	}
	
	public void refresh() {
		synchronized (monitor) {
			futureRowCount = null;
			pages.clear();
		}
	}

	public Page<T> getPage(int i) {
		return internalGetPage(i);
	}

	protected boolean hasNext(int position){
		return position < getTotalRowCount();
	}
	
	protected T get(int rowIndex){
		int pg = rowIndex / pageSize;
		int offset = rowIndex % pageSize;
		
		Page<T> page = getPage(pg);
		return page.getList().get(offset);
	}
	
	public int getTotalPageCount() {
		
		int rowCount = getTotalRowCount();
		if (rowCount == 0){
			return 0;
		} else {
			return ((rowCount-1) / pageSize) + 1;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTotalRowCount() {
		try {
			return getFutureRowCount().get();
		} catch (Exception e) {
			throw new PersistenceException(e);
		} 
	}

	
}
