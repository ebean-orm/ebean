package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.HashSet;

/**
 * Keywords deemed inappropriate for use as table alias' column or table name.
 * <p>
 * That is, they might cause sql syntax errors if used.
 * </p>
 */
public class SqlReservedWords {

	private static final String baseKeyWords = 
		"ALIAS,ALTER,ADD,ALL,ARE,AND,ANY,ARRAY"
		+",AS,ASC,AT"
		+",AVG,BEGIN,BETWEEN,BIGINT,BINARY,BIT,BIT_LENGTH,BLOB,BOOLEAN"
		+",BOTH,BY,CALL,CALLED,CASCADE,CASCADED,CASE,CAST,CATALOG,CHAR"
		+",CHARACTER,CHECK,CLOB,CLOSE,COALESCE,COLLATE"
		+",COLLATION,COLUMN,COMMIT,CONDITION,CONNECT,CONNECTION,CONSTRAINT"
		+",CONSTRAINTS,CONSTRUCTOR,CONTAINS,CONTINUE,CONVERT"
		+",COUNT,CREATE,CROSS,CURRENT_DATE"
		+",CURRENT_PATH,CURRENT_ROLE,CURRENT_TIME,CURRENT_TIMESTAMP"
		+",CURRENT_USER,CURSOR,DATE"
		+",DAY,DEC,DECIMAL,DECLARE,DEFAULT,DELETE"
		+",DESC"
		+",DISTINCT,DO,DOUBLE,DROP,ELSE,ELSEIF,END,EQUALS"
		+",EXEC,EXIT,EXISTS,EXTRACT"
		+",FLOAT,FROM,FOR,FREE"
		+",GET,GLOBAL,GO,GOTO,GRANT,GROUP,HAVING,HOUR"
		+",IF,IN,INNER,INOUT,INSERT"
		+",INT,INTEGER,INTO,IS,JOIN"
		+",LAST,LIKE,LIMIT"
		+",MAX,MIN"
		+",NCHAR,NCLOB,NOT"
		+",NULL,NULLIF,NUMERIC,"
		+",OR,ORDER,OUTER"
		+",REAL,REF,REFERENCES"
		+",RETURN,RETURNS"
		+",SELECT,SSL"
		+",SMALLINT,"
		+",SYSTEM,SYSTEM_USER,TABLE"
		+",TO"
		+",TRIGGER,UNION,UNIQUE"
		+",UPDATE,USER,VARCHAR,VIEW,WHEN"
		+",WHERE,WITH";

	
	private static HashSet<String> keywords = new HashSet<String>();
	static {
		
		String[] initialKeywords = baseKeyWords.split(",");
		for (int i = 0; i < initialKeywords.length; i++) {
			keywords.add(initialKeywords[i].trim());
		}
		
	}


	/**
	 * Return true if the tableAlias is a keyword.
	 */
	public static synchronized boolean isKeyword(String keyword){
		String s = keyword.trim().toUpperCase();
		return keywords.contains(s);
	}
	
	/**
	 * Add a sql keyword to the known set.
	 */
	public static synchronized void addKeyword(String keyword){
		if (keyword != null){
			keyword = keyword.trim().toUpperCase();
			if (keyword.length() > 0){
				keywords.add(keyword);				
			}
		}
	}
}
