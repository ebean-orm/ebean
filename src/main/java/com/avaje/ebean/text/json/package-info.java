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
 * JsonContext json = Ebean.createJsonContext();
 * 
 * JsonWriteOptions writeOptions = new JsonWriteOptions();
 * writeOptions.setRootPathVisitor(new JsonWriteBeanVisitor&lt;Customer&gt;() {
 * 
 *     public void visit(Customer bean, JsonWriter ctx) {
 *         System.out.println(&quot;write visit customer: &quot; + bean);
 *         ctx.appendKeyValue(&quot;dummyCust&quot;, &quot;34&quot;);
 *         ctx.appendKeyValue(&quot;smallCustObject&quot;, &quot;{\&quot;a\&quot;:34,\&quot;b\&quot;:\&quot;asdasdasd\&quot;}&quot;);
 *     }
 * });
 * 
 * writeOptions.setPathProperties(&quot;contacts&quot;, &quot;firstName,id&quot;);
 * writeOptions.setPathVisitor(&quot;contacts&quot;, new JsonWriteBeanVisitor&lt;Contact&gt;() {
 * 
 *     public void visit(Contact bean, JsonWriter ctx) {
 *         System.out.println(&quot;write additional custom json on customer: &quot; + bean);
 *         ctx.appendKeyValue(&quot;dummy&quot;, &quot;  3400&quot; + bean.getId() + &quot;&quot;);
 *         ctx.appendKeyValue(&quot;smallObject&quot;, &quot;{\&quot;contactA\&quot;:34,\&quot;contactB\&quot;:\&quot;banana\&quot;}&quot;);
 *     }
 * 
 * });
 * 
 *  // output as a JSON string with pretty formatting
 * String s = json.toJsonString(list, true, writeOptions);
 * 
 * </pre>
 */
package com.avaje.ebean.text.json;