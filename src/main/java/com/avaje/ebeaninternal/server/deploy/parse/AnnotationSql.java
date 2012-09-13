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
package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.annotation.Sql;
import com.avaje.ebean.annotation.SqlSelect;
import com.avaje.ebeaninternal.server.deploy.DRawSqlMeta;

/**
 * Read the class level deployment annotations.
 */
public class AnnotationSql extends AnnotationParser {

	public AnnotationSql(DeployBeanInfo<?> info) {
		super(info);
	}

	public void parse() {
		Class<?> cls = descriptor.getBeanType();
		Sql sql = cls.getAnnotation(Sql.class);
		if (sql != null){
			setSql(sql);
		}
		
		
        SqlSelect sqlSelect = cls.getAnnotation(SqlSelect.class);
		if (sqlSelect != null){
			setSqlSelect(sqlSelect);
		}
	}
	
	private void setSql(Sql sql) {
		SqlSelect[] select = sql.select();
		for (int i = 0; i < select.length; i++) {
			setSqlSelect(select[i]);
		}
	}

	private void setSqlSelect(SqlSelect sqlSelect) {

		DRawSqlMeta rawSqlMeta = new DRawSqlMeta(sqlSelect);
		descriptor.add(rawSqlMeta);
	}
}
