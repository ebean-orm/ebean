package org.tests.o2m.jointable.inheritance;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "class_super")
public class ClassA extends ClassSuper {
}
