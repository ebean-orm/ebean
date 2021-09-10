package org.tests.iud;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class PcfCalendar extends PcfModel {

  @OneToMany(cascade = ALL, orphanRemoval = true)
  private List<PcfEvent> events = new ArrayList<>();

  public void addEvent(PcfEvent event) {
    events.add(event);
  }
}
