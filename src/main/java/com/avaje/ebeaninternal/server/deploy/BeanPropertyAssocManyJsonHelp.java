package com.avaje.ebeaninternal.server.deploy;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.TextException;

public class BeanPropertyAssocManyJsonHelp {

  private final BeanPropertyAssocMany<?> many;

  public BeanPropertyAssocManyJsonHelp(BeanPropertyAssocMany<?> many) {
    this.many = many;
  }

  public void jsonRead(JsonParser parser, EntityBean parentBean) {
    
    if (!this.many.jsonDeserialize || !parser.hasNext()) {
      return;
    }
    Event event = parser.next();
    if (Event.VALUE_NULL == event) {
      return;
    }
    if (Event.START_ARRAY != event) {
      throw new TextException("Unexpected token "+event+" - expecting start_array at: "+parser.getLocation());
    }
    
    Object collection = many.createEmpty(false);
    BeanCollectionAdd add = many.getBeanCollectionAdd(collection, null);
    do {
      EntityBean detailBean = (EntityBean)many.targetDescriptor.jsonRead(parser, many.name);
      if (detailBean == null) {
        // read the entire array
        break;
      }
      add.addBean(detailBean);

      if (parentBean != null && many.childMasterProperty != null) {
        // bind detail bean back to master via mappedBy property
        many.childMasterProperty.setValue(detailBean, parentBean);
      }
    } while (true);

    many.setValue(parentBean, collection);
  }
}
