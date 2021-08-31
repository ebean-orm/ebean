package org.querytest;

import io.ebean.DB;
import io.ebean.Database;
import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.Test;
import org.junit.Assert;

import java.util.List;

public class QCustomerByteArrayTest {
  @Test
  public void testIsNull() {
    final byte[] photo1= new byte[] {  0x05, 0x10, 0x20, 0x30 };
    final byte[] photo2=new byte[] { 0x50, 0x55, 0x60, 0x70, 0x10 };
    final Database db=DB.getDefault();
    final String noPhoto="no_photo_client";
    final String wPhoto1="with_photo1_client";
    final String wPhoto2="with_photo2_client";

    Customer c;

    c=new Customer();
    c.setName(noPhoto);
    c.save();

    c=new Customer();
    c.setName(wPhoto1);
    c.setPhoto(photo1);
    c.save();

    c=new Customer();
    c.setName(wPhoto2);
    c.setPhoto(photo2);
    c.save();

    Assert.assertNotNull(new QCustomer(db).name.eq(noPhoto).photo.isNull().findOne());
    //    Assert.assertNotNull(new QCustomer(db).name.eq(noPhoto).photo.ne(photo1).findOne()); // Fails
    Assert.assertNull(new QCustomer(db).name.eq(noPhoto).photo.isNotNull().findOne());
    Assert.assertNull(new QCustomer(db).name.eq(noPhoto).photo.eq(photo1).findOne());


    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto1).photo.isNotNull().findOne());
    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto1).photo.eq(photo1).findOne());
    Assert.assertNotNull(new QCustomer(db).photo.eq(photo1).findOne());
    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto1).photo.ne(photo2).findOne());

    Assert.assertNull(new QCustomer(db).name.eq(wPhoto1).photo.isNull().findOne());
    Assert.assertNull(new QCustomer(db).name.eq(wPhoto1).photo.eq(photo2).findOne());
    Assert.assertNull(new QCustomer(db).name.eq(wPhoto1).photo.ne(photo1).findOne());

    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto2).photo.isNotNull().findOne());
    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto2).photo.eq(photo2).findOne());
    Assert.assertNotNull(new QCustomer(db).photo.eq(photo2).findOne());
    Assert.assertNotNull(new QCustomer(db).name.eq(wPhoto2).photo.ne(photo1).findOne());

    Assert.assertNull(new QCustomer(db).name.eq(wPhoto2).photo.isNull().findOne());
    Assert.assertNull(new QCustomer(db).name.eq(wPhoto2).photo.eq(photo1).findOne());
    Assert.assertNull(new QCustomer(db).name.eq(wPhoto2).photo.ne(photo2).findOne());

    Assert.assertEquals(2, new QCustomer(db).photo.in(photo1, photo2).findList().size());


    List<Customer> ordered = new QCustomer(db).name.in(wPhoto1, wPhoto2).photo.desc().findList();
    Assert.assertEquals(wPhoto2, ordered.get(0).getName());
    Assert.assertArrayEquals(photo2, ordered.get(0).getPhoto());
    Assert.assertEquals(wPhoto1, ordered.get(1).getName());
    Assert.assertArrayEquals(photo1, ordered.get(1).getPhoto());

    ordered = new QCustomer(db).name.in(wPhoto1, wPhoto2).photo.asc().findList();
    Assert.assertEquals(wPhoto2, ordered.get(1).getName());
    Assert.assertArrayEquals(photo2, ordered.get(1).getPhoto());
    Assert.assertEquals(wPhoto1, ordered.get(0).getName());
    Assert.assertArrayEquals(photo1, ordered.get(0).getPhoto());
  }
}
