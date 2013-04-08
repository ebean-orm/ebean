package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import com.avaje.ebeaninternal.server.type.ScalarType;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

    private final ScalarType<T> collectionScalarType;
    
    public BeanPropertySimpleCollection(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
        super(owner, descriptor, deploy);
        this.collectionScalarType = deploy.getCollectionScalarType();
    }

    public void initialise() {
        super.initialise();
    }
    
}
