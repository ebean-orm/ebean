/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.lib.sql;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Security mechanisim.
 */
public class Prefix {

	private static final Logger logger = Logger.getLogger(Prefix.class.getName());
	
	private static final int[] oa = { 50, 12, 4, 6, 8, 10, 7, 23, 45, 23, 6, 9, 12, 2, 8, 34 };

	public static String getProp(String prop) {
		String v = dec(prop);
		int p = v.indexOf(":");
		String r = v.substring(1, p);
		return r;
	}

	public static void main(String[] args) {
		String m = e(args[0]);
		logger.info("[" + m + "]");
		String o = getProp(m);
		logger.info("[" + o + "]");
	}

	public static String e(String msg) {
		msg = elen(msg, 40);
		return enc(msg);
	}

	public static byte az(byte c, int offset) {

		int z = c + offset;
		if (z > 122) {
			// dp("z> "+z);
			z = z - 122 + 48 - 1;
		}
		// dp("z="+z+" c:"+(int)c);
		return (byte) z;
	}

	public static byte bz(byte c, int offset) {
		int z = c - offset;
		if (z < (48)) {
			// dp("z< "+z);
			z = z + 122 - 48 + 1;
		}
		return (byte) z;
	}

	public static String enc(String msg) {
		byte[] msgbytes = msg.getBytes();
		byte[] encbytes = new byte[msgbytes.length + 1];
		Random r = new Random();
		int key = r.nextInt(70);

		char k = (char) (key + 48);

		encbytes[0] = az((byte) k, oa[0]);
		// dp("key:"+key+" encbytes[0]:"+(byte)encbytes[0]);
		int ios = key;
		for (int i = 1; i < (msgbytes.length + 1); i++) {
			encbytes[i] = az(msgbytes[i - 1], (oa[(i + ios) % oa.length]));
		}
		return new String(encbytes);
	}

	public static String dec(String msg) {
		byte[] msgbytes = msg.getBytes();
		byte[] encbytes = new byte[msgbytes.length];

		encbytes[0] = bz(msgbytes[0], oa[0]);
		byte key = encbytes[0];
		int ios = (key - 48);
		for (int i = 1; i < msgbytes.length; i++) {
			encbytes[i] = bz(msgbytes[i], oa[(i + ios) % oa.length]);
		}
		return new String(encbytes);
	}

	public static String elen(String msg, int len) {
		Random r = new Random();
		if (msg.length() < len) {
			int max = len - msg.length();
			StringBuilder sb = new StringBuilder();
			sb.append(msg).append(":");
			for (int i = 1; i < max; i++) {
				int bc = r.nextInt(122 - 48);
				sb.append(Character.toString((char) (bc + 48)));
			}
			return sb.toString();
		}
		return msg;
	}
}
