package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.avaje.ebean.annotation.CacheStrategy;

@CacheStrategy(useBeanCache=true)
@Entity
public class Section extends BasicDomain {

    private static final long serialVersionUID = 1L;

    public enum Type {
        GENERAL,
        NOTE
    }
    
    @ManyToOne
    Article article;
    
    Type type = Type.GENERAL;
    
    @Lob
    String content;

    public Section() {
    }

    public Section(String content) {
        this.content = content;
    }
    
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
    
}
