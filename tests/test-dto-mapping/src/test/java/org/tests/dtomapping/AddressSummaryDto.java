package org.tests.dtomapping;

/**
 * Plain DTO mapped from {@link org.tests.dtomapping.model.Address} - registered with an explicit
 * {@code mapperName()} override (see {@code package-info.java}) so the generated mapper doesn't
 * collide with the pre-existing hand-written {@link AddressSummaryDtoMapper} class of the default
 * expected name - mirroring the real-world scenario that motivated {@code mapperName()}
 * (central-access's legacy hand-written {@code FleetMapper} occupying the name a generated
 * {@code CFleet -> Fleet} mapper would otherwise need).
 */
public class AddressSummaryDto {

  private final Long id;
  private final String line1;
  private final String city;

  public AddressSummaryDto(Long id, String line1, String city) {
    this.id = id;
    this.line1 = line1;
    this.city = city;
  }

  public Long getId() {
    return id;
  }

  public String getLine1() {
    return line1;
  }

  public String getCity() {
    return city;
  }
}
