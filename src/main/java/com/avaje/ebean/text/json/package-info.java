/**
 * JSON formatting and parsing objects (See JsonContext).
 * <p>
 * The goal is to provide JSON support taking into account 
 * various ORM issues such as partial objects (for fetching and
 * updating), reference beans and
 * bi-directional relationships. 
 * </p>
 * 
 * <h3>Example:</h3>
 * <pre class="code">
 *  // find some customers ...
 *  
 * List&lt;Customer&gt; list = Ebean.find(Customer.class)
 *     .select(&quot;id, name, status, shippingAddress&quot;)
 *     .fetch(&quot;billingAddress&quot;,&quot;line1, city&quot;)
 *     .fetch(&quot;billingAddress.country&quot;, &quot;*&quot;)
 *     .fetch(&quot;contacts&quot;, &quot;firstName,email&quot;)
 *     .order().desc(&quot;id&quot;)
 *     .findList();
 * 
 * JsonContext json = Ebean.json();
 *
 *  // output as a JSON string
 * String jsonOutput = json.toJson(list);
 * 
 * </pre>
 */
package com.avaje.ebean.text.json;