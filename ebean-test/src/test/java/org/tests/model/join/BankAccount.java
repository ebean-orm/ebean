package org.tests.model.join;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("B")
@Entity
public class BankAccount extends HAccount {

}
