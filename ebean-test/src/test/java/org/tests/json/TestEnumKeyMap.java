package org.tests.json;

import io.ebean.DB;
import io.ebean.annotation.DbEnumValue;
import io.ebean.annotation.DbJson;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for Map<Enum, Object> support in @DbJson fields.
 */
class TestEnumKeyMap extends BaseTestCase {

  enum Status {
    ACTIVE,
    INACTIVE,
    PENDING;

    @DbEnumValue
    public String getValue() {
      return name().substring(0, 1); // A, I, P
    }
  }

  enum Priority {
    LOW,
    MEDIUM,
    HIGH
  }

  @Entity
  @Table(name = "enum_map_test")
  public static class EnumMapBean {
    @Id
    Long id;

    @DbJson
    Map<Status, Object> statusData;

    @DbJson
    Map<Priority, String> priorityLabels;

    public EnumMapBean(Long id) {
      this.id = id;
    }

    public EnumMapBean() {
    }
  }

  @Test
  void testEnumKeyMapWithCustomEnumValue() {
    EnumMapBean bean = new EnumMapBean(1L);

    Map<Status, Object> statusData = new LinkedHashMap<>();
    statusData.put(Status.ACTIVE, "Running smoothly");
    statusData.put(Status.PENDING, 42L);
    statusData.put(Status.INACTIVE, Map.of("reason", "maintenance"));

    bean.statusData = statusData;

    DB.save(bean);

    // Read it back
    EnumMapBean found = DB.find(EnumMapBean.class, 1L);
    assertThat(found).isNotNull();
    assertThat(found.statusData).hasSize(3);
    System.out.println(found.statusData);
    assertThat(found.statusData.get(Status.ACTIVE)).isEqualTo("Running smoothly");
    assertThat(found.statusData.get(Status.PENDING)).isEqualTo(42L);
    assertThat(found.statusData.get(Status.INACTIVE)).isInstanceOf(Map.class);

    // Update
    found.statusData.put(Status.ACTIVE, "Updated status");
    found.statusData.remove(Status.INACTIVE);
    DB.save(found);

    EnumMapBean updated = DB.find(EnumMapBean.class, 1L);
    assertNotNull(updated);
    assertThat(updated.statusData).hasSize(2);
    assertThat(updated.statusData.get(Status.ACTIVE)).isEqualTo("Updated status");
    assertThat(updated.statusData).doesNotContainKey(Status.INACTIVE);
  }

  @Test
  void testEnumKeyMapWithStandardEnum() {
    EnumMapBean bean = new EnumMapBean(2L);

    Map<Priority, String> priorityLabels = new LinkedHashMap<>();
    priorityLabels.put(Priority.LOW, "Not urgent");
    priorityLabels.put(Priority.MEDIUM, "Standard processing");
    priorityLabels.put(Priority.HIGH, "Urgent - immediate action required");

    bean.priorityLabels = priorityLabels;

    DB.save(bean);

    EnumMapBean found = DB.find(EnumMapBean.class, 2L);
    assertThat(found).isNotNull();
    assertThat(found.priorityLabels).hasSize(3);
    assertThat(found.priorityLabels.get(Priority.LOW)).isEqualTo("Not urgent");
    assertThat(found.priorityLabels.get(Priority.HIGH)).isEqualTo("Urgent - immediate action required");
  }

  @Test
  void testNullAndEmptyMaps() {
    EnumMapBean bean = new EnumMapBean(3L);
    bean.statusData = null;
    bean.priorityLabels = new LinkedHashMap<>();

    DB.save(bean);

    EnumMapBean found = DB.find(EnumMapBean.class, 3L);
    assertThat(found).isNotNull();
    assertThat(found.statusData).isNull();
    assertThat(found.priorityLabels).isEmpty();
  }
}
