package org.tests.model.join;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@DiscriminatorValue("C")
@Entity
public class CustomerAccess extends HAccess {

}
