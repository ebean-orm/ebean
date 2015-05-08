package com.avaje.ebeaninternal.server.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.UUID;

import com.avaje.ebeaninternal.server.type.ScalarTypeUUIDBinary;

/**
 * Default implementation of TypeConverter.
 * <p>
 * Converts objects to the required type if required.
 * </p>
 */
public final class BasicTypeConverter implements Serializable {

	private static final long serialVersionUID = 7691463236204070311L;
	
	/**
	 * Type code for java.util.Calendar.
	 */
	public static final int UTIL_CALENDAR = -999998986;

	/**
	 * Type code for java.util.Date.
	 */
	public static final int UTIL_DATE = -999998988;

	/**
	 * Type code for java.math.BigInteger.
	 */
	public static final int MATH_BIGINTEGER = -999998987;

	/**
	 * Type code for an Enum type.
	 */
	public static final int ENUM = -999998989;
	
	private BasicTypeConverter() {
	}

	/**
	 * Convert the Object to the required data type.
	 * 
	 * @param value
	 *            the Object value
	 * @param toDataType
	 *            the dataType as per java.sql.Types.
	 */
	public static Object convert(Object value, int toDataType) {

		try {
			switch (toDataType) {
				case UTIL_DATE: {
					return toUtilDate(value);
				}
				case UTIL_CALENDAR: {
					return toCalendar(value);
				}
				case Types.BIGINT: {
					return toLong(value);
				}
				case Types.INTEGER: {
					return toInteger(value);
				}
				case Types.BIT: {
					return toBoolean(value);
				}
				case Types.TINYINT: {
					return toByte(value);
				}
				case Types.SMALLINT: {
					return toShort(value);
				}
				case Types.NUMERIC: {
					return toBigDecimal(value);
				}
				case Types.DECIMAL: {
					return toBigDecimal(value);
				}
				case Types.REAL: {
					return toFloat(value);
				}
				case Types.DOUBLE: {
					return toDouble(value);
				}
				case Types.FLOAT: {
					return toDouble(value);
				}
				case Types.BOOLEAN: {
					return toBoolean(value);
				}
				case Types.TIMESTAMP: {
					return toTimestamp(value);
				}
				case Types.DATE: {
					return toDate(value);
				}
				case Types.VARCHAR: {
					return toString(value);
				}
				case Types.CHAR: {
					return toString(value);
				}
				case Types.OTHER: {
					return value;
				}
				case Types.JAVA_OBJECT: {
					return value;
				}
				case Types.BINARY:
				case Types.LONGVARBINARY:
				case Types.BLOB: {
					return value;
				}
				case Types.LONGVARCHAR:
				case Types.CLOB: {
					return value;
				}
				default: {
					String msg = "Unhandled data type [" + toDataType + "] converting [" + value + "]";
					throw new RuntimeException(msg);
				}
			}
		} catch (ClassCastException e) {
			String m = "ClassCastException converting to data type [" + toDataType + "] value [" + value + "]";
			throw new RuntimeException(m);
		}
	}

	/**
	 * Convert the value to a String.
	 */
	public static String toString(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof char[]) {
			return String.valueOf((char[]) value);
		}

