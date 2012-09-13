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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonValueAdapter;


/**
 * JPA standard based Enum scalar type.
 * <p>
 * Converts between bean Enum types to database string or integer columns.
 * </p>
 * <p>
 * The limitation of this class is that it converts the Enum to either the ordinal
 * or string value of the Enum. If you wish to convert the Enum to some other value
 * then you should look at the Ebean specific @EnumMapping.
 * </p>
 */
public class ScalarTypeEnumStandard {
	
	@SuppressWarnings({"rawtypes","unchecked"})
    public static class StringEnum extends EnumBase implements ScalarTypeEnum {
		
		private final int length;
		
		/**
		 * Create a ScalarTypeEnum.
		 */
		public StringEnum(Class enumType) {
			super(enumType, false, Types.VARCHAR);
			this.length = maxValueLength(enumType);
		}

		/**
		 * Return the IN values for DB constraint construction.
		 */
		public String getContraintInValues(){

			StringBuilder sb = new StringBuilder();
			
			sb.append("(");
			Object[] ea = enumType.getEnumConstants();
			for (int i = 0; i < ea.length; i++) {
				Enum<?> e = (Enum<?>)ea[i];
				if (i > 0){
					sb.append(",");
				}
				sb.append("'").append(e.toString()).append("'");
			}
			sb.append(")");
			
			return sb.toString();
		}
		
		private int maxValueLength(Class<?> enumType){
			
			int maxLen = 0;
			
			Object[] ea = enumType.getEnumConstants();
			for (int i = 0; i < ea.length; i++) {
				Enum<?> e = (Enum<?>)ea[i];
				maxLen = Math.max(maxLen, e.toString().length());
			}
			
			return maxLen;
		}
		
		public int getLength() {
			return length;
		}

		public void bind(DataBind b, Object value) throws SQLException {
			if (value == null){
				b.setNull(Types.VARCHAR);
			} else {
				b.setString(value.toString());
			}
		}
	
		public Object read(DataReader dataReader) throws SQLException {
			
			String string = dataReader.getString();
			if (string == null){
				return null;
			} else {
				return Enum.valueOf(enumType, string);
			}
		}
		
		/**
		 * Convert the Boolean value to the db value.
		 */
		public Object toJdbcType(Object beanValue) {
			if (beanValue == null) {
				return null;
			}
			Enum<?> e = (Enum<?>)beanValue;
			return e.toString();
		}
	
		public Object toBeanType(Object dbValue) {
			if (dbValue == null) {
				return null;
			}
			
			return Enum.valueOf(enumType, (String)dbValue);
		}

	}
	
	@SuppressWarnings({"rawtypes","unchecked"})
    public static class OrdinalEnum extends EnumBase implements ScalarTypeEnum {

		private final Object[] enumArray;
				
		/**
		 * Create a ScalarTypeEnum.
		 */
		public OrdinalEnum(Class enumType) {
			super(enumType, false, Types.INTEGER);
			this.enumArray = EnumSet.allOf(enumType).toArray();
		}

		/**
		 * Return the IN values for DB constraint construction.
		 */
		public String getContraintInValues(){

			StringBuilder sb = new StringBuilder();
			
			sb.append("(");
			for (int i = 0; i < enumArray.length; i++) {
				Enum<?> e = (Enum<?>)enumArray[i];
				if (i > 0){
					sb.append(",");
				}
				sb.append(e.ordinal());
			}
			sb.append(")");
			
			return sb.toString();
		}

		
		public void bind(DataBind b, Object value) throws SQLException {
			if (value == null){
				b.setNull(Types.INTEGER);
			} else {
				Enum<?> e = (Enum<?>)value;
				b.setInt(e.ordinal());
			}
		}
	      
		public Object read(DataReader dataReader) throws SQLException {

			Integer ordinal = dataReader.getInt();
			if (ordinal == null){
				return null;
			} else {
				if (ordinal < 0 || ordinal >= enumArray.length){
					String m = "Unexpected ordinal ["+ordinal+"] out of range ["+enumArray.length+"]";
					throw new IllegalStateException(m);
				}
				return enumArray[ordinal];
			}
		}
		
		/**
		 * Convert the Boolean value to the db value.
		 */
		public Object toJdbcType(Object beanValue) {
			if (beanValue == null) {
				return null;
			}
			Enum e = (Enum)beanValue;
			return e.ordinal();
		}
	
		/**
		 * Convert the db value to the Boolean value.
		 */
		public Object toBeanType(Object dbValue) {
			if (dbValue == null) {
				return null;
			}
			
			int ordinal = ((Integer)dbValue).intValue();
			if (ordinal < 0 || ordinal >= enumArray.length){
				String m = "Unexpected ordinal ["+ordinal+"] out of range ["+enumArray.length+"]";
				throw new IllegalStateException(m);
			}
			return enumArray[ordinal];
		}

	}
	
    @SuppressWarnings({"rawtypes","unchecked"})
    public abstract static class EnumBase extends ScalarTypeBase {

        protected final Class enumType;
        
        public EnumBase(Class<?> type, boolean jdbcNative, int jdbcType) {
            super(type, jdbcNative, jdbcType);
            this.enumType = type;
        }
        
        public String format(Object t) {
            return t.toString();
        }

        public String formatValue(Object t) {
            return t.toString();
        }
        
        public Object parse(String value) { 
            return Enum.valueOf(enumType, value);
        }

        public Object parseDateTime(long systemTimeMillis) {
            throw new TextException("Not Supported");
        }

        public boolean isDateTimeCapable() {
            return false;
        }
                
        @Override
        public Object jsonFromString(String value, JsonValueAdapter ctx) {
            return parse(value);
        }

        @Override
        public String jsonToString(Object value, JsonValueAdapter ctx) {
            return EscapeJson.escapeQuote(value.toString());
        }

        public Object readData(DataInput dataInput) throws IOException {
            if (!dataInput.readBoolean()) {
                return null;
            } else {
                String s = dataInput.readUTF();
                return parse(s);
            }
        }

        public void writeData(DataOutput dataOutput, Object v) throws IOException {
            if (v == null){
                dataOutput.writeBoolean(false);
            } else {
                dataOutput.writeBoolean(true);
                dataOutput.writeUTF(format(v));
            }
        }
        
    }
}
