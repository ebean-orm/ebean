package io.ebeaninternal.server.deploy;

import io.ebean.PersistenceIOException;
import io.ebean.bean.BeanDiffVisitor;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.util.ArrayStack;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Builds the 'new values' and 'old values' in JSON form for ChangeLog.
 */
class BeanChangeJson implements BeanDiffVisitor {

  private final StringWriter newData;
  private final StringWriter oldData;

  private final SpiJsonWriter newJson;
  private final SpiJsonWriter oldJson;

  private final ArrayStack<BeanDescriptor<?>> stack = new ArrayStack<>();

  private BeanDescriptor<?> descriptor;

  BeanChangeJson(BeanDescriptor<?> descriptor, boolean statelessUpdate) {
    this.descriptor = descriptor;
    this.newData = new StringWriter(200);
    this.newJson = descriptor.createJsonWriter(newData);
    newJson.writeStartObject();

    if (statelessUpdate) {
      this.oldJson = null;
      this.oldData = null;
    } else {
      this.oldData = new StringWriter(200);
      this.oldJson = descriptor.createJsonWriter(oldData);
      oldJson.writeStartObject();
    }
  }

  @Override
  public void visit(int position, Object newVal, Object oldVal) {

    try {
      BeanProperty prop = descriptor.propertiesIndex[position];
      if (prop.isDbUpdatable()) {
        prop.jsonWriteValue(newJson, newVal);
        if (oldJson != null) {
          prop.jsonWriteValue(oldJson, oldVal);
        }
      }
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  @Override
  public void visitPush(int position) {
    stack.push(descriptor);

    BeanPropertyAssocOne<?> embedded = (BeanPropertyAssocOne<?>)descriptor.propertiesIndex[position];
    descriptor = embedded.getTargetDescriptor();
    newJson.writeStartObject(embedded.getName());
    if (oldJson != null) {
      oldJson.writeStartObject(embedded.getName());
    }
  }

  @Override
  public void visitPop() {
    newJson.writeEndObject();
    if (oldJson != null) {
      oldJson.writeEndObject();
    }
    descriptor = stack.pop();
  }

  /**
   * Flush the buffers.
   */
  void flush() {
    try {
      newJson.writeEndObject();
      newJson.flush();
      if (oldJson != null) {
        oldJson.writeEndObject();
        oldJson.flush();
      }
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  /**
   * Return the new values JSON.
   */
  String newJson() {
    return newData.toString();
  }

  /**
   * Return the old values JSON.
   */
  String oldJson() {
    return oldData == null ? null : oldData.toString();
  }
}
