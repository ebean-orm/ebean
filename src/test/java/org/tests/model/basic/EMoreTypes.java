package org.tests.model.basic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "e_more_types")
public class EMoreTypes {
  
  @Id
  Integer id;
  @Temporal(TemporalType.TIME)
  private Calendar timeField;

  @Temporal(TemporalType.DATE)
  private Calendar dateField;

  @Temporal(TemporalType.TIMESTAMP)
  private Calendar dateTimeField;
  
  private boolean booleanField;
  
  private byte byteField;
  
  private short shortField;
  
  private char charField;
  
  private int intField;
  
  private long longField;
  
  private float floatField;
  
  private double doubleField;
  
  private BigInteger bigIntField;
  
  private BigDecimal bigDecimalField;
  
  private String stringField;

  @Lob
  private String lobField;
  
  
}
