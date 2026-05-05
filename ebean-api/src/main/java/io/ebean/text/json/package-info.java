/**
 * JSON formatting and parsing objects (See JsonContext).
 * <p>
 * The goal is to provide JSON support taking into account
 * various ORM issues such as partial objects (for fetching and
 * updating), reference beans and
 * bi-directional relationships.
 * </p>
 * <h3>Example:</h3>
 * <pre>{@code
 *  // find some customers ...
 *
 * List<Customer> list = DB.find(Customer.class)
 *     .select("id, name, status, shippingAddress")
 *     .fetch("billingAddress","line1, city")
 *     .fetch("billingAddress.country", "*")
 *     .fetch("contacts", "firstName,email")
 *     .orderBy().desc("id")
 *     .findList();
 *
 * JsonContext json = DB.json();
 *
 *  // output as a JSON string
 * String jsonOutput = json.toJson(list);
 *
 * }</pre>
 */
package io.ebean.text.json;
