package org.tests.model.join;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@DiscriminatorValue("B")
@Entity
public class BankAccount extends HAccount {

}
