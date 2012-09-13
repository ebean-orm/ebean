package com.avaje.tests.model.basic;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;

public class DMLTest extends TestCase {

    public DMLTest() {
    }

    public static void main(String[] args) {
        DMLTest dmlTest = new DMLTest();
        dmlTest.deleteAll();
        dmlTest.insertData();
    }

    public void test() {
        Assert.assertTrue(true);
    }
    
    public void deleteAll() {
        Ebean.beginTransaction();
        try {
            Ebean.createUpdate(Phone.class, "delete from phone").execute();

            Ebean.createUpdate(Person.class, "delete from person").execute();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }
    }

    private void insertData() {
        Person person = new Person();
        person.setSurname("Kosinov");
        person.setName("Yaroslav");
        Phone phone = new Phone();
        phone.setPhoneNumber("5244011");
        List<Phone> phones = new ArrayList<Phone>();
        phones.add(phone);
        person.setPhones(phones);
        Ebean.save(person);

        person = new Person();
        person.setSurname("Kosinov");
        person.setName("Gennady");
        phone = new Phone();
        phone.setPhoneNumber("5712658");
        phone.setPerson(person);
        Ebean.save(phone);

    }

}
