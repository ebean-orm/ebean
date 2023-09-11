package org.tests.iud;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class PcfCalendar extends PcfModel {

  @OneToMany(cascade = ALL, orphanRemoval = true)
  private List<PcfEvent> events = new ArrayList<>();

  public void addEvent(PcfEvent event) {
    events.add(event);
  }
}
