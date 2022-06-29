package org.tests.o2m.dm;

import org.tests.model.draftable.BaseDomain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class GoodsEntity extends BaseDomain {
    private String name;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkflowEntity workflowEntity;

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
}
