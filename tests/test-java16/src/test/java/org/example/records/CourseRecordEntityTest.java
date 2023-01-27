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
    CourseRecordEntity courseRecord = found.get();
    assertThat(courseRecord.id()).isEqualTo(42L);
    assertThat(courseRecord.notes()).isEqualTo("Record");
    assertThat(courseRecord).isEqualTo(courseRecord);
    assertThat(courseRecord.toString()).isEqualTo("CourseRecordEntity[id=42, name=SuppliedId, notes=Record]");
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
    CourseRecordEntity courseRecord = second.get();
    assertThat(courseRecord.notes()).isEqualTo("Record with generated id");

    assertThat(courseRecord).isEqualTo(new CourseRecordEntity(1000, "Second", "Record with generated id"));
    assertThat(courseRecord.toString()).isEqualTo("CourseRecordEntity[id=1000, name=Second, notes=Record with generated id]");
  }
}
