package org.tests.model.aggregation;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

@Entity
public class DMachine extends Model {

  @Id
  private long id;

  private String name;

  @ManyToOne
  private final DOrg organisation;

  @Version
  private long version;

  @OneToMany(mappedBy = "machine")
  private List<DMachineStats> machineStats;

  @OneToMany(mappedBy = "machine")
  private List<DMachineAuxUseAgg> auxUseAggs;

  public DMachine(DOrg organisation, String name) {
    this.organisation = organisation;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DOrg getOrganisation() {
    return organisation;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<DMachineStats> getMachineStats() {
    return machineStats;
  }

  public void setMachineStats(List<DMachineStats> machineStats) {
    this.machineStats = machineStats;
  }

  public List<DMachineAuxUseAgg> getAuxUseAggs() {
    return auxUseAggs;
  }

  public void setAuxUseAggs(List<DMachineAuxUseAgg> auxUseAggs) {
    this.auxUseAggs = auxUseAggs;
  }
}
