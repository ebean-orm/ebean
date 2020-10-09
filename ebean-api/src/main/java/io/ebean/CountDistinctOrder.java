package io.ebean;
/**
 * Enumeration to use with {@link Query#setCountDistinct(CountDistinctOrder)}.
 * @author Roland Praml, FOCONIS AG
 *
 */
public enum CountDistinctOrder {
  NO_ORDERING,
  
  /** order by attribute ascending */
  ATTR_ASC, 
  
  /** order by attribute descending */
  ATTR_DESC, 
  
  /** order by count ascending and attribute ascending */
  COUNT_ASC_ATTR_ASC,
  
  /** order by count ascending and attribute descending */
  COUNT_ASC_ATTR_DESC,
  
  /** order by count descending and attribute ascending */
  COUNT_DESC_ATTR_ASC,
  
  /** order by count descending and attribute descending */
  COUNT_DESC_ATTR_DESC,
}