		return value.toString();
	}

	
    /**
     * Convert the value to a Boolean with an explicit String true value.
     */
    public static Boolean toBoolean(Object value, String dbTrueValue) {

        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String s = value.toString();
        return s.equalsIgnoreCase(dbTrueValue);
	}
    
	/**
	 * Convert the value to a Boolean. Can be a Boolean or the string values
	 * "true" or "false".
	 */
	public static Boolean toBoolean(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		return Boolean.valueOf(value.toString());
	}

	/**
	 * Convert the value to a UUID.
	 */
	public static UUID toUUID(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return UUID.fromString((String) value);
		}
		if (value instanceof byte[]) {
		  return ScalarTypeUUIDBinary.convertFromBytes((byte[])value);
		}
		return (UUID) value;
	}

	/**
	 * convert the passed in object to a BigDecimal. It should be another
	 * numeric type.
	 */
	public static BigDecimal toBigDecimal(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		return new BigDecimal(value.toString());
	}

	public static Float toFloat(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Float) {
			return (Float) value;
		}
		if (value instanceof Number) {
			return Float.valueOf(((Number) value).floatValue());
		}
		return Float.valueOf(value.toString());
	}

	public static Short toShort(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Short) {
			return (Short) value;
		}
		if (value instanceof Number) {
			return Short.valueOf(((Number) value).shortValue());
		}
		return Short.valueOf(value.toString());
	}

	public static Byte toByte(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Byte) {
			return (Byte) value;
		}
		return Byte.valueOf(value.toString());
	}

	/**
	 * convert the passed in object to a Integer. It should be another numeric
	 * type.
	 */
	public static Integer toInteger(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return Integer.valueOf(((Number) value).intValue());
		}
		return Integer.valueOf(value.toString());
	}

	/**
	 * Convert the object to a Long. It should be another numeric type.
	 */
	public static Long toLong(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Long) {
			return (Long) value;
		}
		if (value instanceof String) {
			return Long.valueOf((String) value);
		}
		if (value instanceof Number) {
			return Long.valueOf(((Number) value).longValue());
		}
		if (value instanceof java.util.Date) {
			return Long.valueOf(((java.util.Date) value).getTime());
		}
		if (value instanceof Calendar) {
			return Long.valueOf(((Calendar) value).getTime().getTime());
		}
		return Long.valueOf(value.toString());
	}

	public static BigInteger toMathBigInteger(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof BigInteger) {
			return (BigInteger) value;
		}
		return new BigInteger(value.toString());
	}

	/**
	 * Convert the object to a Double. It should be another numberic type.
	 */
	public static Double toDouble(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Double) {
			return (Double) value;
		}
		if (value instanceof Number) {
			return Double.valueOf(((Number) value).doubleValue());
		}
		return Double.valueOf(value.toString());
	}

	/**
	 * convert the passed in object to a Timestamp. It is expected to be a
	 * java.sql.Date really.
	 */
	public static Timestamp toTimestamp(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Timestamp) {
			return (Timestamp) value;

		} else if (value instanceof java.util.Date) {
			// no nanos here... so hopefully ok
			return new Timestamp(((java.util.Date) value).getTime());

		} else if (value instanceof Calendar) {
			return new Timestamp(((Calendar) value).getTime().getTime());

		} else if (value instanceof String) {
			return Timestamp.valueOf((String) value);

		} else if (value instanceof Number) {
			return new Timestamp(((Number) value).longValue());

		} else {
			String msg = "Unable to convert [" + value.getClass().getName() + "] into a Timestamp.";
			throw new RuntimeException(msg);
		}
	}

	public static java.sql.Time toTime(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof java.sql.Time) {
			return (java.sql.Time) value;

		} else if (value instanceof String) {
			return java.sql.Time.valueOf((String) value);

		} else {
			String m = "Unable to convert [" + value.getClass().getName() + "] into a java.sql.Date.";
			throw new RuntimeException(m);
		}
	}

	/**
	 * convert the passed in object to a java sql Date.
	 */
	public static java.sql.Date toDate(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof java.sql.Date) {
			return (java.sql.Date) value;

		} else if (value instanceof java.util.Date) {
			return new java.sql.Date(((java.util.Date) value).getTime());

		} else if (value instanceof Calendar) {
			return new java.sql.Date(((Calendar) value).getTime().getTime());

		} else if (value instanceof String) {
			return java.sql.Date.valueOf((String) value);

		} else if (value instanceof Number) {
			return new java.sql.Date(((Number) value).longValue());

		} else {
			String m = "Unable to convert [" + value.getClass().getName() + "] into a java.sql.Date.";
			throw new RuntimeException(m);
		}
	}

	/**
	 * convert the passed in object to a java sql Date.
	 */
	public static java.util.Date toUtilDate(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof java.sql.Timestamp) {
			// loss of nanos precision
			return new java.util.Date(((java.sql.Timestamp) value).getTime());
		}
		// DEVNOTE: strictly speaking do I need to convert a java.sql.Date to
		// java.util.Date? equals() is symmetrical so perhaps this is not
		// really required?
		if (value instanceof java.sql.Date) {
			return new java.util.Date(((java.sql.Date) value).getTime());
		}
		if (value instanceof java.util.Date) {
			return (java.util.Date) value;

		} else if (value instanceof Calendar) {
			return ((Calendar) value).getTime();

		} else if (value instanceof String) {
			return new java.util.Date(Timestamp.valueOf((String) value).getTime());

		} else if (value instanceof Number) {
			return new java.util.Date(((Number) value).longValue());

		} else {
			throw new RuntimeException("Unable to convert [" + value.getClass().getName() + "] into a java.util.Date");
		}
	}

	/**
	 * convert the passed in object to a java sql Date.
	 */
	public static Calendar toCalendar(Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Calendar) {
			return (Calendar) value;

		} else if (value instanceof java.util.Date) {
			java.util.Date date = ((java.util.Date) value);
			return toCalendarFromDate(date);

		} else if (value instanceof String) {
			java.util.Date date = toUtilDate(value);
			return toCalendarFromDate(date);

		} else if (value instanceof Number) {
			long timeMillis = ((Number) value).longValue();
			java.util.Date date = new java.util.Date(timeMillis);
			return toCalendarFromDate(date);

		} else {
			String m = "Unable to convert [" + value.getClass().getName() + "] into a java.util.Date";
			throw new RuntimeException(m);
		}
	}

	private static Calendar toCalendarFromDate(java.util.Date date) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

}
