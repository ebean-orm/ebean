package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.annotation.Platform;
import io.ebean.cache.ServerCacheManager;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.core.type.ScalarTypeUtils;
import io.ebean.meta.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * HtmlMetricReportGenerator provides a neat interface for ebean metrics.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class HtmlMetricReportGenerator implements MetricReportGenerator {

  private static final Pattern SPLITPATTERN = Pattern.compile("[\\._]");
  // pattern for MariaDB binary UUIDs
  private static final Pattern BINPATTERN = Pattern.compile("\\[.*\\[(-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), "
    + "(-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), "
    + "(-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3}), (-?\\d{1,3})\\].*\\]");

  private final DatabasePlatform platform;
  private final MetaInfoManager metaInfo;
  private final ServerCacheManager cacheManager;
  private final QueryPlanRequest queryRequest = new QueryPlanRequest();
  private final QueryPlanInit initRequest = new QueryPlanInit();
  private final String name;
  private List<MetaQueryPlan> queryPlans = Collections.emptyList();

  /**
   * Initializes the report with some common defaults.
   */
  public HtmlMetricReportGenerator(Database db) {
    platform = db.pluginApi().databasePlatform();
    metaInfo = db.metaInfo();
    cacheManager = db.cacheManager();
    initRequest.thresholdMicros(100_000);
    initRequest.setAll(true);
    queryRequest.maxCount(10);
    queryRequest.maxTimeMillis(30_000);
    name = db.name();
  }

  /**
   * This is used to configure the current report.
   * It will receive the REST calls from the web application and returns either OK or RELOAD if the page should be reloaded.
   */
  @Override
  public synchronized String configure(List<MetricReportValue> configurations) {
    String ret = "OK";
    for (MetricReportValue configuration : configurations) {

      if (configuration.getName().startsWith("hash.")) {
        String hash = configuration.getName().substring(5);
        if (configuration.intValue() > 0) {
          initRequest.hashes().add(hash);
        } else {
          initRequest.hashes().remove(hash);
        }
        if (initRequest.isAll()) {
          initRequest.setAll(false);
          ret = "REFRESH";
        }

      } else {
        switch (configuration.getName()) {
          case "queryRequest.maxCount":
            queryRequest.maxCount(configuration.intValue());
            break;

          case "queryRequest.maxTimeMillis":
            queryRequest.maxTimeMillis(configuration.intValue());
            break;

          case "queryRequest.since":
            queryRequest.since(configuration.intValue());
            break;

          case "queryRequest.apply":
            queryPlans = metaInfo.queryPlanCollectNow(queryRequest);
            ret = "REFRESH";
            break;

          case "initRequest.thresholdMicros":
            initRequest.thresholdMicros(configuration.intValue());
            break;

          case "initRequest.isAll":
            if (configuration.intValue() > 0) {
              initRequest.hashes().clear();
            }
            initRequest.setAll(configuration.intValue() > 0);
            break;

          case "initRequest.apply":
            queryPlans = metaInfo.queryPlanInit(initRequest);
            ret = "REFRESH";
            break;

          case "clearCaches":
            cacheManager.clearAll();
            ret = "REFRESH";
            break;

          case "resetMetrics":
            metaInfo.resetAllMetrics();
            ret = "REFRESH";
            break;

          default:
            throw new IllegalArgumentException(configuration.getName() + " is invalid");

        }
      }
    }
    return ret;
  }

  /**
   * Writes the report as UTF-8 to the outputstream.
   */
  @Override
  public synchronized void writeReport(OutputStream out) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    Html html = new Html();
    createTabs(html);
    StringBuilder sb = new StringBuilder(60
    );
    sb.append("Ebean metrics report for ");
    addText(sb, name); // prevent HTML injection in name ;)
    html.write(writer, sb.toString());
    writer.flush();
  }

  /**
   * Create tabs. Can be overwritten to create additional tabs.
   */
  protected void createTabs(Html html) {
    // by default, we want to collect all metrics, but do not want to reset them
    BasicMetricVisitor bmv = new BasicMetricVisitor(name, MetricNamingMatch.INSTANCE, false, true, true, true);
    metaInfo.visitMetrics(bmv);
    actionTab(html);
    queryMetricTab(bmv.queryMetrics(), html);
    timedMetricTab(bmv.timedMetrics(), html);
    countMetricTab(bmv.countMetrics(), html);
    queryPlansTab(html);
  }

  /**
   * 1st tab: Action tab.
   */
  protected void actionTab(Html html) {
    HtmlTab tab;

    tab = html.tab("Actions");
    tab.startTable("Name", "Value");
    tab.input("initRequest.thresholdMicros", initRequest.thresholdMicros());
    tab.input("initRequest.isAll", initRequest.isAll() ? 1 : 0);
    tab.action("initRequest.apply", "Start capturing");

    tab.input("queryRequest.maxCount", queryRequest.maxCount());
    tab.input("queryRequest.maxTimeMillis", queryRequest.maxTimeMillis());
    tab.input("queryRequest.since", queryRequest.since());
    tab.tableRow("current time", System.currentTimeMillis());
    tab.action("queryRequest.apply", "Collect plans");


    tab.action("clearCaches", "Clear caches");
    tab.action("resetMetrics", "Reset all metrics");

    tab.endTable();
    tab.html("<h2>Usage:</h2>");
    tab.html("<ol>\n");
    tab.html("<li>Set initRequest.thresholdMicros to the value, you want to capture</li>\n");
    tab.html("<li>Add hashes or set initRequest.isAll = 1 (default)</li>\n");
    tab.html("<li>Click 'Start capturing'</li>\n");
    tab.html("<li>Do the action, which executes the query/queries you want to inspect</li>\n");
    tab.html("<li>Click 'Collect plans' to collect the query-plans</li>\n");
    tab.html("</ol>\n");
  }

  /**
   * 2nd tab: Query Metrics.
   */
  protected void queryMetricTab(List<MetaQueryMetric> metrics, Html html) {

    HtmlTab tab = html.tab("Query metrics");
    tab.startTable("type?", "?", "?", "?", "count", "total", "mean", "max", "sql", "hash");
    for (MetaQueryMetric metric : metrics) {
      String[] names = splitPad(metric.name(), 4);
      tab.tableRow(names[0], names[1], names[2], names[3],
        metric.count(),
        micros(metric.total()), micros(metric.mean()), micros(metric.max()),
        metric.sql(), hash(metric.hash()));
    }
    tab.endTable();
  }

  /**
   * 3rd tab: Timed metrics.
   */
  protected void timedMetricTab(List<MetaTimedMetric> metrics, Html html) {
    HtmlTab tab = html.tab("Timed metrics");
    tab.startTable("type?", "?", "?", "?", "count", "total", "mean", "max");
    for (MetaTimedMetric metric : metrics) {
      String[] names = splitPad(metric.name(), 4);
      tab.tableRow(names[0], names[1], names[2], names[3],
        metric.count(),
        micros(metric.total()), micros(metric.mean()), micros(metric.max()));
    }
    tab.endTable();
  }

  /**
   * 4th tab: Count metrics.
   */
  protected void countMetricTab(List<MetaCountMetric> metrics, Html html) {
    HtmlTab tab = html.tab("Count Metrics");
    tab.startTable("type?", "?", "?", "?", "count");
    for (MetaCountMetric metric : metrics) {
      String[] names = splitPad(metric.name(), 4);
      tab.tableRow(names[0], names[1], names[2], names[3], metric.count());
    }
    tab.endTable();
  }

  /**
   * 5th tab: Query plans.
   */
  protected void queryPlansTab(Html html) {
    HtmlTab tab = html.tab("Query plans");
    tab.startTable("type?", "tenant?", "count", "micros", "sql", "whenCaptured", "captureMicros","hash");
    for (MetaQueryPlan queryPlan : queryPlans) {
      tab.tableRow(queryPlan.beanType().getSimpleName(),
        queryPlan.tenantId(),
        queryPlan.captureCount(),
        micros(queryPlan.queryTimeMicros()),
        queryPlan(queryPlan.sql(), queryPlan.bind(), queryPlan.plan()),
        queryPlan.whenCaptured(),
        queryPlan.captureMicros(),
        hash(queryPlan.hash()));
    }
    tab.endTable();
  }

  /**
   * Splits the string on '.' or '_' with a fixed <code>length</code> of entries.
   */
  protected static String[] splitPad(String name, int length) {
    String[] ret = SPLITPATTERN.split(name, length);
    if (ret.length < length) {
      return Arrays.copyOf(ret, length);
    }
    return ret;
  }

  /**
   * Adds html-safe text to the string-builder.
   */
  protected static void addText(StringBuilder sb, String text) {
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);

      switch (ch) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '\'':
          sb.append("&#39;");
          break; // HTML entity for Apostroph '
        default:
          sb.append(ch);
          break;
      }
    }
  }

  /**
   * Creates a QueryPlan object. This is rendered as sql and optionally displayable bind and plan.
   */
  protected QueryPlan queryPlan(String sql, String bind, String plan) {

    if (bind != null && (platform.isPlatform(Platform.MARIADB) || platform.isPlatform(Platform.MYSQL))) {
      // MariaDB UUIDs beatifier.
      Matcher matcher = BINPATTERN.matcher(bind);

      while (matcher.matches()) {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
          bytes[i] = Byte.parseByte(matcher.group(i + 1));
        }
        UUID uuid = ScalarTypeUtils.uuidFromBytes(bytes, true);
        bind = bind.substring(0, matcher.start(1) - 1) + uuid + bind.substring(matcher.end(16) + 1);
        matcher = BINPATTERN.matcher(bind);
      }
    }
    return new QueryPlan(sql, bind, plan, platform.isPlatform(Platform.SQLSERVER));
  }

  /**
   * Creates a Micros object. It is rendered in human readable form. e.g. "12345678" micros is converted to "12.3 s"
   */
  protected Micros micros(long value) {
    return new Micros(value);
  }

  /**
   * Creates a Hash object. It is rendered with a checkbox.
   */
  protected Hash hash(String hash) {
    return new Hash(hash, initRequest.includeHash(hash));
  }

  /**
   * Returns the currently collected plans. Maybe useful, if you want to fetch them via JSON.
   */
  public List<MetaQueryPlan> getCurrentPlans() {
    return queryPlans;
  }

  /**
   * Human readable micros object.
   */
  protected static class Micros {
    private final long micros;

    protected Micros(long micros) {
      this.micros = micros;
    }

    @Override
    public String toString() {
      if (micros < 1_000L) {
        return String.format("%d µs", micros);
      } else if (micros < 10_000L) {
        return String.format("%.2f ms", micros / 1000d);
      } else if (micros < 100_000L) {
        return String.format("%.1f ms", micros / 1000d);
      } else if (micros < 1_000_000L) {
        return String.format("%.0f ms", micros / 1000d);
      } else if (micros < 10_000_000L) {
        return String.format("%.2f s", micros / 1000_000d);
      } else if (micros < 100_000_000L) {
        return String.format("%.1f s", micros / 1000_000d);
      } else {
        return String.format("%.0f s", micros / 1000_000d);
      }
    }
  }

  /**
   * QueryPan popup object.
   */
  protected static class QueryPlan {
    final String sql;
    final String bind;
    final String plan;
    final boolean sqlServer;

    QueryPlan(String sql, String bind, String plan, boolean sqlServer) {
      this.sql = sql;
      this.bind = bind;
      this.plan = plan;
      this.sqlServer = sqlServer;
    }
  }

  /**
   * Hash object.
   */
  public static class Hash {
    final String hash;
    final boolean checked;

    Hash(String hash, boolean checked) {
      this.hash = hash;
      this.checked = checked;
    }

    @Override
    public String toString() {
      return hash;
    }
  }

  /**
   * This class reprensets the whole HTML plage with it's tabs.
   */
  public static class Html {

    private final List<HtmlTab> tabs = new ArrayList<>();

    public HtmlTab tab(String title) {
      HtmlTab htmlTab = new HtmlTab(title);
      tabs.add(htmlTab);
      return htmlTab;
    }

    protected void appendResource(Appendable out, String resName) throws IOException {
      InputStream res = getClass().getResourceAsStream(resName);
      if (res == null) {
        throw new NullPointerException("Could not find " + resName);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(res, StandardCharsets.UTF_8))) {
        Iterator<String> it = reader.lines().iterator();
        while (it.hasNext()) {
          out.append(it.next()).append('\n');
        }
      }
    }

    public void write(Appendable out, String title) throws IOException {
      out.append("<!DOCTYPE html><html lang='en'>\n");
      out.append("<head>\n");
      out.append("<title>");
      out.append(title);
      out.append("</title>");
      //sb.append("<link rel='stylesheet' type='text/css' href='metrics.css'/>");
      out.append("<style type='text/css'>\n");
      appendResource(out, "metrics.css");

      // CSS stuff for tabs. Based on https://www.mediaevent.de/tutorial/css-tabs.html
      addTabCss(out, "#tab@:checked ~ figure .tab@", "{display: block;}");
      addTabCss(out, "#tab@:checked ~ nav label[for='tab@']", "{background: white; color: #111; position: relative;}");
      out.append(".tabbed figure {display: block; margin-left: 0; border-bottom: 1px solid silver; clear: both;}\n");
      out.append(".tabbed > input, .tabbed figure > div {display: none;}\n");
      out.append(".tabbed figure>div {width: 100%;}\n");
      out.append("nav label {float: left; padding: 15px 15px; border: 1px solid silver; background: #ef781b; color: #eee;}\n");
      out.append("nav label:hover {background: #888;}\n");

      out.append("</style>");
      out.append("</head>\n");
      out.append("<body>");
      out.append("<h1>");
      out.append(title);
      out.append("</h1>");
      out.append("<input type='checkbox' id='raw'> display raw values<br>");
      out.append("<input type='checkbox' id='binds'> display query plan bind values<br>");
      out.append("<input type='checkbox' id='plans'> display query plan details<br>");
      out.append("<div class='tabbed'>\n");
      // add radio inputs that controls the selected tabs.
      for (int i = 0; i < tabs.size(); i++) {
        out.append("  <input id='tab").append(String.valueOf(i)).append("' type='radio'  name='tabs'");
        if (i == 0) {
          out.append(" checked='checked'");
        }
        out.append("/>\n");
      }

      // add labels (=tabs)
      out.append("  <nav>\n");
      for (int i = 0; i < tabs.size(); i++) {
        out.append("    <label for='tab").append(String.valueOf(i)).append("'>");
        out.append(tabs.get(i).getTitle()); // HTML in title would be allowed
        out.append("</label>\n");
      }
      out.append("  </nav>\n");

      // add tab contents
      out.append("  <figure>\n");
      for (int i = 0; i < tabs.size(); i++) {
        out.append("    <div class='tab").append(String.valueOf(i)).append("'>");
        out.append(tabs.get(i).toString());
        out.append("</div>\n");
      }
      out.append("  </figure>\n");
      out.append("</div>\n");
      // add javascript.
      out.append("<script>");
      appendResource(out, "metrics.js");
      out.append("</script></body></html>");
    }

    /**
     * Adds css for each tab by replacing the '@' sign with tab numbers.
     */
    protected void addTabCss(Appendable sb, String template, String css) throws IOException {
      for (int i = 0; i < tabs.size(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(template.replace("@", String.valueOf(i)));
      }
      sb.append(css);
      sb.append('\n');
    }
  }

  /**
   * HtmlTab that is mainly used to render tables.
   */
  public static class HtmlTab {

    private final String title;

    private final StringBuilder sb = new StringBuilder();

    HtmlTab(String title) {
      this.title = title;
    }

    /**
     * Return the table's title.
     */
    public String getTitle() {
      return title;
    }

    /**
     * Adds table headers. If header ends with '?' - a filter will be added.
     */
    public void startTable(String... headers) {
      sb.append("<table class='sortable'>\n");
      sb.append("<thead>\n");
      sb.append("<tr class='sortHdr'>");

      boolean hasFilter = false;
      for (String header : headers) {
        sb.append("<th>");
        addText(sb, header);
        if (header.endsWith("?")) {
          sb.setLength(sb.length() - 1);
          hasFilter = true;
        }
        sb.append("</th>\n");
      }
      sb.append("</tr>");
      if (hasFilter) {
        // add second header row for filters
        sb.append("<tr class='filterHdr'>");
        for (String header : headers) {
          sb.append("<th>");
          if (header.endsWith("?")) {
            sb.append("<select/>");
          } else {
            sb.append("&nbsp;");
          }
          sb.append("</th>\n");
        }
        sb.append("</tr>");
      }
      sb.append("</thead><tbody>\n");
    }

    public void tableRow(Object... items) {
      sb.append("<tr>");

      for (Object item : items) {
        if (item == null) {
          sb.append("<td>&nbsp;</td>");

        } else if (item instanceof QueryPlan) {
          // Render the QueryPlan sql, bind and plan output.
          sb.append("<td>");
          QueryPlan queryPlan = (QueryPlan) item;
          addText(sb, String.valueOf(queryPlan.sql));
          if (queryPlan.bind != null) {
            sb.append("<br><i class='bind'>");
            addText(sb, queryPlan.bind);
            sb.append("</i>");
          }
          if (queryPlan.plan != null) {
            sb.append("<code class='plan'>");
            if (queryPlan.sqlServer) {
              sb.append("<a href='data:application/octet-stream;base64,");
              sb.append(Base64.getEncoder().encodeToString(queryPlan.plan.getBytes(StandardCharsets.UTF_8)));
              sb.append("' download='").append(items[0]).append('-').append(items[items.length - 1])
                .append(".sqlplan'>Download</a>");
            } else {
              addText(sb, String.valueOf(queryPlan.plan));
            }
            sb.append("</code>");
          }
          sb.append("</td>");

        } else if (item instanceof Micros) {
          sb.append("<td class='number' data-sort='").append(((Micros) item).micros).append("'><div>");
          addText(sb, String.valueOf(item));
          sb.append("</div><div>");
          addText(sb, String.valueOf(((Micros) item).micros));
          sb.append("</div></td>");

        } else if (item instanceof Number) {
          sb.append("<td class='number'>");
          addText(sb, String.valueOf(item));
          sb.append("</td>");

        } else if (item instanceof Hash) {
          Hash hash = (Hash) item;
          sb.append("<td class='hash'>");
          sb.append("<input type='checkbox' name='hash.").append(hash.hash).append("' onchange='updateValue(this)'");
          if (hash.checked) {
            sb.append(" checked='checked'");
          }
          sb.append("> ").append(hash.hash).append("</td>");

        } else {
          sb.append("<td>");
          addText(sb, String.valueOf(item));
          sb.append("</td>");

        }
      }
      sb.append("</tr>\n");
    }

    /**
     * Adds an input field to the table. Changes are sent to the <code>updateValue</code> javascript function.
     */
    public void input(String label, long value) {
      sb.append("<tr><td>");
      addText(sb, label);
      sb.append("</td><td><input name='");
      addText(sb, label);
      sb.append("' value='").append(value).append("' onchange='updateValue(this)'/>");
      sb.append("</td></tr>\n");
    }

    /**
     * Adds an action button to the table. Changes are sent to the <code>updateValue</code> javascript function.
     */
    public void action(String actionId, String caption) {
      sb.append("<tr><td>&nbsp;</td>");
      sb.append("<td><button name='");
      addText(sb, actionId);
      sb.append("' value='1' onclick='updateValue(this)'>");
      addText(sb, caption);
      sb.append("</button></td></tr>\n");
    }

    public void endTable() {
      // add a second tbody. it is used as buffer for filtered entries
      sb.append("</tbody><tbody class='filtered'></tbody></table>\n");
    }

    /**
     * Add HTML to this tab.
     */
    public void html(String html) {
      sb.append(html);
    }

    @Override
    public String toString() {
      return sb.toString();
    }
  }
}
