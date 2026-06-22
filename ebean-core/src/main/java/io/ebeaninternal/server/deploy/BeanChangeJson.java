package io.ebeaninternal.server.deploy;

import io.ebean.PersistenceIOException;
import io.ebean.bean.BeanDiffVisitor;
import io.ebean.config.JsonConfig;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.json.WriteJson;
import io.ebeaninternal.server.util.ArrayStack;
import io.avaje.json.stream.JsonStream;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Builds the 'new values' and 'old values' in JSON form for ChangeLog.
 */
final class BeanChangeJson implements BeanDiffVisitor {

  private final StringWriter data;
  private final SpiJsonWriter json;
  private final boolean writeNew;
  private final ArrayStack<BeanDescriptor<?>> stack = new ArrayStack<>();
  private BeanDescriptor<?> descriptor;

  BeanChangeJson(BeanDescriptor<?> descriptor, boolean writeNew) {
    this.descriptor = descriptor;
    this.writeNew = writeNew;
    this.data = new StringWriter(200);
    this.json = new WriteJson(JsonStream.builder().build().writer(data), JsonConfig.Include.ALL);
    json.writeStartObject();
  }

  @Override
  public void visit(int position, Object newVal, Object oldVal) {
    try {
      BeanProperty prop = descriptor.propertiesIndex[position];
      if (prop.isDbUpdatable()) {
        prop.jsonWriteValue(json, writeNew ? newVal : oldVal);
      }
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  @Override
  public void visitPush(int position) {
    stack.push(descriptor);
    BeanPropertyAssocOne<?> embedded = (BeanPropertyAssocOne<?>)descriptor.propertiesIndex[position];
    descriptor = embedded.targetDescriptor();
    json.writeStartObject(embedded.name());
  }

  @Override
  public void visitPop() {
    json.writeEndObject();
    descriptor = stack.pop();
  }

  /**
   * Flush the buffers.
   */
  void flush() {
    try {
      json.writeEndObject();
      json.flush();
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  String json() {
    return data.toString();
  }
}
