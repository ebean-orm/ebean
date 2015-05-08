package com.avaje.ebeaninternal.server.ddl;

import java.io.StringWriter;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbDdlSyntax;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.DbTypeMap;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;
import com.avaje.ebeaninternal.server.type.ScalarType;

/**
 * The context used during DDL generation.
 */
public class DdlGenContext {

	private final StringWriter stringWriter = new StringWriter();

	/**
	 * Used to map bean types to DB specific types.
	 */
	private final DbTypeMap dbTypeMap;

	/**
	 * Handles DB specific DDL syntax.
	 */
	private final DbDdlSyntax ddlSyntax;

	/**
	 * The new line character that is used.
	 */
	private final String newLine;

	/**
	 * Last content written (used with removeLast())
	 */
	private final List<String> contentBuffer = new ArrayList<String>();

	private Set<String> intersectionTables = new HashSet<String>();

	private List<String> intersectionTablesCreateDdl = new ArrayList<String>();
	private List<String> intersectionTablesFkDdl = new ArrayList<String>();

	private final DatabasePlatform dbPlatform;
	
	/** The Naming convention used to define FK an IX names */
	private final NamingConvention namingConvention;

	/** The global fk count used to keep FK names unique */
	private int fkCount;

	/** The ix count. */
	private int ixCount;

	public DdlGenContext(DatabasePlatform dbPlatform, NamingConvention namingConvention){
		this.dbPlatform = dbPlatform;
		this.dbTypeMap = dbPlatform.getDbTypeMap();
		this.ddlSyntax = dbPlatform.getDbDdlSyntax();
		this.newLine = ddlSyntax.getNewLine();
		this.namingConvention = namingConvention;
	}

	/**
	 * Return the dbPlatform.
	 */
	public DatabasePlatform getDbPlatform() {
		return dbPlatform;
	}

	public boolean isProcessIntersectionTable(String tableName){
		return intersectionTables.add(tableName);
	}

	public void addCreateIntersectionTable(String createTableDdl){
		intersectionTablesCreateDdl.add(createTableDdl);
	}

	public void addIntersectionTableFk(String intTableFk){
		intersectionTablesFkDdl.add(intTableFk);
	}

	public void addIntersectionCreateTables() {
		for (String intTableCreate : intersectionTablesCreateDdl) {
			write(newLine);
			write(intTableCreate);
		}
	}

	public void addIntersectionFkeys() {
		write(newLine);
		write(newLine);
		for (String intTableFk : intersectionTablesFkDdl) {
			write(newLine);
			write(intTableFk);
		}
	}

	/**
	 * Return the generated content (DDL script).
	 */
	public String getContent(){
		return stringWriter.toString();
	}

	/**
	 * Return the map used to determine the DB specific type
	 * for a given bean property.
	 */
	public DbTypeMap getDbTypeMap() {
		return dbTypeMap;
	}

	/**
	 * Return object to handle DB specific DDL syntax.
	 */
	public DbDdlSyntax getDdlSyntax() {
		return ddlSyntax;
	}

	public String getColumnDefn(BeanProperty p) {
		DbType dbType = getDbType(p);
		return p.renderDbType(dbType);
	}

	private DbType getDbType(BeanProperty p) {

		ScalarType<?> scalarType = p.getScalarType();
		if (scalarType == null) {
			throw new RuntimeException("No scalarType for " + p.getFullBeanName());
		}

		if (p.isDbEncrypted()){
		    return dbTypeMap.get(p.getDbEncryptedType());
		}
		
		int jdbcType = scalarType.getJdbcType();
		if (p.isLob() && jdbcType == Types.VARCHAR){
			// workaround for Postgres TEXT type which is 
			// VARCHAR in jdbc API but TEXT in ddl
			jdbcType = Types.CLOB;
		}
		return dbTypeMap.get(jdbcType);
	}
	/**
	 * Write content to the buffer.
	 */
	public DdlGenContext write(String content, int minWidth){

		content = pad(content, minWidth);

		contentBuffer.add(content);
		
		return this;

	}

	/**
	 * Write content to the buffer.
	 */
	public DdlGenContext write(String content){
		return write(content, 0);
	}

	public DdlGenContext writeNewLine() {
		write(newLine);
		return this;
	}

	/**
	 * Remove the last content that was written.
	 */
	public DdlGenContext removeLast() {
		if (!contentBuffer.isEmpty()){
			contentBuffer.remove(contentBuffer.size()-1);
		} else {
			throw new RuntimeException("No lastContent to remove?");
		}
		return this;
	}

	/**
	 * Flush the content to the buffer.
	 */
	public DdlGenContext flush() {
		if (!contentBuffer.isEmpty()){
			for (String s:contentBuffer){
				
				if (s != null){
					stringWriter.write(s);
				}
			}
			contentBuffer.clear();
		}
		return this;
	}

	private String padding(int length){

		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	public String pad(String content, int minWidth){
		if (minWidth > 0 && content.length() < minWidth){
			int padding = minWidth - content.length();
			return content + padding(padding);
		}
		return content;
	}

	/**
	 * @return the namingConvention
	 */
	public NamingConvention getNamingConvention() {
		return namingConvention;
	}

	/**
	 * @return the incremented fkCount
	 */
	public int incrementFkCount() {
		return ++fkCount;
	}

	/**
	 * @return the incremented ixCount
	 */
	public int incrementIxCount() {
		return ++ixCount;
	}

	/**
	 * Strips off the Database Platform specific quoted identifier characters.
	 */
  public String removeQuotes(String dbColumn) {
    
    dbColumn = StringHelper.replaceString(dbColumn, dbPlatform.getOpenQuote(), "");
    dbColumn = StringHelper.replaceString(dbColumn, dbPlatform.getCloseQuote(), "");
    
    return dbColumn;
  }
}
