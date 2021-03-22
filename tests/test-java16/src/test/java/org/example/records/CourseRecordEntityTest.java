package org.example.records;

import io.ebean.DB;
import org.example.records.query.QCourseRecordEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseRecordEntityTest {

  @Test
  void record_insert_withGivenIdValue() {
    var course = new CourseRecordEntity(42, "SuppliedId", "Record");
    DB.save(course);
    assertThat(course.id()).isEqualTo(42);

    var found = new QCourseRecordEntity()
      .name.startsWith("SuppliedId")
      .findOneOrEmpty();

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(42L);
    assertThat(found.get().notes()).isEqualTo("Record");
  }

  @Test
  void record_insert_usingGeneratedId() {
    var course2 = new CourseRecordEntity(0, "Second", "Record with generated id");
    DB.save(course2);
    // as using @Identity(start = 1000)
    assertThat(course2.id()).isEqualTo(1000);

    var second = new QCourseRecordEntity()
      .name.startsWith("Second")
      .findOneOrEmpty();

    assertThat(second).isPresent();
    assertThat(second.get().notes()).isEqualTo("Record with generated id");
  }
}
