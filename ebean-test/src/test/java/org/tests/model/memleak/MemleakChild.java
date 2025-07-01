package org.tests.model.memleak;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Class with @Cache(enableQueryCache = true)
 *
 * @author Jonas Fr&ouml;hler, FOCONIS AG
 */
@Entity
public class MemleakChild {

  @Id
  Long id;

  String name;

  transient byte[] memConsumer = new byte[1000000];

}
