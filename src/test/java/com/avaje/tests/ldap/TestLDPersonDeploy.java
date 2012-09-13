package com.avaje.tests.ldap;

import java.util.Set;

import javax.naming.directory.Attribute;

import junit.framework.Assert;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertySimpleCollection;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor.EntityType;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeLdapTimestamp;
import com.avaje.tests.model.ldap.LDPerson;


public class TestLDPersonDeploy extends BaseLdapTest {

    public void test() {
        
        boolean b = true;
        if (b){
            // turn this test off for the moment
            return;
        }

        
        GlobalProperties.put("ebean.classes", LDPerson.class.toString());
        
        EbeanServer server = createServer();
        SpiEbeanServer spiServer = (SpiEbeanServer)server;
        
        BeanDescriptor<LDPerson> descriptor = spiServer.getBeanDescriptor(LDPerson.class);
        Assert.assertTrue(EntityType.LDAP.equals(descriptor.getEntityType()));
        
        BeanProperty beanProperty = descriptor.getBeanProperty("modifiedTime");
        
        Assert.assertEquals("modifiedTime", beanProperty.getName());
        Assert.assertEquals("modifiedTime", beanProperty.getDbColumn());
        
        ScalarType<?> scalarType = beanProperty.getScalarType();
        Assert.assertTrue(scalarType instanceof ScalarTypeLdapTimestamp<?>);
        
        BeanProperty accountsProp = descriptor.getBeanProperty("accounts");
        Assert.assertTrue(accountsProp instanceof BeanPropertySimpleCollection<?>);
        
        LDPerson person = new LDPerson();
        person.addAccount(1001);
        person.addAccount(1002);
        person.addAccount(1003);
        
        Attribute acctAttribute = accountsProp.createAttribute(person);
        Assert.assertTrue(acctAttribute.size() == 3);

        LDPerson newPerson = new LDPerson();

        accountsProp.setAttributeValue(newPerson, acctAttribute);
        Set<Long> accounts = newPerson.getAccounts();
        
        Assert.assertTrue(accounts.size() == 3);
        
    }
    
}
