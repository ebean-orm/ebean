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
package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.Date.
 */
public class ScalarTypeUtilDate {

	public static class TimestampType extends ScalarTypeBaseDateTime<java.util.Date> {

		public TimestampType() {
			super(java.util.Date.class, false, Types.TIMESTAMP);
		}

		public java.util.Date read(DataReader dataReader) throws SQLException {
			Timestamp timestamp = dataReader.getTimestamp();
			if (timestamp == null) {
				return null;
			} else {
				return new java.util.Date(timestamp.getTime());
			}
		}

		public void bind(DataBind b, java.util.Date value)
				throws SQLException {
			if (value == null) {
				b.setNull(Types.TIMESTAMP);
			} else {

				Timestamp timestamp = new Timestamp(value.getTime());
				b.setTimestamp(timestamp);
			}
		}

		public Object toJdbcType(Object value) {
			return BasicTypeConverter.toTimestamp(value);
		}

		public java.util.Date toBeanType(Object value) {
			return BasicTypeConverter.toUtilDate(value);
		}

		
		
		@Override
        public Date convertFromTimestamp(Timestamp ts) {
		    return new java.util.Date(ts.getTime());
        }

        @Override
        public Timestamp convertToTimestamp(Date t) {
            return new Timestamp(t.getTime());
        }

		public java.util.Date parseDateTime(long systemTimeMillis) {
			return new java.util.Date(systemTimeMillis);
		}

        public Object luceneFromIndexValue(Object value) {
            Long l = (Long)value;
            return new java.util.Date(l);
        }

        public Object luceneToIndexValue(Object value) {
            return ((java.util.Date)value).getTime();
        }		
	}

	public static class DateType extends ScalarTypeBaseDate<java.util.Date> {

		public DateType() {
			super(Date.class, false, Types.DATE);
		}

		@Override
        public Date convertFromDate(java.sql.Date ts) {
            return new java.util.Date(ts.getTime());
        }

        @Override
        public java.sql.Date convertToDate(Date t) {
            return new java.sql.Date(t.getTime());
        }

		public Object toJdbcType(Object value) {
			return BasicTypeConverter.toDate(value);
		}

		public java.util.Date toBeanType(Object value) {
			return BasicTypeConverter.toUtilDate(value);
		}
	}
}
