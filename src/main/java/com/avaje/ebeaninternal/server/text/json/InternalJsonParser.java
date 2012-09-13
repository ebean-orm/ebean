package com.avaje.ebeaninternal.server.text.json;

import java.io.Reader;

import com.avaje.ebean.text.json.JsonElement;

public class InternalJsonParser {

	public static JsonElement parse(String s) {
		
		ReadJsonSourceString src = new ReadJsonSourceString(s);
		ReadBasicJsonContext b = new ReadBasicJsonContext(src);
		return ReadJsonRawReader.readJsonElement(b);
	}

	public static JsonElement parse(Reader s) {
		
		ReadJsonSourceReader src = new ReadJsonSourceReader(s, 512, 256);
		ReadBasicJsonContext b = new ReadBasicJsonContext(src);
		return ReadJsonRawReader.readJsonElement(b);
	}

}
