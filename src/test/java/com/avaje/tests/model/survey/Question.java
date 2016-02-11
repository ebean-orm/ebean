package com.avaje.tests.model.survey;

import javax.persistence.*;

@Entity
public class Question {
	@Id
	public Long id;

	@ManyToOne
    @JoinColumn(name = "groupObjectId")
    private Group group;

    private int sequenceNumber;

    public void setSequenceNumber(int number) {
        this.sequenceNumber = number;
    }

}