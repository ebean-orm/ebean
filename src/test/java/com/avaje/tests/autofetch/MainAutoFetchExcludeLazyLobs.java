package com.avaje.tests.autofetch;

import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.EBasicClob;

public class MainAutoFetchExcludeLazyLobs {

	public static void main(String[] args) {
	    
		GlobalProperties.put("ebean.autofetch.queryTuning", "true");
		GlobalProperties.put("ebean.autofetch.profiling", "true");
		
		
		EBasicClob a = new EBasicClob();
		a.setName("name 1");
		a.setTitle("a title");
		a.setDescription("not that meaningful");
		
		Ebean.save(a);
		
		List<EBasicClob> list = Ebean.find(EBasicClob.class)
			.setAutofetch(true)
			.findList();
		
		for (EBasicClob bean : list) {
	        bean.getName();
	        // although we read the description
	        // autofetch will not include it later
	        bean.getDescription();
        }
		
		
		
		
    }
}
