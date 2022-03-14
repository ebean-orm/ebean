package org.tests.model.join;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("C")
@Entity
public class CustomerAccess extends HAccess {

}
