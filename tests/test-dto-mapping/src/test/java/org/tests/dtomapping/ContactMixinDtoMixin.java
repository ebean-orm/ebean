package org.tests.dtomapping;

import io.ebean.annotation.DtoConvert;
import io.ebean.annotation.DtoMixin;
import io.ebean.annotation.DtoPath;

/**
 * Overlays {@code @DtoPath}/{@code @DtoConvert} onto {@link ContactMixinDto}, which carries no
 * annotations of its own - mirrors avaje-jsonb's {@code @Json.MixIn} mechanism. Method names
 * match {@link ContactMixinDto}'s field names ({@code active}, {@code secretCode}, {@code owner});
 * the querybean generator matches each mixin method to the corresponding target property by name
 * and applies whichever annotations are present as if declared on the target field itself.
 */
@DtoMixin(ContactMixinDto.class)
interface ContactMixinDtoMixin {

  @DtoPath("status")
  @DtoConvert(value = ContactConversions.class, method = "toActive")
  boolean active();

  @DtoConvert(value = SecretCipher.class, method = "decode")
  String secretCode();

  /**
   * A single-hop {@code @DtoPath} rename onto a nested (registered {@code @DtoMapping}) type -
   * {@code owner} is renamed from {@code Contact#getCustomer()}, resolving to
   * {@code CustomerRefDto} via its own generated mapper rather than a raw scalar getter call.
   */
  @DtoPath("customer")
  CustomerRefDto owner();
}
