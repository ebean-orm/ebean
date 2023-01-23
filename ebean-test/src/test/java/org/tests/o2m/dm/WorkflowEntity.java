package org.tests.o2m.dm;

import io.ebean.annotation.SoftDelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WorkflowEntity extends HistoryColumns {
  private String revision;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "workflow_id")
  private List<WorkflowOperationEntity> operations = new ArrayList<>();

  @SoftDelete
  private boolean deleted;

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public List<WorkflowOperationEntity> getOperations() {
    return operations;
  }

  public void setOperations(List<WorkflowOperationEntity> operations) {
    this.operations = operations;
  }
}
