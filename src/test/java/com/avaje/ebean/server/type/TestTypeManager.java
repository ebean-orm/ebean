/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebean.server.type;

import java.sql.Types;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebeaninternal.server.core.BootupClasses;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.DefaultTypeManager;
import com.avaje.ebeaninternal.server.type.ScalarDataReader;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Money;

public class TestTypeManager extends TestCase {

    public void test() {
        
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setDatabasePlatform(new H2Platform());
        
        BootupClasses bootupClasses = new BootupClasses();
        
        DefaultTypeManager typeManager = new DefaultTypeManager(serverConfig, bootupClasses);
    
        CheckImmutableResponse checkImmutable = typeManager.checkImmutable(Money.class);
        Assert.assertTrue(checkImmutable.isImmutable());

        checkImmutable = typeManager.checkImmutable(CMoney.class);        
        Assert.assertTrue(checkImmutable.isImmutable());

        ScalarDataReader<?> dataReader = typeManager.recursiveCreateScalarDataReader(ExhangeCMoneyRate.class);
        Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

        dataReader = typeManager.recursiveCreateScalarDataReader(CMoney.class);
        Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

        ScalarType<?> scalarType = typeManager.recursiveCreateScalarTypes(Money.class);
        Assert.assertTrue(scalarType.getJdbcType() == Types.DECIMAL);
        Assert.assertTrue(!scalarType.isJdbcNative());
        Assert.assertEquals(Money.class, scalarType.getType());
        
        
    }
    
}
