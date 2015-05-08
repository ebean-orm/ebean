package com.avaje.ebeaninternal.server.util;

import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Parameters used to create the specific Map Set or List object.
 */
public class BeanCollectionParams {

	private final SpiQuery.Type manyType;

	/**
	 * Construct without a specific capacity.
	 */
	public BeanCollectionParams(SpiQuery.Type manyType) {
		this.manyType = manyType;
	}

	/**
	 * Return the type Map Set or List.
	 */
	public SpiQuery.Type getManyType() {
		return manyType;
	}

}
