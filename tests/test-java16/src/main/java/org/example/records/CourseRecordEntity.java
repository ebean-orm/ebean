package org.example.records;

import io.ebean.annotation.Identity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Identity(start = 1000)
@Entity
@Table(name="course_rec")
public record CourseRecordEntity(@Id long id, String name, String notes) {
}
