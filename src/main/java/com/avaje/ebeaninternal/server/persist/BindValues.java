package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;

/**
 * Holds a list of bind values for binding to a PreparedStatement.
 */
public class BindValues {

    int commentCount;

    final ArrayList<Value> list = new ArrayList<Value>();
    
    /**
     * Create with a Binder.
     */
    public BindValues(){
    }
    
    /**
     * Return the number of bind values.
     */
    public int size() {
        return list.size() - commentCount;
    }
    
    /**
     * Add a bind value with its JDBC datatype.
     * 
     * @param value the bind value
     * @param dbType the type as per java.sql.Types
     */
    public void add(Object value, int dbType, String name){
        list.add(new Value(value, dbType, name));
    }
    
    public void addComment(String comment){
    	++commentCount;
        list.add(new Value(comment));
    }
    
    /**
     * List of bind values.
     */
    public ArrayList<Value> values() {
        return list;
    }
    
    /**
     * A Value has additionally the JDBC data type.
     */
    public static class Value {
        
        private final Object value;
        
        private final int dbType;
               
        private final String name;

        private final boolean isComment;
        
        /**
         * Create a comment. This is so that comments can be put into
         * the bind log.
         */
        public Value(String comment) {
            this.name = comment;
            this.isComment = true;
            value = null;
            dbType = 0;
        }
        
        
        /**
         * Create the value.
         */
        public Value(Object value, int dbType, String name) {
        	this.isComment = false;
            this.value = value;
            this.dbType = dbType;
            this.name = name;
        }
        
        /**
         * This is a comment for the bind log and NOT an actual bind value.
         */
        public boolean isComment() {
        	return isComment;
        }
        
        /**
         * Return the type as per java.sql.Types.
         */
        public int getDbType() {
            return dbType;
        }
        
        /**
         * Return the value.
         */
        public Object getValue() {
            return value;
        }
        
        /**
         * Return the property name.
         */
        public String getName() {
        	return name;
        }
        
        public String toString(){
            return ""+value;
        }
    }
}
