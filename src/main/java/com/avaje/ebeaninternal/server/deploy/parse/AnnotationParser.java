package com.avaje.ebeaninternal.server.deploy.parse;

import javax.persistence.CascadeType;
import javax.persistence.PersistenceException;

import com.avaje.ebean.annotation.LdapAttribute;
import com.avaje.ebean.config.ldap.LdapAttributeAdapter;
import com.avaje.ebeaninternal.server.deploy.BeanCascadeInfo;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Base class for reading deployment annotations.
 */
public abstract class AnnotationParser extends AnnotationBase {

	protected final DeployBeanInfo<?> info;
    
    protected final DeployBeanDescriptor<?> descriptor;
        
    protected final Class<?> beanType;
    
    public AnnotationParser(DeployBeanInfo<?> info){
    	super(info.getUtil());
        this.info = info;
        this.beanType = info.getDescriptor().getBeanType();
        this.descriptor = info.getDescriptor();
    }
    
    /**
     * read the deployment annotations.
     */
    public abstract void parse();
    
    /**
     * Helper method to set cascade types to the CascadeInfo on BeanProperty.
     */
    protected void setCascadeTypes(CascadeType[] cascadeTypes, BeanCascadeInfo cascadeInfo) {
        if (cascadeTypes != null && cascadeTypes.length > 0) {
            cascadeInfo.setTypes(cascadeTypes);
        }
    }
    
    protected void readLdapAttribute(LdapAttribute ldapAttribute, DeployBeanProperty prop) {

        if (!isEmpty(ldapAttribute.name())){
            prop.setDbColumn(ldapAttribute.name());
        }
        prop.setDbInsertable(ldapAttribute.insertable());
        prop.setDbUpdateable(ldapAttribute.updatable());

        Class<?> adapterCls = ldapAttribute.adapter();
        
        if (adapterCls != null && !void.class.equals(adapterCls)){
            try {
                LdapAttributeAdapter adapter = (LdapAttributeAdapter)adapterCls.newInstance();
                prop.setLdapAttributeAdapter(adapter);
            } catch (Exception e){
                String msg= "Error creating LdapAttributeAdapter for ["+prop.getFullBeanName()+"] "
                    +"with class ["+adapterCls+"] using the default constructor.";
                throw new PersistenceException(msg, e);
            }
        }
        
    }
}
