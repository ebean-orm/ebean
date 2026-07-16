package org.tests.dtomapping;

import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@code @DtoMapping(mapperName = ...)} - see {@code package-info.java} and
 * {@link AddressSummaryDtoMapper} (the stand-in pre-existing hand-written class whose name would
 * otherwise collide with the generated mapper's default name). The generated mapper for
 * {@code Address -> AddressSummaryDto} is named {@code AddressSummaryMapper} instead, proven here
 * simply by the fact that this compiles and the two classes coexist in the same package.
 */
class TestDtoMapperName {

  @Test
  void mapperName_rendersUnderOverriddenName_avoidingClashWithHandWrittenClass() {
    Address address = new Address("1 Queen Street", "Auckland");
    address.save();

    // AddressSummaryMapper is the *generated* mapper - AddressSummaryDtoMapper (the default name)
    // is the unrelated hand-written stand-in class, both coexist in this same package
    AddressSummaryDto dto = new AddressSummaryMapper().map(address);

    assertThat(dto.getLine1()).isEqualTo("1 Queen Street");
    assertThat(dto.getCity()).isEqualTo("Auckland");
    assertThat(AddressSummaryDtoMapper.legacyDescribe()).contains("legacy");
  }
}
