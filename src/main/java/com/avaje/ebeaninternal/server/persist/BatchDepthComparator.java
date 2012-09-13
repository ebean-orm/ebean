/**
 * Copyright (C) 2006  Robin Bygrave
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
package com.avaje.ebeaninternal.server.persist;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Used to sort BatchedBeanHolder by their depth.
 * <p>
 * Beans are queued and put into BatchedBeanHolder along with their depth. This
 * delays the actually binding to PreparedStatements until the
 * BatchedBeanHolder's are flushed. This is so that we can get the generated
 * keys from inserts. These values are required to persist the 'detail' beans.
 * </p>
 */
public class BatchDepthComparator implements Comparator<BatchedBeanHolder>, Serializable {

  private static final long serialVersionUID = 264611821665757991L;

  public int compare(BatchedBeanHolder b1, BatchedBeanHolder b2) {

    if (b1.getOrder() < b2.getOrder()) {
      return -1;
    }
    if (b1.getOrder() == b2.getOrder()) {
      return 0;
    }
    return 1;
  }

}
