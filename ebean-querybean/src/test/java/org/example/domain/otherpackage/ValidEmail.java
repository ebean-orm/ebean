package org.example.domain.otherpackage;

public class ValidEmail implements Email<ValidEmail> {
  private final String emailAddress;

  public ValidEmail(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  @Override
  public int compareTo(final ValidEmail o) {
    return emailAddress.compareTo(o.emailAddress);
  }
}
