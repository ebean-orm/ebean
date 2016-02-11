package com.avaje.tests.model.survey;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="survey_group")
public class Group {
	@Id
	public Long id;

	@ManyToOne
    @JoinColumn(name = "categoryObjectId")
    private Category category;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("sequenceNumber")
    private List<Question> questions = new ArrayList<Question>();

    private int sequenceNumber;

    public List<Question> getQuestions() {
    	return questions;
    }

    public void setQuestions(List<Question> questions) {
    	this.questions = questions;
    }

        public void setSequenceNumber(int number) {
        this.sequenceNumber = number;
    }
}