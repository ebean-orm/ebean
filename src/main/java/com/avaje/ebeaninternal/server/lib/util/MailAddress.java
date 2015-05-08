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
