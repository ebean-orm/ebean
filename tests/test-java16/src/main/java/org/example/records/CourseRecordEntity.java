package org.example.records;

import io.ebean.annotation.Identity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Identity(start = 1000)
@Entity
@Table(name="course_rec")
public record CourseRecordEntity(@Id long id, String name, String notes) {
}
