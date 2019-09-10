package io.ebean;

import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoQueryFromOrmTest extends BaseTestCase {

  @BeforeClass
  public static void resetStats() {
    DB.getDefault().getMetaInfoManager().resetAllMetrics();
  }

  @AfterClass
  public static void reportStats() {
    ServerMetrics metrics = DB.getDefault().getMetaInfoManager().collectMetrics();
    for (MetaQueryMetric metric : metrics.getDtoQueryMetrics()) {
      System.out.println(metric);
    }

    System.out.println("-- transaction metrics --");
    for (MetaTimedMetric metric : metrics.getTimedMetrics()) {
      System.out.println(metric);
    }
  }

  private static final ProfileLocation loc0 = ProfileLocation.create();

  @ForPlatform(Platform.H2)
  @Test
  public void testPlanHits() {

    ResetBasicData.reset();

    resetAllMetrics();

    String[] prefix = {"Bl", "B", "Red", "jim"};

    for (String val : prefix) {
      DB.find(Contact.class)
        .setProfileLocation(loc0)
        .select("email, " + concat("lastName", ", ", "firstName") + " as fullName").where()
        .istartsWith(concat("lastName", ", ", "firstName"), val).orderBy().asc("lastName").setMaxRows(10)
        .asDto(ContactDto.class).setLabel("prefixLoop").findList();
    }

    ServerMetrics metrics = collectMetrics();

    List<MetaQueryMetric> stats = metrics.getDtoQueryMetrics();
    for (MetaQueryMetric stat : stats) {
      long meanMicros = stat.getMean();
      assertThat(meanMicros).isLessThan(900_000);
      assertThat(stat.getProfileLocation()).isSameAs(loc0);
    }

    assertThat(stats).hasSize(1);
    assertThat(stats.get(0).getCount()).isEqualTo(4);
  }

  @ForPlatform(Platform.H2)
  @Test
  public void selectFormulaWith_bindPositionedParameters() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<Contact> list = DB.find(Contact.class)
      .select("email, concat(lastName, ISO_WEEK(?)) as lastName")
      .setMaxRows(10)
      .setParameter(1, OffsetDateTime.now())
      .findList();

    assertThat(list).isNotEmpty();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.email, concat(t0.last_name, ISO_WEEK(?)) lastName from contact");
  }

//  @ForPlatform(Platform.H2)
//  @Test
//  public void selectFormulaWith_bindNamedParameters_FAILS_namedParamsNotSupportedInSelectClause() {
//
//    ResetBasicData.reset();
//
//    LoggedSqlCollector.start();
//
//    List<Contact> list = DB.find(Contact.class)
//      .select("email, concat(lastName, ISO_WEEK(:date)) as lastName")
//      .setMaxRows(10)
//      .setParameter("date", OffsetDateTime.now())
//      .findList();
//
//
//    assertThat(list).isNotEmpty();
//
//    List<String> sql = LoggedSqlCollector.stop();
//    assertThat(sql).hasSize(2);
//    assertThat(sql.get(0)).contains("select t0.id, t0.email, concat(t0.last_name, ISO_WEEK(?)) lastName from contact");
//  }

  @Test
  public void asDto_withExplicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    DtoQuery<ContactDto> query = DB.find(Contact.class)
      // we must explicitly add the id property for DTO query (if we want it)
      .select("id, email, " + concat("lastName", ", ", "firstName") + " as fullName").where().isNotNull("email")
      .isNotNull("lastName").orderBy().asc("lastName").asDto(ContactDto.class).setLabel("explicitId")
      .setRelaxedMode();

    List<ContactDto> dtos = query.findList();

    assertThat(dtos).isNotEmpty();

    for (ContactDto dto : dtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.id, t0.email, " + concat("t0.last_name", ", ", "t0.first_name")
      + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");
  }

  @Test
  public void asDto_withoutExplicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    DtoQuery<ContactDto> query = DB.find(Contact.class)
      .select("email, " + concat("lastName", ", ", "firstName") + " as fullName").where().isNotNull("email")
      .isNotNull("lastName").orderBy().asc("lastName").asDto(ContactDto.class);

    List<ContactDto> dtos = query.findList();

    assertThat(dtos).isNotEmpty();

    for (ContactDto dto : dtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.email, " + concat("t0.last_name", ", ", "t0.first_name")
      + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");
  }

  @Test
  public void example() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos = DB.find(Contact.class).setLabel("emailFullName")
      .select("email, " + concat("lastName", ", ", "firstName") + " as fullName").where().isNotNull("email")
      .isNotNull("lastName").orderBy().asc("lastName").setMaxRows(10).asDto(ContactDto.class).findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      assertThat(dto.getEmail()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();

    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select top 10 t0.email, " + concat("t0.last_name", ", ", "t0.first_name")
        + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");

    } else {
      assertThat(sql.get(0)).contains("select t0.email, " + concat("t0.last_name", ", ", "t0.first_name")
        + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");
    }
  }

  @Test
  public void example_explicitId() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos = DB.find(Contact.class)
      .select("id, email, " + concat("lastName", ", ", "firstName") + " as fullName").where().isNotNull("email")
      .isNotNull("lastName").orderBy().asc("lastName").setMaxRows(10).asDto(ContactDto.class).findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      assertThat(dto.getId()).isNotNull();
      assertThat(dto.getFullName()).isNotNull();
      assertThat(dto.getEmail()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select top 10 t0.id, t0.email, "
        + concat("t0.last_name", ", ", "t0.first_name")
        + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");
    } else {
      assertThat(sql.get(0)).contains("select t0.id, t0.email, " + concat("t0.last_name", ", ", "t0.first_name")
        + " fullName from contact t0 where t0.email is not null and t0.last_name is not null order by t0.last_name");
    }
  }

  @Test
  public void example_singleProperty() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactDto> contactDtos = DB.find(Contact.class)
      .select(concat("lastName", ", ", "firstName") + " as fullName").where().isNotNull("lastName").orderBy()
      .asc("lastName").asDto(ContactDto.class).setFirstRow(2).setMaxRows(5).findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactDto dto : contactDtos) {
      assertThat(dto.getFullName()).isNotNull();
      assertThat(dto.getId()).isNull();
      assertThat(dto.getEmail()).isNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select " + concat("t0.last_name", ", ", "t0.first_name") + " fullName from contact t0 where");
  }

  @Test
  public void example_aggregate() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    List<ContactTotals> contactDtos = DB.find(Contact.class).select("lastName, count(*) as totalCount").where()
      .isNotNull("lastName").having().gt("count(*)", 1).orderBy().desc("count(*)").asDto(ContactTotals.class)
      .findList();

    assertThat(contactDtos).isNotEmpty();

    for (ContactTotals dto : contactDtos) {
      assertThat(dto.getLastName()).isNotNull();
      assertThat(dto.getTotalCount()).isNotNull();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select t0.last_name, count(*) totalCount from contact t0 where t0.last_name is not null group by t0.last_name having count(*) > ?");
  }

  @Test
  public void toDto_fromExpressionList() {

    ResetBasicData.reset();

    List<ContactTotals> contactDtos = DB.find(Contact.class).select("lastName, count(*) as totalCount").where()
      .isNotNull("lastName").asDto(ContactTotals.class).findList();

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
