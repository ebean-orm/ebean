package com.avaje.ebean.postgis;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.plugin.ExtraTypeFactory;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLatteLineString;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLatteMultiLineString;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLatteMultiPoint;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLatteMultiPolygon;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLattePoint;
import com.avaje.ebean.postgis.latte.ScalarTypeGeoLattePolygon;
import com.avaje.ebeaninternal.server.type.ScalarType;

import java.util.ArrayList;
import java.util.List;

public class RegisterFactory implements ExtraTypeFactory {


  @Override
  public List<ScalarType<?>> createTypes(ServerConfig serverConfig, Object objectMapper) {

    List<ScalarType<?>> list = new ArrayList<ScalarType<?>>();
    list.add(new ScalarTypePgisPoint());
    list.add(new ScalarTypePgisPolygon());
    list.add(new ScalarTypePgisLineString());
    list.add(new ScalarTypePgisMultiPolygon());
    list.add(new ScalarTypePgisMultiPoint());
    list.add(new ScalarTypePgisMultiLineString());

    if (serverConfig.getClassLoadConfig().isPresent("org.geolatte.geom.Geometry")) {
      list.add(new ScalarTypeGeoLattePoint());
      list.add(new ScalarTypeGeoLattePolygon());
      list.add(new ScalarTypeGeoLatteLineString());
      list.add(new ScalarTypeGeoLatteMultiPolygon());
      list.add(new ScalarTypeGeoLatteMultiPoint());
      list.add(new ScalarTypeGeoLatteMultiLineString());
    }

    return list;
  }
}
