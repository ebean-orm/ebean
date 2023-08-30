package org.tests.o2m.jointable.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_super")
public class ClassA extends ClassSuper {
}
