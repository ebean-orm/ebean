package com.avaje.ebeaninternal.server.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebean.common.BeanSet;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Creates the BeanCollections.
 * <p>
 * Creates the BeanSet BeanMap and BeanList objects.
 * </p>
 */
public class BeanCollectionFactory {

	private static class BeanCollectionFactoryHolder {
		private static BeanCollectionFactory me = new BeanCollectionFactory();
	}
	
    private static final int defaultListInitialCapacity = 20;
    private static final int defaultSetInitialCapacity = 32;
    private static final int defaultMapInitialCapacity = 32;

    private BeanCollectionFactory() {

    }

    /**
     * Create a BeanCollection for the given parameters.
     */
    public static BeanCollection<?> create(BeanCollectionParams params) {
        return BeanCollectionFactoryHolder.me.createMany(params);
    }

    
    private BeanCollection<?> createMany(BeanCollectionParams params) {

    	SpiQuery.Type manyType = params.getManyType();
        switch (manyType) {
		case MAP:
			return createMap(params);
		case LIST:
			return createList(params);
		case SET:
			return createSet(params);
			
		default:
			 throw new RuntimeException("Invalid Arg " + manyType);
		}
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private BeanMap createMap(BeanCollectionParams params) {
        
        return new BeanMap(new LinkedHashMap(defaultMapInitialCapacity));       
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private BeanSet createSet(BeanCollectionParams params) {

        return new BeanSet(new LinkedHashSet(defaultSetInitialCapacity));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private BeanList createList(BeanCollectionParams params) {
        
        return new BeanList(new ArrayList(defaultListInitialCapacity));
    }
}
