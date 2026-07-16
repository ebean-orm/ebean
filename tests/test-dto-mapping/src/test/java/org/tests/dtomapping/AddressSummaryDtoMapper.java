package org.tests.dtomapping;

/**
 * Stand-in for a pre-existing hand-written mapper class that already occupies the default
 * generated-mapper name ({@code AddressSummaryDtoMapper}, derived from the {@code
 * AddressSummaryDto} target's simple name). Deliberately unrelated to {@code io.ebean.DtoMapper} -
 * its only purpose is to occupy the name so the {@code @DtoMapping(..., mapperName = "...")}
 * override registered in {@code package-info.java} is proven necessary (without the override,
 * codegen would emit a second, conflicting {@code AddressSummaryDtoMapper} class and fail to
 * compile).
 */
public class AddressSummaryDtoMapper {

  public static String legacyDescribe() {
    return "legacy hand-written mapper - not the generated one";
  }
}
