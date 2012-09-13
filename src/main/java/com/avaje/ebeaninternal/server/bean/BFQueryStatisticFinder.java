package com.avaje.ebeaninternal.server.bean;

import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.meta.MetaQueryStatistic;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.query.CQueryPlan;

/**
 * BeanFinder for MetaQueryStatistic.
 */
public class BFQueryStatisticFinder implements BeanFinder<MetaQueryStatistic> {

	
	public MetaQueryStatistic find(BeanQueryRequest<MetaQueryStatistic> request) {
		throw new RuntimeException("Not Supported yet");
	}

	/**
	 * Only returns Lists at this stage.
	 */
	public BeanCollection<MetaQueryStatistic> findMany(BeanQueryRequest<MetaQueryStatistic> request) {

		SpiQuery.Type queryType = ((SpiQuery<?>)request.getQuery()).getType();
		if (!queryType.equals(SpiQuery.Type.LIST)){
			throw new PersistenceException("Only findList() supported at this stage.");
		}
		
		BeanList<MetaQueryStatistic> list = new BeanList<MetaQueryStatistic>();
		
		SpiEbeanServer server = (SpiEbeanServer) request.getEbeanServer();
		build(list, server);
		
		String orderBy = request.getQuery().order().toStringFormat();
		if (orderBy == null){
			orderBy = "beanType, origQueryPlanHash, autofetchTuned";
		}
		server.sort(list, orderBy);

		return list;
	}

	private void build(List<MetaQueryStatistic> list, SpiEbeanServer server) {

		for (BeanDescriptor<?> desc : server.getBeanDescriptors()) {
			desc.clearQueryStatistics();			
			build(list, desc);
		}		
	}
	
	private void build(List<MetaQueryStatistic> list, BeanDescriptor<?> desc) {

		Iterator<CQueryPlan> it = desc.queryPlans();
		while (it.hasNext()) {
			CQueryPlan queryPlan = (CQueryPlan) it.next();
			list.add(queryPlan.createMetaQueryStatistic(desc.getFullName()));
		}
	}
	
}
