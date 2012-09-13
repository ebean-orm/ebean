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

import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.FutureList;
import com.avaje.ebean.Page;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionTouched;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Page implementation based on limit offset types of queries.
 * 
 * @author rbygrave
 * 
 * @param <T>
 *            the entity bean type
 */
public class LimitOffsetPage<T> implements Page<T>, BeanCollectionTouched {

	private final int pageIndex;

	private final LimitOffsetPagingQuery<T> owner;

	private FutureList<T> futureList;

	public LimitOffsetPage(int pageIndex, LimitOffsetPagingQuery<T> owner) {
		this.pageIndex = pageIndex;
		this.owner = owner;
	}

	public FutureList<T> getFutureList() {

		if (futureList == null) {
			SpiQuery<T> originalQuery = owner.getSpiQuery();
			SpiQuery<T> copy = originalQuery.copy();
			copy.setPersistenceContext(originalQuery.getPersistenceContext());

			int pageSize = owner.getPageSize();
			copy.setFirstRow(pageIndex * pageSize);
			copy.setMaxRows(pageSize);
			copy.setBeanCollectionTouched(this);
			futureList = owner.getServer().findFutureList(copy, null);
		}

		return futureList;
	}

	/**
	 * Perform fetch ahead when the list is first accessed.
	 */
	public void notifyTouched(BeanCollection<?> c) {
		if (c.hasMoreRows()) {
			owner.fetchAheadIfRequired(pageIndex);
		}
	}

	public List<T> getList() {
		try {
			return getFutureList().get();
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean hasNext() {
		return ((BeanCollection<T>) getList()).hasMoreRows();
	}

	public boolean hasPrev() {
		return pageIndex > 0;
	}

	public Page<T> next() {
		return owner.getPage(pageIndex + 1);
	}

	public Page<T> prev() {
		return owner.getPage(pageIndex - 1);
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getTotalPageCount() {
		return owner.getTotalPageCount();
	}

	public int getTotalRowCount() {
		return owner.getTotalRowCount();
	}

	public String getDisplayXtoYofZ(String to, String of) {
		
		int first = pageIndex * owner.getPageSize() + 1;
		int last = first + getList().size() - 1;
		int total = getTotalRowCount();
		
		return first+to+last+of+total;
	}

	
	
}
