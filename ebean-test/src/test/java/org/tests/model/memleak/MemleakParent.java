package org.tests.model.memleak;

import io.ebean.annotation.Cache;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Class that holds the {@link MemleakChild}.
 *
 * @author Jonas Fr&ouml;hler, Foconis Analytics GmbH
 */
@Entity
@Cache(enableQueryCache = true)
public class MemleakParent {

  @Id
  Long id;

  @ManyToOne()
  MemleakChild child;

  String name;

}
