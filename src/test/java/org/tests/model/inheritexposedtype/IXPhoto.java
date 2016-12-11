package org.tests.model.inheritexposedtype;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("photo")
public class IXPhoto extends IXResource {

}
