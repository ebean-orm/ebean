/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.lib.util;

/**
 * An Email address with an associated alias.
 */
public class MailAddress {

    
    String alias;
    
    String emailAddress;
    
    /**
     * Create an address with an optional alias.
     */
    public MailAddress(String alias, String emailAddress){
        this.alias = alias;
        this.emailAddress = emailAddress;
    }
    
    /**
     * Return the alias.
     * If the alias is null this returns an empty string.
     */
    public String getAlias() {
        if (alias == null){
            return "";
        }
        return alias;
    }
    
    /**
     * Return the email address.
     */
    public String getEmailAddress(){
        return emailAddress;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getAlias()).append(" ").append("<").append(getEmailAddress()).append(">");
        return sb.toString();
    }

}
