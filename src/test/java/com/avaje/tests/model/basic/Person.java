package com.avaje.tests.model.basic;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "Person")
@Table(name = "PERSONS")
public class Person implements Serializable {

    private static final long serialVersionUID = 495045977245770183L;
    
    private Long id;
    private String surname;
    private String name;
    private List<Phone> phones;

    public Person() {
    }

    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "SURNAME", nullable = false, unique = false, columnDefinition = "varchar(64)")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Column(name = "NAME", nullable = false, unique = false, columnDefinition = "varchar(64)")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(targetEntity = Phone.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "person")
    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

}