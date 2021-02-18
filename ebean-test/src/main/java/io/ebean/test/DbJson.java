package io.ebean.test;

import io.ebean.DB;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper for testing to assert that the JSON form of an entity
 * or list of entities is as expected.
 * <p>
 * Using assertContains() we can match the JSON form of an entity
 * against a subset of JSON content. Typically the subset of JSON
 * excludes generated properties like id, when created and
 * when modified properties.
 * </p>
 *
 * <h3>Assert contains</h3>
 * <pre>{@code
 *
 *    DbJson.of(order)
 *      .assertContains("/order-partial.json");
 *
 * }</pre>
 *
 *
 * <h3>Assert content matches</h3>
 * <p>
 * Using assertContentMatches() we are doing an exact content match.
 * We typically need to replace generated property values. This assert
 * will start to fail if the model changes like adding a property to
 * the entity so we should use it less widely due to the maintenance
 * burden we have with it.
 * </p>
 *
 * <pre>{@code
 *
 *    DbJson.of(order)
 *      .replace("id", "whenCreated", "whenModified)
 *      .assertContentMatches("/order-full.json");
 *
 * }</pre>
 */
public class DbJson {

  /**
   * Create a PrettyJson object that has the JSON form of the entity bean or beans.
   *
   * <h3>Assert contains</h3>
   * <pre>{@code
   *
   *    DbJson.of(order)
   *      .assertContains("/order-partial.json");
   *
   * }</pre>
   *
   * <h3>Assert content matches</h3>
   * <pre>{@code
   *
   *    DbJson.of(order)
   *      .replace("id", "whenCreated", "whenModified)
   *      .assertContentMatches("/order-full.json");
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
    return IOUtils.readResource(resourcePath);
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
     * Assert the json exactly matches the content at the given resource path.
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
      assertThat(lineEnd(rawJson)).isEqualTo(lineEnd(readResource(resourcePath)));
    }

    /**
     * Normalise line ending characters to just use new line.
     */
    private String lineEnd(String content) {
      return content.replace("\r\n", "\n");
    }

    /**
     * Assert the DB json contains the given json content.
     * <p>
     * With this "contains" check the DB Json can contain more content than what
     * it is checked against. Typically the DB json can contain generated properties
     * like id values, when created, when modified etc and we leave these out of the
     * json content we are checking against.
     * </p>
     *
     * @param json The subset json content that should be contained by the DB json.
     */
    public void assertContains(String json) {
      Json.assertContains(rawJson, json);
    }

    /**
     * Assert the DB Json contains the Json at the given resource path.
     * <p>
     * With this "contains" check the DB Json can contain more content than what
     * it is checked against. Typically the DB json can contain generated properties
     * like id values, when created, when modified etc and we leave these out of the
     * json content we are checking against.
     * </p>
     *
     * @param resourcePath The resource path of the JSON content we are checking against.
     */
    public void assertContainsResource(String resourcePath) {
      assertContains(readResource(resourcePath));
    }
  }
}
