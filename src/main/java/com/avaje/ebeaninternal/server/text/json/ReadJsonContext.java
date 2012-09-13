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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.text.json.JsonElement;
import com.avaje.ebean.text.json.JsonReadBeanVisitor;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.util.ArrayStack;

public class ReadJsonContext extends ReadBasicJsonContext {
    
    private final Map<String, JsonReadBeanVisitor<?>> visitorMap;

    private final JsonValueAdapter valueAdapter;

    private final PathStack pathStack;

    private final ArrayStack<ReadBeanState> beanState;
    private ReadBeanState currentState;
    
    public ReadJsonContext(ReadJsonSource src, JsonValueAdapter dfltValueAdapter, JsonReadOptions options) {
    	super(src);
        this.beanState = new ArrayStack<ReadBeanState>();
        if (options == null){
            this.valueAdapter = dfltValueAdapter;
            this.visitorMap = null;
            this.pathStack = null;
        } else {
            this.valueAdapter = getValueAdapter(dfltValueAdapter, options.getValueAdapter());
            this.visitorMap = options.getVisitorMap();
            this.pathStack = (visitorMap == null || visitorMap.isEmpty()) ? null : new PathStack();
        }
    }
    
    private JsonValueAdapter getValueAdapter(JsonValueAdapter dfltValueAdapter, JsonValueAdapter valueAdapter) {
        return valueAdapter == null ? dfltValueAdapter : valueAdapter;
    }

    public JsonValueAdapter getValueAdapter() {
        return valueAdapter;
    }

    public String readScalarValue() {
        
        ignoreWhiteSpace();
        
        char prevChar = nextChar();//"EOF reading scalarValue?");
        if ('"' == prevChar){
            return readQuotedValue();
        } else {
            return readUnquotedValue(prevChar);
        }
    }
        
    public void pushBean(Object bean, String path, BeanDescriptor<?> beanDescriptor){
        currentState = new ReadBeanState(bean, beanDescriptor);
        beanState.push(currentState);
        if (pathStack != null){
            pathStack.pushPathKey(path);
        }
    }
    
    public ReadBeanState popBeanState() {
        if (pathStack != null){
            String path = pathStack.peekWithNull();
            JsonReadBeanVisitor<?> beanVisitor = visitorMap.get(path);
            if (beanVisitor != null){
                currentState.visit(beanVisitor);
            }
            pathStack.pop();
        }
        
        // return the current ReadBeanState as we can't call setLoadedState()
        // yet. We might bind master/detail beans together via mappedBy property
        // so wait until after that before calling ReadBeanStatesetLoadedState();
        ReadBeanState s = currentState;
        
        beanState.pop();
        currentState = beanState.peekWithNull();
        return s;
    }
    
    public void setProperty(String propertyName){
        currentState.setLoaded(propertyName);
    }
        
    /**
     * Got a key that doesn't map to a known property so read the json value
     * which could be json primitive, object or array.
     * <p>
     * Provide these values to a JsonReadBeanVisitor if registered.
     * </p>
     */
    public JsonElement readUnmappedJson(String key) {
        
    	JsonElement rawJsonValue = ReadJsonRawReader.readJsonElement(this);
        if (visitorMap != null){
            currentState.addUnmappedJson(key, rawJsonValue);
        }
        return rawJsonValue;
    }

    public static class ReadBeanState implements PropertyChangeListener {
        
        private final Object bean;
        private final BeanDescriptor<?> beanDescriptor;
        private final EntityBeanIntercept ebi;
        private final Set<String> loadedProps;
        private Map<String,JsonElement> unmapped;
        
        private ReadBeanState(Object bean, BeanDescriptor<?> beanDescriptor) {
            this.bean = bean;
            this.beanDescriptor = beanDescriptor;
            if (bean instanceof EntityBean){
                this.ebi = ((EntityBean)bean)._ebean_getIntercept();
                this.loadedProps = new HashSet<String>();
            } else {
                this.ebi = null;
                this.loadedProps = null;
            }
        }
        public String toString(){
            return bean.getClass().getSimpleName()+" loaded:"+loadedProps;
        }
        
        /**
         * Add a loaded/set property to the set of loadedProps.
         */
        public void setLoaded(String propertyName){
            if (ebi != null){
                loadedProps.add(propertyName);
            }
        }
        
        private void addUnmappedJson(String key, JsonElement value){
            if (unmapped == null){
                unmapped = new LinkedHashMap<String, JsonElement>();
            }
            unmapped.put(key, value);
        }
        
        @SuppressWarnings("unchecked")
        private <T> void visit(JsonReadBeanVisitor<T> beanVisitor) {
            // listen for property change events so that 
            // we can update the loadedProps if necessary
            if (ebi != null){
                ebi.addPropertyChangeListener(this);
            }
            beanVisitor.visit((T)bean, unmapped);
            if (ebi != null){
                ebi.removePropertyChangeListener(this);
            }
        }
        
        public void setLoadedState(){
            if (ebi != null){
                // takes into account reference beans
                beanDescriptor.setLoadedProps(ebi, loadedProps);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            loadedProps.add(propName);
        }
        
        public Object getBean() {
            return bean;
        }
        
    }

}
