package org.tests.inheritance.company.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Per-Ingemar Andersson, It-huset i Norden AB
 */
@Entity
@DiscriminatorValue("ConcreteBar")
public class ConcreteBar extends AbstractBar {
}
