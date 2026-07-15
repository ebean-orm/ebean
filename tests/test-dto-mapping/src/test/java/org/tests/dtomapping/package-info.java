/**
 * Registers the DTO mapping pairs for issue #2540's nested DTO mapping - triggers
 * {@code querybean-generator} to generate a {@code XxxMapper} implementing
 * {@code io.ebean.DtoMapper<Source, Target>} for each pair (see docs/dto-mapping-design.md).
 * <p>
 * {@code Customer -> CustomerRefDto} is a second, shallow mapping for {@code Customer} (in
 * addition to the full {@code Customer -> CustomerDto} mapping) - it's what {@link
 * org.tests.dtomapping.ContactDto#getCustomer()} maps to, so the back-reference doesn't
 * reintroduce the {@code Customer -> Contact -> Customer} cycle that a full nested {@code
 * CustomerDto} would create.
 * <p>
 * {@code ContactSummary -> ContactSummaryDto} is a worked example (see docs/dto-mapping-design.md,
 * "Ad-hoc computed/formula properties") of the recommended pattern for a Blaze-Persistence-style
 * computed DTO property: {@code ContactSummary} is a {@code @View}-mapped read entity (pointed at
 * the plain {@code contact} table, no new DDL) with a {@code @Formula2}-computed {@code fullName}
 * property, mapped to a plain DTO with no new ad-hoc-SQL-on-DTO annotation required.
 * <p>
 * {@code ContactStats -> ContactStatsDto} is a worked example (see docs/dto-mapping-design.md,
 * "Aggregate/group-by computed properties") of the same {@code @View} pattern applied to Ebean's
 * {@code @Sum}/{@code @Aggregation} group-by formulas - the Blaze-Persistence parallel being an
 * {@code @EntityView} with {@code @Mapping("SIZE(...)")}/{@code @Mapping("SUM(...)")} correlated
 * mappings.
 * <p>
 * {@code Contact -> ContactConversionDto} exercises {@code @DtoConvert} (requirements r13/r14,
 * "Section E: Custom property conversion" in docs/dto-mapping-requirements.md) - both the
 * static-dispatch (no registration) and instance-dispatch (via {@code DtoConverterManager}) cases.
 * <p>
 * {@code Contact -> ContactMixinDto} exercises {@code @DtoMixin} - {@link
 * org.tests.dtomapping.ContactMixinDto} carries no annotations of its own at all;
 * {@link org.tests.dtomapping.ContactMixinDtoMixin} overlays the {@code @DtoPath}/
 * {@code @DtoConvert} annotations instead.
 * <p>
 * {@code Customer -> CustomerDto, name = "noContacts", exclude = "contacts"} exercises named
 * variants (requirement r19) - a second, differently-shaped mapping sharing the very same
 * generated {@code CustomerDtoMapper} class, exposed as its {@code noContacts()} accessor method,
 * used via the new {@code query.mapTo(Class, DtoMapper)} overload.
 * <p>
 * {@code Contact -> ContactBuilderDto} exercises builder-based construction (requirement r18) -
 * {@link org.tests.dtomapping.ContactBuilderDto} follows the {@code avaje-recordbuilder}
 * convention (a static {@code builder()} factory + fluent setters + {@code build()}), forced via
 * {@code builder = ALWAYS}.
 */
@DtoMapping(source = Customer.class, target = CustomerDto.class)
@DtoMapping(source = Customer.class, target = CustomerDto.class, name = "noContacts", exclude = "contacts")
@DtoMapping(source = Address.class, target = AddressDto.class)
@DtoMapping(source = Contact.class, target = ContactDto.class)
@DtoMapping(source = Customer.class, target = CustomerRefDto.class)
@DtoMapping(source = ContactSummary.class, target = ContactSummaryDto.class)
@DtoMapping(source = ContactStats.class, target = ContactStatsDto.class)
@DtoMapping(source = Contact.class, target = ContactConversionDto.class)
@DtoMapping(source = Contact.class, target = ContactMixinDto.class)
@DtoMapping(source = Contact.class, target = ContactBuilderDto.class, builder = DtoMapping.Builder.ALWAYS)
package org.tests.dtomapping;

import io.ebean.annotation.DtoMapping;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.ContactStats;
import org.tests.dtomapping.model.ContactSummary;
import org.tests.dtomapping.model.Customer;
