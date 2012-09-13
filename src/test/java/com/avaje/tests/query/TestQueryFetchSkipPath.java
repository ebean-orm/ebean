package com.avaje.tests.query;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.ContactNote;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFetchSkipPath extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<Order> list = Ebean.find(Order.class)
        
         //.setAutofetch(true)
            .setAutofetch(false)
            .select("status")
//            .fetch("customer","id")
//            .fetch("customer.contacts","id")
            .fetch("customer.contacts.notes","title")
            .findList();
        
        for (Order order : list) {
            order.getStatus();
            Customer customer = order.getCustomer();
            List<Contact> contacts = customer.getContacts();
            for (Contact contact : contacts) {
                System.out.println("contact:"+contact);
                List<ContactNote> notes = contact.getNotes();
                for (ContactNote contactNote : notes) {
                    System.out.println("note:"+contactNote.getTitle());
                }
            }
        }
        
        
    }
    
}
