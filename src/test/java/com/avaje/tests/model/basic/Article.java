package com.avaje.tests.model.basic;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.CacheStrategy;


@CacheStrategy(useBeanCache=true, warmingQuery="find article join sections")
@Entity
public class Article extends BasicDomain {

    private static final long serialVersionUID = 1L;

    String name;
    
    String author;
    
    @OneToMany(cascade=CascadeType.ALL)
    List<Section> sections;

    public Article() {
        
    }
    
    public Article(String name, String author) {
        this.name = name;
        this.author = author;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
    
    public void addSection(Section s){
        if (sections == null){
            sections = new ArrayList<Section>();
        }
        sections.add(s);
    }
    
}
