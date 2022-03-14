package org.tests.model.join;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("A")
@Entity
public class AccountAccess extends HAccess {

}
