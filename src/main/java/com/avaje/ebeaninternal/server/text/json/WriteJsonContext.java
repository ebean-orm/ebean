/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.text.json;

import java.util.Map;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebean.text.json.JsonWriteBeanVisitor;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.ebean.text.json.JsonWriter;
import com.avaje.ebeaninternal.server.type.EscapeJson;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.util.ArrayStack;


public class WriteJsonContext implements JsonWriter {

    private final WriteJsonBuffer buffer;

    private final boolean pretty;

    private final JsonValueAdapter valueAdapter;
    
    private final ArrayStack<Object> parentBeans = new ArrayStack<Object>();
    
    private final PathProperties pathProperties;

    private final Map<String, JsonWriteBeanVisitor<?>> visitorMap;

    private final String callback;
    
    private final PathStack pathStack;
    
    private WriteBeanState beanState;
    
    private int depthOffset;
    
    boolean assocOne;

    public WriteJsonContext(WriteJsonBuffer buffer, boolean pretty, JsonValueAdapter dfltValueAdapter, 
            JsonWriteOptions options, String requestCallback){
        
        this.buffer = buffer;
        this.pretty = pretty;
        this.pathStack = new PathStack();
        this.callback = getCallback(requestCallback, options);
        if (options == null){            
            this.valueAdapter = dfltValueAdapter;
            this.visitorMap = null;
            this.pathProperties = null;

        } else {
            this.valueAdapter = getValueAdapter(dfltValueAdapter, options.getValueAdapter());
            this.visitorMap = emptyToNull(options.getVisitorMap());
            this.pathProperties = emptyToNull(options.getPathProperties());
        }
        
        if (callback != null){
            buffer.append(requestCallback).append("(");
        }
    }
    
    public void appendRawValue(String key, String rawJsonValue) {
    	appendKeyWithComma(key, true);
    	buffer.append(rawJsonValue);
    }
    
    public void appendQuoteEscapeValue(String key, String valueToEscape) {
    	appendKeyWithComma(key, true);
    	EscapeJson.escapeQuote(valueToEscape, buffer);
    }
	
	public void end() {
        if (callback != null){
            buffer.append(")");
        }        
    }
  
    private <MK,MV> Map<MK,MV> emptyToNull(Map<MK,MV> m){
        if ( m == null || m.isEmpty()) {
            return null;
        } else {
            return m;
        }
    }
    
    private PathProperties emptyToNull(PathProperties m){
        if ( m == null || m.isEmpty()) {
            return null;
        } else {
            return m;
        }
    }
    
    private String getCallback(String requestCallback, JsonWriteOptions options) {
        if (requestCallback != null){
            return requestCallback;
        }
        if (options != null){
            return options.getCallback();
        }
        return null;
    }

    private JsonValueAdapter getValueAdapter(JsonValueAdapter dfltValueAdapter, JsonValueAdapter valueAdapter) {
        return valueAdapter == null ? dfltValueAdapter : valueAdapter;
    }
    
    /**
     * Return the set of properties to write to JSON. If null is returned then
     * the default will output the properties loaded for this bean.
     */
    public Set<String> getIncludeProperties() {
        if (pathProperties != null){
            String path = pathStack.peekWithNull();
            return pathProperties.get(path);
        }
        return null;
    }

    public JsonWriteBeanVisitor<?> getBeanVisitor() {
        if (visitorMap != null){
            String path = pathStack.peekWithNull();
            return visitorMap.get(path);
        }
        return null;
    }
    
    public String getJson() {
        return buffer.toString();
    }    

    private void appendIndent(){
        
        buffer.append("\n");
        int depth = depthOffset + parentBeans.size();
        for (int i = 0; i < depth; i++) {
            buffer.append("    ");
        }    
    }
    
    public void appendObjectBegin(){
        if (pretty && !assocOne){
            appendIndent();
        }
        buffer.append("{");
    }
    public void appendObjectEnd(){
        buffer.append("}");
    }
    
    public void appendArrayBegin(){
        if (pretty){
            appendIndent();
        }
        buffer.append("[");
        depthOffset++;
    }
    
    public void appendArrayEnd(){
        depthOffset--;
        if (pretty){
            appendIndent();
        }
        buffer.append("]");
    }

    public void appendComma(){
        buffer.append(",");
    }
    
    public void addDepthOffset(int offset){
        depthOffset += offset;
    }

