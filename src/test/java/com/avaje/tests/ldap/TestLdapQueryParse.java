package com.avaje.tests.ldap;

import java.util.Arrays;

import junit.framework.Assert;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.ldap.LdapOrmQueryRequest;
import com.avaje.ebeaninternal.server.ldap.LdapQueryDeployHelper;
import com.avaje.tests.model.ldap.LDPerson;

public class TestLdapQueryParse extends BaseLdapTest{

    public void test() {
        
        boolean b = true;
        if (b){
            // turn this test off for the moment
            return;
        }

        EbeanServer server = createServer();
        
        Query<LDPerson> query = server.createQuery(LDPerson.class);
        query.select("uid, cn, status");
        query.where("(cn=rob*)");
        query.where()
            .raw("(inetUserStatus=Banana)")
            .eq("status", LDPerson.Status.ACTIVE)
            .eq("sn", "bana");
        
        SpiQuery<LDPerson> sq = (SpiQuery<LDPerson>)query;
        
        SpiEbeanServer spiServer = (SpiEbeanServer)server;
        BeanDescriptor<LDPerson> desc = spiServer.getBeanDescriptor(LDPerson.class);
        LdapOrmQueryRequest<LDPerson> req =  new LdapOrmQueryRequest<LDPerson>(sq, desc, null);

        LdapQueryDeployHelper deployHelper = new LdapQueryDeployHelper(req);
        
        String[] sp = deployHelper.getSelectedProperties();
        String filterExpr = deployHelper.getFilterExpr();
        Object[] filterVals = deployHelper.getFilterValues();
        
        System.out.println("filterExpr: "+filterExpr);
        System.out.println("filterVals: "+Arrays.toString(filterVals));
        Assert.assertNotNull(sp);
        
    }
}
