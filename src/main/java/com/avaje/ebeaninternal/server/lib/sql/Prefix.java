package com.avaje.ebeaninternal.server.lib.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Security mechanisim.
 */
public class Prefix {

	private static final Logger logger = LoggerFactory.getLogger(Prefix.class);
	
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
