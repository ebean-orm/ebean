package org.tests.model.inheritexposedtype;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("photo")
public class IXPhoto extends IXResource {

}