    public void beginAssocOneIsNull(String key) {
        depthOffset++;
        internalAppendKeyBegin(key);
        appendNull();
        depthOffset--;
    }
    
    public void beginAssocOne(String key) {
        pathStack.pushPathKey(key);

        internalAppendKeyBegin(key);
        assocOne = true;
    }
    
    public void endAssocOne() {
        
        pathStack.pop();
        assocOne = false;
    }
    
    public Boolean includeMany(String key) {
        if (pathProperties != null){
            String fullPath = pathStack.peekFullPath(key);
            return pathProperties.hasPath(fullPath);
        }
        return null;
    }
    
    public void beginAssocMany(String key) {
        
        pathStack.pushPathKey(key);
        
        depthOffset--;
        internalAppendKeyBegin(key);
        depthOffset++;
        buffer.append("[");
    }

    public void endAssocMany(){
        
        pathStack.pop();
        
        if (pretty){
            depthOffset--;
            appendIndent();
            depthOffset++;
        }
        buffer.append("]");
    }
    
    private void internalAppendKeyBegin(String key) {
        if (!beanState.isFirstKey()){
            buffer.append(",");
        }
        if (pretty){
            appendIndent();
        }
        appendKeyWithComma(key, false);
    }

    public <T> void appendNameValue(String key, ScalarType<T> scalarType, T value) {
    	appendKeyWithComma(key, true);
    	scalarType.jsonWrite(buffer, value, getValueAdapter());
    }

	public void appendDiscriminator(String key, String discValue) {
    	appendKeyWithComma(key, true);
    	buffer.append("\"");
    	buffer.append(discValue);
    	buffer.append("\"");    	
    }   
    
    private void appendKeyWithComma(String key, boolean withComma) {
        if (withComma){
            if (!beanState.isFirstKey()){
                buffer.append(",");
            }
        }
        buffer.append("\"");
        if(key == null) {
            buffer.append("null");
        } else {
            buffer.append(key);
        }
        buffer.append("\":");
    }

    public void appendNull(String key) {
    	appendKeyWithComma(key, true);
        buffer.append("null");
    }

    public void appendNull() {
        buffer.append("null");
    }

    public JsonValueAdapter getValueAdapter() {
        return valueAdapter;
    }

    public String toString() {
        return buffer.toString();
    }
        
    public void popParentBean(){
        parentBeans.pop();
    }

    public void pushParentBean(Object parentBean){
        parentBeans.push(parentBean);
    }

    public void popParentBeanMany(){
        parentBeans.pop();
        depthOffset--;
    }
    
    public void pushParentBeanMany(Object parentBean){
        parentBeans.push(parentBean);
        depthOffset++;
    }

    public boolean isParentBean(Object bean){
        if (parentBeans.isEmpty()){
            return false;
        } else {
        	return parentBeans.contains(bean);
        }
    }
    
    public WriteBeanState pushBeanState(Object bean) {
        WriteBeanState newState = new WriteBeanState(bean);
        WriteBeanState prevState = beanState;
        beanState = newState;
        return prevState;
    }
    
    public void pushPreviousState(WriteBeanState previousState) {
        this.beanState = previousState;
    }
    
    public boolean isReferenceBean() {
        return beanState.isReferenceBean();
    }
    
    public boolean includedProp(String name) {
        return beanState.includedProp(name);
    }
    
    public Set<String> getLoadedProps() {
        return beanState.getLoadedProps();
    }
    
    
    public static class WriteBeanState {
        
        private final EntityBeanIntercept ebi;
        private final Set<String> loadedProps;
        private final boolean referenceBean;
        private boolean firstKeyOut;
        
        public WriteBeanState(Object bean) {
            if (bean instanceof EntityBean){
                this.ebi = ((EntityBean)bean)._ebean_getIntercept();
                this.loadedProps = ebi.getLoadedProps();
                this.referenceBean = ebi.isReference();
            } else {
                this.ebi = null;
                this.loadedProps = null;
                this.referenceBean = false;
            }
        }
        
        public Set<String> getLoadedProps() {
            return loadedProps;
        }
        
        public boolean includedProp(String name) {
            if (loadedProps == null || loadedProps.contains(name)){
                return true;
            } else {
                return false;
            }
        }
        public boolean isReferenceBean() {
            return referenceBean;
        }
        
        public boolean isFirstKey() {
            if (!firstKeyOut){
                firstKeyOut = true;
                return true;
            } else {
                return false;
            }
        }
        
    }
}
