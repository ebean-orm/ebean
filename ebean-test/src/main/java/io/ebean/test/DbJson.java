package io.ebean.test;

import io.ebean.DB;
import io.ebean.migration.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper for testing to assert that the JSON form of an entity
 * or list of entities match a String / typically test resource.
 *
 * <pre>{@code
 *
 *    DbJson.of(timedEntries)
 *      .replace("id", "eventTime")
 *      .assertContentMatches("/assertJson/full-1-timed.json");
 *
 * }</pre>
 */
public class DbJson {

  /**
   * Create a PrettyJson object that has the JSON form of the
   * entity bean or beans.
   *
   * <pre>{@code
   *
   *    DbJson.of(timedEntries)
   *      .replace("id", "eventTime")
   *      .assertContentMatches("/assertJson/full-1-timed.json");
   *
   * }</pre>
   */
  public static PrettyJson of(Object bean) {
    return new PrettyJson(DB.json().toJsonPretty(bean));
  }

  /**
   * Read the content for the given resource path.
   */
  public static String readResource(String resourcePath) {
    InputStream is = DbJson.class.getResourceAsStream(resourcePath);
    try {
      return IOUtils.readUtf8(is).trim();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Contains the JSON of beans(s).
   */
  public static class PrettyJson {

    private String placeHolder = "\"*\"";
    private String rawJson;

    PrettyJson(String rawJson) {
      this.rawJson = rawJson;
    }

    /**
     * Set the placeHolder to use when replacing property values.
     */
    public PrettyJson withPlaceholder(String placeHolder) {
      this.placeHolder = placeHolder;
      return this;
    }

    /**
     * Replace the values of the given properties with a placeholder value.
     * <p>
     * Typically we do this on generated properties such as id and timestamp properties.
     * </p>
     */
    public PrettyJson replace(String... propertyNames) {
      for (String propertyName : propertyNames) {
        String placeholder = "\"" + propertyName + "\": " + placeHolder;
        rawJson = rawJson.replaceAll("\"" + propertyName + "\": (\\d+)", placeholder);
        rawJson = rawJson.replaceAll("\"" + propertyName + "\": \"(.*?)\"", placeholder);
      }
      return this;
    }

    /**
     * Return the JSON content.
     */
    public String asJson() {
      return rawJson;
    }

    /**
     * Assert the json matches the content at the given resource path.
     *
     * <pre>{@code
     *
     *    DbJson.of(timedEntries)
     *      .replace("id", "eventTime")
     *      .assertContentMatches("/assertJson/full-1-timed.json");
     *
     * }</pre>
     */
    public void assertContentMatches(String resourcePath) {
      assertThat(rawJson).isEqualTo(readResource(resourcePath));
    }
  }
}
