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
package com.avaje.ebean.text.json;

import java.sql.Timestamp;

/**
 * Allows you to customise the Date and Timestamp formats.
 * <p>
 * There is not a standard JSON format for Date or Timestamp types. By default
 * Ebean uses ISO8601 "yyyy-MM-dd'T'HH:mm:ss.SSSZ" and "yyyy-MM-dd".
 * </p>
 * <p>
 * Note that Ebean will convert Joda types to either of the Date or Timestamp
 * types and back for you.
 * </p>
 * 
 * @see JsonReadOptions
 * 
 * @author rbygrave
 */
public interface JsonValueAdapter {

  /**
   * Convert the Date to json string.
   */
  public String jsonFromDate(java.sql.Date date);

  /**
   * Convert the DateTime to json string.
   */
  public String jsonFromTimestamp(java.sql.Timestamp date);

  /**
   * Parse the JSON string into a Date.
   */
  public java.sql.Date jsonToDate(String jsonDate);

  /**
   * Parse the JSON DateTime into a Timestamp.
   */
  public Timestamp jsonToTimestamp(String jsonDateTime);

}
