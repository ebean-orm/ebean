package org.tests.iud;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class PcfPerson extends PcfModel {

  private final String name;

  @OneToMany(cascade = ALL, orphanRemoval = true)
  private List<PcfCalendar> calendars = new ArrayList<>();

  public PcfPerson(String name) {
    this.name = name;
  }

  public void addCalendar(PcfCalendar calendar) {
    calendars.add(calendar);
  }

}
