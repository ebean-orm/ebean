package org.example.records;

import io.ebean.DB;
import org.example.records.query.QCourse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.records.query.QCourse.Alias.id;
import static org.example.records.query.QCourse.Alias.name;

class DtoQueryUsingRecordsTest {

  @Test
  void dtoQuery_projectRecords() {

    var course = new Course("Calculus");
    course.setSummary("Something here");
    course.save();

    performOrmQuery();

    records_via_SqlDtoQuery(course);
    records_via_OrmQueryToDtoQuery(course);

    course.delete();
  }

  private void performOrmQuery() {
    List<Course> courses = new QCourse()
      .name.startsWith("Calc")
      .findList();

    assertThat(courses).hasSize(1);
  }

  private void records_via_SqlDtoQuery(Course course) {
    List<Foo> records = DB.findDto(Foo.class, "select id, name from course where name like ?")
      .setParameter("Calc%")
      .findList();

    assertThat(records).hasSize(1);
    assertThat(records.get(0).name()).isEqualTo(course.getName());
  }

  private void records_via_OrmQueryToDtoQuery(Course course) {
    List<Foo> records2 = new QCourse()
      .select(id, name )
      .name.startsWith("Calc")
      .asDto(Foo.class)
      .findList();

    assertThat(records2).hasSize(1);
    assertThat(records2.get(0).name()).isEqualTo(course.getName());
  }

  public record Foo(long id, String name){}
}
