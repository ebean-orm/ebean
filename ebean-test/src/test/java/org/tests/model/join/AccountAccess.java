package org.tests.model.join;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@DiscriminatorValue("A")
@Entity
public class AccountAccess extends HAccess {

}
