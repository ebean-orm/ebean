/**
 * Copyright (C) 2009 the original author or authors
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

package com.avaje.test.springsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.avaje.ebean.EbeanServer;

/**
 * The Class UserServiceImpl.
 *
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class UserServiceImpl implements UserService {

	/** The ebean server. */
	@Autowired
	private EbeanServer ebeanServer;

	/* (non-Javadoc)
	 * @see org.spring.modules.ebean.UserService#save(org.spring.modules.ebean.User)
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor=Throwable.class)
	public void save(User user) {
		ebeanServer.save(user);
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public User find(long oid) {
		return ebeanServer.find(User.class, oid);
	}


	/**
	 * Gets the ebean server.
	 *
	 * @return the ebeanServer
	 */
	public EbeanServer getEbeanServer() {
		return ebeanServer;
	}

	/**
	 * Sets the ebean server.
	 *
	 * @param ebeanServer the ebeanServer to set
	 */
	public void setEbeanServer(EbeanServer ebeanServer) {
		this.ebeanServer = ebeanServer;
	}


}
