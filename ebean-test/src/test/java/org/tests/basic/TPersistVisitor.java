package org.tests.basic;

import java.util.Collection;

import io.ebean.DB;
import io.ebean.PersistVisitor;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;
/**
 * Sample persist visitor that converts the visited beans in a XML-like structure
 */
public class TPersistVisitor implements PersistVisitor {

    private final StringBuilder sb;
    private final String indent;
    private final String tag;
    private boolean empty = true;
    
    public TPersistVisitor() {
      this(new StringBuilder(), "", "root"); 
    }
    private TPersistVisitor(StringBuilder sb, String indent, String tag) {
      this.sb = sb;
      this.indent = indent;
      this.tag = tag;
      this.sb.append(indent).append('<').append(tag);
    }
    
    TPersistVisitor newVisitor(String tag) {
      if (empty) {
        sb.append(">\n");
        empty = false;
      }
      return new TPersistVisitor(sb, indent + "  ", tag);
    }

    @Override
    public void visitEnd() {
      if (empty) {
        sb.append("/>\n");
      } else {
        this.sb.append(indent).append("</").append(tag).append(">\n");
      }
    }
    
    TPersistVisitor attr(String attr, Object value) {
      sb.append(' ').append(attr).append('=').append('\'').append(value).append('\'');
      return this;
    }
    
    public TPersistVisitor visitBean(EntityBean bean) {
      return newVisitor("bean").attr("type", bean.getClass().getSimpleName()).attr("newOrDirty", DB.beanState(bean).isNewOrDirty());
    }

    @Override
    public PersistVisitor visitProperty(Property prop) {
      return newVisitor("property").attr("name", prop.name());
    }

   @Override
  public PersistVisitor visitCollection(Collection<?> collection) {
     return newVisitor("collection").attr("size", collection.size());
    }
    
    @Override
    public String toString() {
      return sb.toString();
    }
  }