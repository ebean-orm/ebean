/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.text.json;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.avaje.ebean.text.json.JsonValueAdapter;

public class DefaultJsonValueAdapter implements JsonValueAdapter {

    private final SimpleDateFormat dateTimeProto;

    public DefaultJsonValueAdapter(String dateTimeFormat){
        this.dateTimeProto = new SimpleDateFormat(dateTimeFormat); 
        this.dateTimeProto.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public DefaultJsonValueAdapter(){
        this("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
    
    private SimpleDateFormat dtFormat() {
        return (SimpleDateFormat)dateTimeProto.clone();
    }
    
    public String jsonFromDate(Date date) {
        return "\""+date.toString()+"\"";
    }

    public String jsonFromTimestamp(Timestamp date) {
        return "\""+dtFormat().format(date)+"\"";
    }

    public Date jsonToDate(String jsonDate) {
        return Date.valueOf(jsonDate);
    }

    public Timestamp jsonToTimestamp(String jsonDateTime) {
        try {
            java.util.Date d = dtFormat().parse(jsonDateTime);
            return new Timestamp(d.getTime());
        } catch (Exception e) {
            String m = "Error parsing Datetime["+jsonDateTime+"]";
            throw new RuntimeException(m, e);
        }
    }

    
    
}
