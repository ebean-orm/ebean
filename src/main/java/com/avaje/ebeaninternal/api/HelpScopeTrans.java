/**
 * Copyright (C) 2006  Robin Bygrave
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
package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.TxScope;

/**
 * Helper object to make AOP generated code simpler.
 */
public class HelpScopeTrans {

	/**
	 * Create a ScopeTrans for a given methods TxScope.
	 */
	public static ScopeTrans createScopeTrans(TxScope txScope) {
		
		EbeanServer server = Ebean.getServer(txScope.getServerName());
		SpiEbeanServer iserver = (SpiEbeanServer)server;
		return iserver.createScopeTrans(txScope);
	}
	
	/**
	 * Exiting the method in an expected fashion.
	 * <p>
	 * That is returning successfully or via a caught exception. 
	 * Unexpected exceptions are caught via the Thread uncaughtExceptionHandler.
	 * </p>
	 * @param returnOrThrowable the return or throwable object
	 * @param opCode the opcode for ATHROW or ARETURN etc
	 * @param scopeTrans the scoped transaction the method was run with.
	 */
	public static void onExitScopeTrans(Object returnOrThrowable, int opCode, ScopeTrans scopeTrans){
		
		scopeTrans.onExit(returnOrThrowable, opCode);
	}
}
