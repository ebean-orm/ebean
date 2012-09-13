package com.avaje.tests.compositekeys;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.composite.RCustomer;
import com.avaje.tests.model.composite.RCustomerKey;
import com.avaje.tests.model.composite.ROrder;
import com.avaje.tests.model.composite.ROrderPK;

public class CreateIdExpandedFormServer {

    public static EbeanServer create() {
        
        
        ServerConfig config = new ServerConfig();
        config.setName("h2");
        config.loadFromProperties();
        config.setName("modifiedH2");
        
        config.addClass(ROrder.class);
        config.addClass(ROrderPK.class);
        config.addClass(RCustomer.class);
        config.addClass(RCustomerKey.class);
        
        config.setDatabasePlatform(new ModifiedH2Platform());
        
        return EbeanServerFactory.create(config);   
    }
}
