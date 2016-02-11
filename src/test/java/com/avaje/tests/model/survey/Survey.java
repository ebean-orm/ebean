package com.avaje.tests.model.survey;

import javax.persistence.*;
import java.util.*;

@Entity
public class Survey {
	@Id
	public Long id;

	@OneToMany(mappedBy = "survey", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("sequenceNumber")
    private List<Category> categories;

    public List<Category> getCategories() {
    	return categories;
    }

    public void setCategories(List<Category> categories) {
    	this.categories = categories;
    }
}