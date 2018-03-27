package io.ebean.config.properties;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Loads configuration from Yml into the load context.
 * <p>
 * Note that this ignores 'lists' so just reads 'maps' and scalar values.
 * </p>
 */
class YamlLoader {

  private final Yaml yaml = new Yaml();

  private final LoadContext loadContext;

  YamlLoader(LoadContext loadContext) {
    this.loadContext = loadContext;
  }

  @SuppressWarnings("unchecked")
  void load(InputStream is) {
    if (is != null) {
      for (Object map : yaml.loadAll(is)) {
        loadMap((Map<String, Object>)map, null);
      }
    }
  }

  @SuppressWarnings("unchecked")
  void loadMap(Map<String, Object> map, String path) {

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      if (path != null) {
        key = path + "." + key;
      }
      Object val = entry.getValue();
      if (val instanceof Map) {
        loadMap((Map<String, Object>) val, key);
      } else  {
        addScalar(key, val);
      }
    }
  }

  private void addScalar(String key, Object val) {
    if (val instanceof String) {
      loadContext.put(key, (String) val);
    } else if (val instanceof Number || val instanceof Boolean) {
      loadContext.put(key, val.toString());
    }
  }

}
