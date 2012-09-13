package com.avaje.ebeaninternal.server.lib.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public abstract class AbstractResourceSource {


	/**
	 * Helper method to read String content.
	 */
	public String readString(ResourceContent content, int bufSize) throws IOException {

		if (content == null) {
			throw new NullPointerException("content is null?");
		}
		StringWriter writer = new StringWriter();

		InputStream is = content.getInputStream();
		InputStreamReader reader = new InputStreamReader(is);

		char[] buf = new char[bufSize];
		int len;
		while ((len = reader.read(buf, 0, buf.length)) != -1) {
			writer.write(buf, 0, len);
		}

		reader.close();

		return writer.toString();
	}

	/**
	 * Helper method to read byte[] content.
	 */
	public byte[] readBytes(ResourceContent content, int bufSize) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		InputStream is = content.getInputStream();

		byte[] buf = new byte[bufSize];
		int len;
		while ((len = is.read(buf, 0, buf.length)) != -1) {
			bytes.write(buf, 0, len);
		}

		is.close();

		return bytes.toByteArray();
	}
	
}
