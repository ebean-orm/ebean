package org.tests.rawsql.transport;

import javax.persistence.Entity;
import java.sql.Date;

@Entity
public class SampleReport {
  Long id;
  String name;
  Date anniversary;
  String city;
}
