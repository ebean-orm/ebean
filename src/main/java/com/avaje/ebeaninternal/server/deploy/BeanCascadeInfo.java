package com.avaje.ebeaninternal.server.deploy;

import javax.persistence.CascadeType;

/**
 * Persist info for determining if save or delete should be performed.
 * <p>
 * This is set to associated Beans, Table joins and List.
 * </p>
 */
public class BeanCascadeInfo {

    /**
     * should delete cascade.
     */
    boolean delete;
    
    /**
     * Should save cascade.
     */
    boolean save;
    
    /**
     * Should validate cascade.
     */
    boolean validate;
    
    /**
     * Set the raw deployment attribute.
     */
    public void setAttribute(String attr) {
        if (attr == null){
            return;
        }
        attr = attr.toLowerCase();
        delete = (attr.indexOf("delete")>-1);
        if (!delete){
            // same as EJB3 remove
            delete = (attr.indexOf("remove")>-1);
        }
        save = (attr.indexOf("save")>-1);
        if (!save){
            // same as EJB3 persist
            save = (attr.indexOf("persist")>-1);
        }
        if (attr.indexOf("validate")>-1){
        	validate = true;
        }
        
        if (attr.indexOf("all")>-1){
            delete = true;
            save = true;
            validate = true;
        }
    }

    public void setTypes(CascadeType[] types) {
        for (int i = 0; i < types.length; i++) {
            setType(types[i]);
        }
    }
    
    private void setType(CascadeType type) {
        if (type.equals(CascadeType.ALL)){
            save = true;
            delete = true;
        }
        if (type.equals(CascadeType.REMOVE)){
            delete = true;
        }
        if (type.equals(CascadeType.PERSIST)){
            save = true;
        }
        if (type.equals(CascadeType.MERGE)){
            save = true;
        }
        if (save || delete){
        	validate = true;
        }        
    }
    
    /**
     * Return true if delete should cascade.
     */
    public boolean isDelete() {
        return delete;
    }
    /**
     * Set to true if delete should cascade.
     */
    public void setDelete(boolean isDelete) {
        this.delete = isDelete;
    }
    /**
     * Return true if save should cascade.
     */
    public boolean isSave() {
        return save;
    }
    	
    /**
     * Set to true if save should cascade.
     */
    public void setSave(boolean isUpdate) {
        this.save = isUpdate;
    }

    /**
     * Return true if validate should be cascaded.
     */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * Set validate to cascade or not.
	 */
	public void setValidate(boolean isValidate) {
		this.validate = isValidate;
	}

}
