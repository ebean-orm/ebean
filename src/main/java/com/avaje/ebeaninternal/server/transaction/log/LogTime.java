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
package com.avaje.ebeaninternal.server.transaction.log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Utility object used to in logging time.
 * <p>
 * Used in place of SimpleDateFormat which requires synchronization. Determines
 * if the day has changed and returns a format of the current time and day.
 * </p>
 */
public class LogTime {

	private static final String[] sep = { ":", "." };

	private static LogTime day;
	static {
	    day = new LogTime();
	}
	
	public static LogTime get() {
		return day;
	}

	public static LogTime nextDay() {
		LogTime d = new LogTime();
		day = d;
		return d;
	}

	public static LogTime getWithCheck() {
		LogTime d = day;
		if (d.isNextDay()) {
			return nextDay();
		} else {
			return d;
		}
	}

	private final String ymd;

	private final long startMidnight;

	private final long startTomorrow;

	/**
	 * Because every variable is private final the constructor should be thread
	 * safe (In JDK5+).
	 */
	private LogTime() {

		GregorianCalendar now = new GregorianCalendar();

		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		this.startMidnight = now.getTime().getTime();
		this.ymd = getDayDerived(now);
		
		now.add(Calendar.DATE, 1);
		this.startTomorrow = now.getTime().getTime();
	}

	/**
	 * Return true if we have moved into tomorrow. This is used to trigger log
	 * switching if required.
	 */
	public boolean isNextDay() {
		return (System.currentTimeMillis() >= startTomorrow);
	}

	/**
	 * Return the Year Month Day for today.
	 */
	public String getYMD() {
		return ymd;
	}

	/**
	 * Return the current time specify the separators.
	 * <p>
	 * The separators is a String[2] with the first string separating hours
	 * minutes and seconds and the second separating seconds from millis.
	 * </p>
	 * <p>
	 * The default is {":","."}
	 * </p>
	 */
	public String getNow(String[] separators) {

	    return getTimestamp(System.currentTimeMillis(), separators);

	}
	
    public String getTimestamp(long systime) {

        StringBuilder sb = new StringBuilder();
        getTime(sb, systime, startMidnight, sep);
        return sb.toString();
    }
    
    public String getTimestamp(long systime, String[] separators) {

        StringBuilder sb = new StringBuilder();
        getTime(sb, systime, startMidnight, separators);
        return sb.toString();
    }

	/**
	 * Returns the current time.
	 * <p>
	 * Format used is hours:minutes:seconds.millis
	 * </p>
	 */
	public String getNow() {
		return getNow(sep);
	}

	/**
	 * Set the derived day information.
	 */
	private String getDayDerived(Calendar now) {

		int nowyear = now.get(Calendar.YEAR);
		int nowmonth = now.get(Calendar.MONTH);
		int nowday = now.get(Calendar.DAY_OF_MONTH);

		nowmonth++;

		StringBuilder sb = new StringBuilder();

		format(sb, nowyear, 4);
		format(sb, nowmonth, 2);
		format(sb, nowday, 2);

		return sb.toString();
	}

	private void getTime(StringBuilder sb, long time, long midnight, String[] separator) {

		long rem = time - midnight;// startMidnight;

		long millis = rem % 1000;
		rem = rem / 1000;
		long secs = rem % 60;
		rem = rem / 60;
		long mins = rem % 60;
		rem = rem / 60;
		long hrs = rem;

		format(sb, hrs, 2);
		sb.append(separator[0]);
		format(sb, mins, 2);
		sb.append(separator[0]);
		format(sb, secs, 2);
		sb.append(separator[1]);
		format(sb, millis, 3);
	}

	private void format(StringBuilder sb, long value, int places) {
		String format = Long.toString(value);

		int pad = places - format.length();
		for (int i = 0; i < pad; i++) {
			sb.append("0");
		}
		sb.append(format);
	}

}
