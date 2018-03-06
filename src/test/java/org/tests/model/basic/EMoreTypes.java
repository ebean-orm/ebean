package org.tests.model.basic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import io.ebean.annotation.DbDefault;

/**
 * Contains (nearly) all scalar types to check DDL
 */
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
  @DbDefault("now")
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

  private UUID uuidField;

  @Lob
  private String lobField;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Calendar getTimeField() {
    return timeField;
  }

  public void setTimeField(Calendar timeField) {
    this.timeField = timeField;
  }

  public Calendar getDateField() {
    return dateField;
  }

  public void setDateField(Calendar dateField) {
    this.dateField = dateField;
  }

  public Calendar getDateTimeField() {
    return dateTimeField;
  }

  public void setDateTimeField(Calendar dateTimeField) {
    this.dateTimeField = dateTimeField;
  }

  public boolean isBooleanField() {
    return booleanField;
  }

  public void setBooleanField(boolean booleanField) {
    this.booleanField = booleanField;
  }

  public byte getByteField() {
    return byteField;
  }

  public void setByteField(byte byteField) {
    this.byteField = byteField;
  }

  public short getShortField() {
    return shortField;
  }

  public void setShortField(short shortField) {
    this.shortField = shortField;
  }

  public char getCharField() {
    return charField;
  }

  public void setCharField(char charField) {
    this.charField = charField;
  }

  public int getIntField() {
    return intField;
  }

  public void setIntField(int intField) {
    this.intField = intField;
  }

  public long getLongField() {
    return longField;
  }

  public void setLongField(long longField) {
    this.longField = longField;
  }

  public float getFloatField() {
    return floatField;
  }

  public void setFloatField(float floatField) {
    this.floatField = floatField;
  }

  public double getDoubleField() {
    return doubleField;
  }

  public void setDoubleField(double doubleField) {
    this.doubleField = doubleField;
  }

  public BigInteger getBigIntField() {
    return bigIntField;
  }

  public void setBigIntField(BigInteger bigIntField) {
    this.bigIntField = bigIntField;
  }

  public BigDecimal getBigDecimalField() {
    return bigDecimalField;
  }

  public void setBigDecimalField(BigDecimal bigDecimalField) {
    this.bigDecimalField = bigDecimalField;
  }

  public String getStringField() {
    return stringField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }

  public String getLobField() {
    return lobField;
  }

  public void setLobField(String lobField) {
    this.lobField = lobField;
  }

}
