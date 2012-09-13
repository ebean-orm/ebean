package com.avaje.ebeaninternal.server.query;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class LoadedPropertiesCache {

	static ConcurrentHashMap<Integer, Set<String>> cache = new ConcurrentHashMap<Integer, Set<String>>(250, 0.75f, 16);
	
	public static Set<String> get(int partialHash, Set<String> partialProps, BeanDescriptor<?> desc){
		
		int manyHash = desc.getNamesOfManyPropsHash();
		int totalHash = 37*partialHash + manyHash;
		
		Integer key = Integer.valueOf(totalHash);
		
		Set<String> includedProps = cache.get(key);
		
		if (includedProps == null){
			// its not in the cache so build it
			LinkedHashSet<String> mergeNames = new LinkedHashSet<String>();
			mergeNames.addAll(partialProps);
			if (manyHash != 0){
				mergeNames.addAll(desc.getNamesOfManyProps());
			}
			
			// we want it to be immutable and cache it
			includedProps = Collections.unmodifiableSet(mergeNames);
			cache.put(key, includedProps);
		}
		
		return includedProps;
	}
	
}
