package com.avaje.tests.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTransientInternalFields extends TestCase {

    public void test() {
        
        ResetBasicData.reset();
        
        List<Customer> list = Ebean.find(Customer.class).findList();
        
        Customer c = list.get(0);
        
        Object back = serialWriteRead(c, false);
        
        if (back instanceof EntityBean){
            EntityBean entityBean = (EntityBean)back;
            EntityBeanIntercept ebi = entityBean._ebean_getIntercept();
            if (ebi == null){
                ebi = entityBean._ebean_intercept();
                Assert.assertNotNull(ebi);
            }
        }
        
        File serTestFile = new File("serTransTest");
        if (serTestFile.exists()){
            serTestFile.delete();
        }
    }
    
    private Object serialWriteRead(Object inputObject, boolean vanilla){

        try {

            File serTestFile = new File("serTransTest");
            FileOutputStream fout = new FileOutputStream(serTestFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            
            oos.writeObject(inputObject);
            oos.close();

            FileInputStream fin = new FileInputStream(serTestFile);
            
            ObjectInputStream ois;
            if (vanilla){
                ois = new ObjectInputStream(fin);
            } else {
                ois = Ebean.getServer(null).createProxyObjectInputStream(fin);
            }
            Object readObject = ois.readObject();
            ois.close();
            return readObject;
            
        } catch (Exception e){
            e.printStackTrace();
            Assert.assertTrue(false);
            return null;
        }

    }
}
