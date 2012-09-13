package com.avaje.ebeaninternal.server.bean;

import java.util.Iterator;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebean.meta.MetaAutoFetchStatistic;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autofetch.AutoFetchManager;
import com.avaje.ebeaninternal.server.autofetch.Statistics;

/**
 * Bean Finder for MetaAutoFetchStatistic.
 * <p>
 * This gets the meta data from the AutoFetchManager and creates a copy of that
 * data to give back to the caller in the form of MetaAutoFetchStatistic beans.
 * </p>
 */
public class BFAutoFetchStatisticFinder implements BeanFinder<MetaAutoFetchStatistic> {


	public MetaAutoFetchStatistic find(BeanQueryRequest<MetaAutoFetchStatistic> request) {
		SpiQuery<MetaAutoFetchStatistic> query = (SpiQuery<MetaAutoFetchStatistic>)request.getQuery();
		try {
			String queryPointKey = (String) query.getId();

			SpiEbeanServer server = (SpiEbeanServer) request.getEbeanServer();
			AutoFetchManager manager = server.getAutoFetchManager();

			Statistics stats = manager.getStatistics(queryPointKey);
			if (stats != null) {
				return stats.createPublicMeta();
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	/**
	 * Only returns Lists at this stage.
	 */
	public BeanCollection<MetaAutoFetchStatistic> findMany(BeanQueryRequest<MetaAutoFetchStatistic> request) {

		SpiQuery.Type queryType = ((SpiQuery<?>)request.getQuery()).getType();
		if (!queryType.equals(SpiQuery.Type.LIST)) {
			throw new PersistenceException("Only findList() supported at this stage.");
		}

		SpiEbeanServer server = (SpiEbeanServer) request.getEbeanServer();
		AutoFetchManager manager = server.getAutoFetchManager();

		BeanList<MetaAutoFetchStatistic> list = new BeanList<MetaAutoFetchStatistic>();

		Iterator<Statistics> it = manager.iterateStatistics();
		while (it.hasNext()) {
			Statistics stats = it.next();
			// create a copy for public use
			list.add(stats.createPublicMeta());
		}
		
		String orderBy = request.getQuery().order().toStringFormat();
		if (orderBy == null){
			orderBy = "beanType";
		}
		server.sort(list, orderBy);


		return list;
	}

}
