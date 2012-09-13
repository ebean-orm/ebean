package com.avaje.tests.update;

import java.util.Date;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.embedded.EEmbDatePeriod;
import com.avaje.tests.model.embedded.EEmbInner;
import com.avaje.tests.model.embedded.EEmbOuter;

public class TestEmbeddedRefreshUpdate extends TestCase {

	public void test() {
		
		EEmbOuter outer = new EEmbOuter();
		outer.setNomeOuter("test");
		
		EEmbDatePeriod embeddedBean = new EEmbDatePeriod();
		embeddedBean.setDate1(new Date());
		
		outer.setDatePeriod(embeddedBean);
		
		Ebean.save(outer);
		
		EEmbOuter loaded = Ebean.find(EEmbOuter.class).findUnique();
		
		// if commented Ebean saves correctly
		Ebean.refresh(loaded);
		
		loaded.getDatePeriod().setDate2(new Date());
		
		// BUG 343
		Ebean.save(loaded);
		
		// See BUG 344
		Ebean.find(EEmbInner.class).fetch("outer").orderBy("outer.datePeriod.date1").findList();
		
	}
}
