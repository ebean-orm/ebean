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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A base object for query Future objects.
 * 
 * @author rbygrave
 *
 * @param <T> the entity bean type
 */
public abstract class BaseFuture<T> implements Future<T> {
	
	private final FutureTask<T> futureTask;
	
	public BaseFuture(FutureTask<T> futureTask) {
		this.futureTask = futureTask;
	}
	
	public boolean cancel(boolean mayInterruptIfRunning) {
		return futureTask.cancel(mayInterruptIfRunning);
	}

	public T get() throws InterruptedException, ExecutionException {
		return futureTask.get();
	}

	public T get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		
		return futureTask.get(timeout, unit);
	}

	public boolean isCancelled() {
		return futureTask.isCancelled();
	}

	public boolean isDone() {
		return futureTask.isDone();
	}

	
}
