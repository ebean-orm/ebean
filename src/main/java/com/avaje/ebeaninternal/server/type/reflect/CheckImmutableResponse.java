package com.avaje.ebeaninternal.server.type.reflect;



public class CheckImmutableResponse {

    private boolean immutable = true;
    
    private String reasonNotImmutable;

    private boolean compoundType;
    
    public String toString(){
        if(immutable){
            return "immutable";
        } else {
            return "not immutable due to:"+reasonNotImmutable;
        }
    }

    public boolean isCompoundType() {
        return compoundType;
    }

    public void setCompoundType(boolean compoundType) {
        this.compoundType = compoundType;
    }

    public String getReasonNotImmutable() {
        return reasonNotImmutable;
    }

    public void setReasonNotImmutable(String error) {
        this.immutable = false;
        this.reasonNotImmutable = error;
    }

    public boolean isImmutable() {
        return immutable;
    }
}
