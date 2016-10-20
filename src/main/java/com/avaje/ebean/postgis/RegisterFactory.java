package com.avaje.ebean.postgis;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.plugin.ExtraTypeFactory;
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
    list.add(new ScalarTypePgisMultiPolygon());

    if (serverConfig.getClassLoadConfig().isPresent("org.geolatte.geom.Geometry")) {
      list.add(new ScalarTypeGeoLattePoint());
      list.add(new ScalarTypeGeoLattePolygon());
      list.add(new ScalarTypeGeoLatteMultiPolygon());
    }

    return list;
  }
}
