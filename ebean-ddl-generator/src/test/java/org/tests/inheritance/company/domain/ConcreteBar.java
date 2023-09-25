package org.tests.inheritance.company.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * @author Per-Ingemar Andersson, It-huset i Norden AB
 */
@Entity
@DiscriminatorValue("ConcreteBar")
public class ConcreteBar extends AbstractBar {
}
