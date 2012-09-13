package com.avaje.tests.compositekeys.db;

import javax.persistence.Embeddable;

@Embeddable
public class SubTypeKey
{
    private int subTypeId;

	public int getSubTypeId()
	{
		return subTypeId;
	}

	public void setSubTypeId(int subTypeId)
	{
		this.subTypeId = subTypeId;
	}
	
	public int hashCode() {
		return 31 * 7 + subTypeId;
	}
	
	public boolean equals(Object o){
		if (this == o){
			return true;
		}
		if (!(o instanceof SubTypeKey)) {
			return false;
		}
		return subTypeId == ((SubTypeKey)o).subTypeId;
	}
	
}