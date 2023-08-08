package org.tests.o2m.dm;

import javax.persistence.*;
import java.util.List;

@Entity
public class GoodsEntity extends HistoryColumns {
  private String name;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private WorkflowEntity workflowEntity;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WorkflowEntity getWorkflowEntity() {
    return workflowEntity;
  }

  public void setWorkflowEntity(WorkflowEntity workflowEntity) {
    this.workflowEntity = workflowEntity;
  }

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }
}
