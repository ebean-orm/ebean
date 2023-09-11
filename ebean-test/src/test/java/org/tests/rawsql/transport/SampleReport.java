package org.tests.rawsql.transport;

import jakarta.persistence.Entity;
import java.sql.Date;

@Entity
public class SampleReport {
  Long id;
  String name;
  Date anniversary;
  String city;
}
