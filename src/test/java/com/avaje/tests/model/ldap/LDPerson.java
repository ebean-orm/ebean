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
package com.avaje.tests.model.ldap;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

import org.joda.time.LocalDateTime;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.annotation.LdapAttribute;
import com.avaje.ebean.annotation.LdapDomain;
import com.avaje.tests.ldap.SimpleLdapSetAdapater;

@LdapDomain(
    baseDn = "ou=avajepeople,o=avajecustomers,dc=avaje,dc=net,dc=nz", 
    objectclass = "top,organizationalperson,avajeperson,inetadmin,inetorgperson,person,inetuser")
@Entity
public class LDPerson {

    public enum Status {
        @EnumValue(value = "Active")
        ACTIVE,

        @EnumValue(value = "Inactive")
        INACTIVE
    }

    @Id
    @Column(name = "uid")
    private String userId;

    @Column(name = "inetUserStatus")
    private Status status;

    private String cn;
    
    private String sn;

    private String givenName;

    private String userPassword;

    @Basic(fetch=FetchType.LAZY)
    private LocalDateTime modifiedTime;

    @Basic(fetch=FetchType.LAZY)
    @LdapAttribute(adapter=SimpleLdapSetAdapater.class)
    private Set<Long> accounts;
    
    public String toString() {
        return userId + " " + status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Set<Long> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Long> accounts) {
        this.accounts = accounts;
    }
    
    public void addAccount(long accountNumber){
        if (accounts == null){
            accounts = new HashSet<Long>();
        }
        accounts.add(accountNumber);
    }
    
}
