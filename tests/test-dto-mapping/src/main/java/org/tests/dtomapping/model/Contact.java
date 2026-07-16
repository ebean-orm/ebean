package org.tests.dtomapping.model;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.UUID;

@Entity
public class Contact extends Model {

  @Id
  private Long id;

  private String firstName;
  private String lastName;

  /**
   * Primitive {@code boolean} field, mapped directly (no {@code @DtoPath}/{@code @DtoConvert}) -
   * exercises the generator resolving {@code isActive()} rather than guessing {@code getActive()}
   * for the source accessor (standard JavaBean convention for a primitive boolean).
   */
  private boolean active = true;

  @ManyToOne
  private Customer customer;

  /** Per-contact engagement score - purely so {@link ContactStats} has something to {@code @Sum}. */
  private Integer engagementScore;

  /**
   * Stored as a raw {@code Short} (0/1) - a stand-in for the kind of legacy scalar encoding
   * {@code @DtoConvert}'s static dispatch is meant for (e.g. {@code short} to {@code boolean}).
   */
  private Short status;

  /**
   * Stand-in for a value needing a real dependency to convert (e.g. a cipher) - a stand-in for
   * {@code @DtoConvert}'s instance dispatch, resolved via {@code DtoConverterManager}.
   */
  private String secretCode;

  /**
   * Stand-in for a common recurring type pair (e.g. {@code UUID <-> String}) - exercises
   * {@code @DtoConverters} package-level type-pair auto-dispatch (requirement r21), where no
   * per-property {@code @DtoConvert} is needed at all unless overriding the registered default.
   */
  private UUID referenceCode;

  public Contact(String firstName, String lastName, Customer customer) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.customer = customer;
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public Integer getEngagementScore() {
    return engagementScore;
  }

  public void setEngagementScore(Integer engagementScore) {
    this.engagementScore = engagementScore;
  }

  public Short getStatus() {
    return status;
  }

  public void setStatus(Short status) {
    this.status = status;
  }

  public String getSecretCode() {
    return secretCode;
  }

  public void setSecretCode(String secretCode) {
    this.secretCode = secretCode;
  }

  public UUID getReferenceCode() {
    return referenceCode;
  }

  public void setReferenceCode(UUID referenceCode) {
    this.referenceCode = referenceCode;
  }
}
