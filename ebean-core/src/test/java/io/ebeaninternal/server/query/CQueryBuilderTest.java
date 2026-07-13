package io.ebeaninternal.server.query;

import io.ebean.ProfileLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CQueryBuilderTest {

  /**
   * Simulates what buildRowCountQuery does: strip the top-level order by, then wrap with count.
   * This is the logic that must use lastTopLevelOrderBy instead of lastIndexOf.
   */
  private static String simulateCountWrap(String sql) {
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    if (pos != -1) {
      sql = sql.substring(0, pos);
    }
    return "select count(*) from ( " + sql + ") as c";
  }

  private static int countChar(String s, char c) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) count++;
    }
    return count;
  }

  @Test
  void lastTopLevelOrderBy_simple() {
    String sql = "select t0.id from ad t0 order by t0.id";
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    assertThat(pos).isEqualTo(sql.indexOf(" order by "));
  }

  @Test
  void lastTopLevelOrderBy_noOrderBy() {
    String sql = "select t0.id from ad t0";
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    assertThat(pos).isEqualTo(-1);
  }

  @Test
  void lastTopLevelOrderBy_insideSubquery() {
    // order by is only inside a subquery - should not be found at top level
    String sql = "select t0.id from ad t0 where t0.id in (select t0.id from ad t0 order by t0.rebate)";
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    assertThat(pos).isEqualTo(-1);
  }

  @Test
  void lastTopLevelOrderBy_bothLevels() {
    // order by inside subquery AND at top level - should find only the top-level one
    String sql = "select t0.id from ad t0 where t0.id in (select t0.id from ad t0 order by t0.rebate) order by t0.id";
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    assertThat(sql.substring(pos)).isEqualTo(" order by t0.id");
  }

  @Test
  void lastTopLevelOrderBy_nestedSubqueries() {
    // deeply nested order by should not be found
    String sql = "select t0.id from ad t0 where t0.id in (select t0.id from ad t0 where t0.x in (select id from foo order by bar))";
    int pos = CQueryBuilder.lastTopLevelOrderBy(sql);
    assertThat(pos).isEqualTo(-1);
  }

  /**
   * Reproduces the exact scenario from https://github.com/ebean-orm/ebean/issues/3686
   *
   * With lastIndexOf(" order by "), the inner subquery's order by is matched,
   * stripping its closing parenthesis and producing unbalanced SQL.
   */
  @Test
  void countWrap_formulaJoinWithSubqueryOrderBy_issue3686() {
    // This is the SQL that buildRowCountQuery would produce before wrapping,
    // matching the bug report: @Formula with JOIN + IN subquery with distinctOn + orderBy
    String innerSql = "select t0.id from ad t0"
      + " LEFT JOIN price_range ON price_range.ad_id = t0.id"
      + " where t0.id in (select distinct on (t0.rebate) t0.id from ad t0 order by t0.rebate)";

    String countSql = simulateCountWrap(innerSql);

    // The subquery's closing ) must be preserved
    assertThat(countSql).contains("order by t0.rebate)");
    // Parentheses must be balanced
    assertThat(countChar(countSql, '(')).isEqualTo(countChar(countSql, ')'));
    // Should end with ") as c" - the outer count wrapper's closing paren
    assertThat(countSql).endsWith(") as c");
  }

  @Test
  void countWrap_topLevelOrderByIsStripped() {
    // When there IS a top-level order by, it should be stripped
    String innerSql = "select t0.id from ad t0"
      + " LEFT JOIN price_range ON price_range.ad_id = t0.id"
      + " where t0.id in (select distinct on (t0.rebate) t0.id from ad t0 order by t0.rebate)"
      + " order by price_range.discounted_price";

    String countSql = simulateCountWrap(innerSql);

    // Top-level order by should be removed
    assertThat(countSql).doesNotContain("discounted_price");
    // But inner subquery order by must remain intact
    assertThat(countSql).contains("order by t0.rebate)");
    // Parentheses must be balanced
    assertThat(countChar(countSql, '(')).isEqualTo(countChar(countSql, ')'));
  }

  @Test
  void countWrap_simpleOrderByIsStripped() {
    // Simple case: top-level order by with no subquery
    String innerSql = "select t0.id from ad t0 order by t0.id";

    String countSql = simulateCountWrap(innerSql);

    assertThat(countSql).isEqualTo("select count(*) from ( select t0.id from ad t0) as c");
  }

  @Test
  void wrapSelectExists_default_usesScalarExists() {
    String sql = CQueryBuilder.wrapSelectExists("select 1 from o_order t0 where t0.id > ?", false, "");
    assertThat(sql).isEqualTo("select exists(select 1 from o_order t0 where t0.id > ?)");
  }

  /**
   * SQL Server does not support exists(...) as a directly selectable scalar
   * boolean expression - see https://github.com/ebean-orm/ebean/issues/3848
   */
  @Test
  void wrapSelectExists_existsWithCaseWhen_wrapsAsCaseWhen() {
    String sql = CQueryBuilder.wrapSelectExists("select 1 from o_order t0 where t0.id > ?", true, "");
    assertThat(sql).isEqualTo("select case when exists(select 1 from o_order t0 where t0.id > ?) then 1 else 0 end");
  }

  /**
   * Oracle also requires a FROM clause on every select (from dual) - see https://github.com/ebean-orm/ebean/issues/3848
   */
  @Test
  void wrapSelectExists_existsWithCaseWhenAndFromClause_appendsFromClause() {
    String sql = CQueryBuilder.wrapSelectExists("select 1 from o_order t0 where t0.id > ?", true, " from dual");
    assertThat(sql).isEqualTo("select case when exists(select 1 from o_order t0 where t0.id > ?) then 1 else 0 end from dual");
  }

  @Test
  void inlineSqlCommentLabel_rootExplicitLabel_prefixesBeanType() {
    String label = CQueryBuilder.inlineSqlCommentLabel("fetchMachineFleets", null, false, "COrganisationMachine");
    assertThat(label).isEqualTo("COrganisationMachine.fetchMachineFleets");
  }

  @Test
  void inlineSqlCommentLabel_rootExplicitLabel_avoidsDuplicatePrefix() {
    String label = CQueryBuilder.inlineSqlCommentLabel("COrganisationMachine.fetchMachineFleets", null, false, "COrganisationMachine");
    assertThat(label).isEqualTo("COrganisationMachine.fetchMachineFleets");
  }

  @Test
  void inlineSqlCommentLabel_secondaryLabel_usedAsIs() {
    String label = CQueryBuilder.inlineSqlCommentLabel("COrganisationMachine.fetchMachineFleets.contacts.lazy", null, true, "Contact");
    assertThat(label).isEqualTo("COrganisationMachine.fetchMachineFleets.contacts.lazy");
  }

  @Test
  void inlineSqlCommentLabel_profileLocationFallback() {
    String label = CQueryBuilder.inlineSqlCommentLabel(null, profileLocation("Finder.byName"), false, "COrganisationMachine");
    assertThat(label).isEqualTo("Finder.byName");
  }

  private static ProfileLocation profileLocation(String label) {
    return new ProfileLocation() {
      @Override
      public boolean obtain() {
        return false;
      }

      @Override
      public String location() {
        return label;
      }

      @Override
      public String label() {
        return label;
      }

      @Override
      public String fullLocation() {
        return label;
      }

      @Override
      public void add(long executionTime) {
      }

      @Override
      public boolean trace() {
        return false;
      }

      @Override
      public void setTraceCount(int traceCount) {
      }
    };
  }
}
