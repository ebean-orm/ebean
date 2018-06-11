package io.ebean;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoQueryFromOrmTest extends BaseTestCase {

  @BeforeClass
  public static void resetStats() {
    Ebean.getDefaultServer().getMetaInfoManager().resetAllMetrics();
  }

  @AfterClass
  public static void reportStats() {
    BasicMetricVisitor basic = Ebean.getDefaultServer().getMetaInfoManager().visitBasic();
    for (MetaQueryMetric metric : basic.getDtoQueryMetrics()) {
      System.out.println(metric);
    }

    System.out.println("-- transaction metrics --");
    for (MetaTimedMetric metric : basic.getTimedMetrics()) {
      System.out.println(metric);
    }
  }

  @Ignore
  @Test
  public void testPlanHits() {

    ResetBasicData.reset();

    resetAllMetrics();

    String[] prefix = {"Bl", "B", "Red", "jim"};

    for (String val : prefix) {
      List<ContactDto> list = Ebean.find(Contact.class)
        .select("email, concat(lastName,', ',firstName) as fullName")
        .where().istartsWith("concat(lastName,', ',firstName)", val)
        .orderBy().asc("lastName")
        .setMaxRows(10)
        .asDto(ContactDto.class)
        .setLabel("prefixLoop")
        .findList();

      System.out.println("List:" + list);
    }

    BasicMetricVisitor basic = visitMetricsBasic();

    List<MetaQueryMetric> stats = basic.getDtoQueryMetrics();
    for (MetaQueryMetric stat : stats) {
      System.out.println(stat);
    }

    assertThat(stats).hasSize(1);
    assertThat(stats.get(0).getCount()).isEqualTo(3);

  }

  @Test
  public void asDto_withExplicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    DtoQuery<ContactDto> query =
      Ebean.find(Contact.class)
        // we must explicitly add the id property for DTO query (if we want it)
        .select("id, email, concat(lastName,', ',firstName) as fullName")
        .where().isNotNull("email").isNotNull("lastName")
        .orderBy().asc("lastName")
        .asDto(ContactDto.class)
        .setLabel("explicitId")
        .setRelaxedMode();

    List<ContactDto> dtos = query.findList();

    assertThat(dtos).isNotEmpty();

    for (ContactDto dto : dtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
      System.out.println(dto);
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.id, t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");
  }

  @Test
  public void asDto_withoutExplicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    DtoQuery<ContactDto> query =
      Ebean.find(Contact.class)
        .select("email, concat(lastName,', ',firstName) as fullName")
        .where().isNotNull("email").isNotNull("lastName")
        .orderBy().asc("lastName")
        .asDto(ContactDto.class);

    List<ContactDto> dtos = query.findList();

    assertThat(dtos).isNotEmpty();

    for (ContactDto dto : dtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
      System.out.println(dto);
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");
  }


  @Test
  public void example() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos
      = Ebean.find(Contact.class)
      .setLabel("emailFullName")
      .select("email, concat(lastName,', ',firstName) as fullName")
      .where().isNotNull("email").isNotNull("lastName")
      .orderBy().asc("lastName")
      .setMaxRows(10)
      .asDto(ContactDto.class)
      .findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
      System.out.println(dto);
    }

    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select top 10 t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");

    } else {
      assertThat(sql.get(0)).contains("select t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");
    }
  }

  @Test
  public void example_explicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos
      = Ebean.find(Contact.class)
      .select("id, email, concat(lastName,', ',firstName) as fullName")
      .where().isNotNull("email").isNotNull("lastName")
      .orderBy().asc("lastName")
      .setMaxRows(10)
      .asDto(ContactDto.class)
      .findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      System.out.println(dto);
      assertThat(dto.getId()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
      assertThat(dto.getEmail()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select top 10 t0.id, t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");
    } else {
      assertThat(sql.get(0)).contains("select t0.id, t0.email, concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where t0.email is not null  and t0.last_name is not null  order by t0.last_name");
    }
  }

  @Test
  public void example_singleProperty() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos
      = Ebean.find(Contact.class)
      .select("concat(lastName,', ',firstName) as fullName")
      .where().isNotNull("lastName")
      .orderBy().asc("lastName")
      .asDto(ContactDto.class)
      .setFirstRow(2)
      .setMaxRows(5)
      .findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      System.out.println(dto);
      assertThat(dto.getFullName()).isNotNull();
      assertThat(dto.getId()).isNull();
      assertThat(dto.getEmail()).isNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select concat(t0.last_name,', ',t0.first_name) fullName from contact t0 where");
  }


  @Test
  public void example_aggregate() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactTotals> contactDtos
      = Ebean.find(Contact.class)
      .select("lastName, count(*) as totalCount")
      .where().isNotNull("lastName")
      .having().gt("count(*)", 1)
      .orderBy().desc("count(*)")
      .asDto(ContactTotals.class)
      .findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactTotals dto : contactDtos) {
      assertThat(dto.getLastName()).isNotNull();
      assertThat(dto.getTotalCount()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.last_name, count(*) totalCount from contact t0 where t0.last_name is not null  group by t0.last_name having count(*) > ?");
  }

  @Test
  public void toDto_fromExpressionList() {

    ResetBasicData.reset();

    List<ContactTotals> contactDtos
      = Ebean.find(Contact.class)
      .select("lastName, count(*) as totalCount")
      .where().isNotNull("lastName")
      .asDto(ContactTotals.class)
      .findList();

    assertThat(contactDtos).isNotEmpty();
  }

  public static class ContactTotals {

    String lastName;
    Long totalCount;

    @Override
    public String toString() {
      return "lastName:" + lastName + " total:" + totalCount;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public Long getTotalCount() {
      return totalCount;
    }

    public void setTotalCount(Long totalCount) {
      this.totalCount = totalCount;
    }
  }

  public static class ContactDto {

    String email;
    String fullName;
    Integer id;

    @Override
    public String toString() {
      return "id:" + id + " email:" + email + " fn:" + fullName;
    }

    public String getEmail() {
      return email;
    }

    public String getFullName() {
      return fullName;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }
  }
}
