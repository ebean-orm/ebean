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
 * <p>
 * {@code Contact -> ContactIgnoreDto} exercises {@code @DtoIgnore} - {@code externalRef}/
 * {@code auditNotes} have no corresponding getter on {@code Contact} at all (unlike a plain
 * excluded/{@code @DtoConvert}-backed property), modelling a property permanently populated by
 * application code from elsewhere (e.g. {@code Fleet.assignedMachines} in central-access). The
 * generated mapper additionally exposes {@code mapToBuilder(source)} (since the target uses
 * builder construction) so a caller can set the real value before calling {@code build()}.
 * <p>
 * {@code Customer -> CustomerTagsDto, name = "noTags", exclude = "tags"} exercises the extended
 * named-variant exclusion (docs/dto-mapping-requirements.md, "Section G" follow-up) - a plain
 * {@code SCALAR}/{@code @DtoConvert}-backed {@code List} property (no registered nested DTO
 * mapping for its element type) can also be excluded from a variant, not just
 * {@code NESTED_ONE}/{@code NESTED_MANY} properties.
 * <p>
 * {@code Address -> AddressSummaryDto, mapperName = "AddressSummaryMapper"} exercises {@code
 * mapperName()} - {@link AddressSummaryDtoMapper} is a stand-in pre-existing hand-written class
 * occupying the default expected name ({@code AddressSummaryDtoMapper}), so the override renames
 * the generated mapper to {@code AddressSummaryMapper} instead, letting both coexist (mirrors the
 * real central-access {@code CFleet -> Fleet}/legacy {@code FleetMapper} collision).
 * <p>
 * {@code Contact -> ContactTypeConverterDto} exercises {@code @DtoConverters} (requirement r21,
 * "Section E: Type-pair (package-level) custom scalar conversion") - {@link UuidConverters} is
 * registered once below for the whole package, and {@code UUID -> String} properties with no
 * per-property {@code @DtoConvert} are auto-dispatched to it (both a plain same-name property and
 * a {@code @DtoPath}-renamed one), while an explicit {@code @DtoConvert} on the same field still
 * takes precedence over the registered default.
 * <p>
 * {@code Contact -> ContactSetterDto} exercises setter-based (mutable JavaBean) construction
 * (requirement r22, "Section G: Setter-based (mutable JavaBean) target construction") - {@link
 * ContactSetterDto} is a plain no-arg-constructor-plus-setters type (mirroring JAXB/XSD-generated
 * legacy SOAP types), auto-detected ({@code setter = AUTO}, the default) since it has no
 * positional constructor matching its properties. Its {@code @DtoIgnore} properties are populated
 * directly via their public setters after mapping, with no special generated accessor needed
 * (unlike the builder strategy's {@code mapToBuilder(...)}).
 * <p>
 * {@code Contact -> ContactSetterFluentDto} exercises the same setter-based construction path but
 * with <b>fluent-style</b> setters (each returns {@code this} rather than {@code void}) - {@link
 * ContactSetterFluentDto} - confirming detection accepts either shape and the generated code
 * simply calls the setter as a bare statement, ignoring any return value.
 */
@DtoConverters(UuidConverters.class)
@DtoMapping(source = Customer.class, target = CustomerDto.class)
@DtoMapping(source = Customer.class, target = CustomerDto.class, name = "noContacts", exclude = "contacts")
@DtoMapping(source = Customer.class, target = CustomerTagsDto.class)
@DtoMapping(source = Customer.class, target = CustomerTagsDto.class, name = "noTags", exclude = "tags")
@DtoMapping(source = Address.class, target = AddressDto.class)
@DtoMapping(source = Address.class, target = AddressSummaryDto.class, mapperName = "AddressSummaryMapper")
@DtoMapping(source = Contact.class, target = ContactDto.class)
@DtoMapping(source = Customer.class, target = CustomerRefDto.class)
@DtoMapping(source = ContactSummary.class, target = ContactSummaryDto.class)
@DtoMapping(source = ContactStats.class, target = ContactStatsDto.class)
@DtoMapping(source = Contact.class, target = ContactConversionDto.class)
@DtoMapping(source = Contact.class, target = ContactMixinDto.class)
@DtoMapping(source = Contact.class, target = ContactBuilderDto.class, builder = DtoMapping.Builder.ALWAYS)
@DtoMapping(source = Contact.class, target = ContactIgnoreDto.class, builder = DtoMapping.Builder.ALWAYS)
@DtoMapping(source = Contact.class, target = PrimitiveNullPathDto.class)
@DtoMapping(source = Contact.class, target = PrimitiveNullPathFailOnNullDto.class)
@DtoMapping(source = Customer.class, target = ComputedPathDto.class)
@DtoMapping(source = Customer.class, target = ComputedPathNoFetchDto.class)
@DtoMapping(source = Contact.class, target = ContactLeafDto.class)
@DtoMapping(source = Customer.class, target = ComputedNestedDto.class)
@DtoMapping(source = Customer.class, target = ComputedNestedListDto.class)
@DtoMapping(source = Customer.class, target = ComputedRefDto.class)
@DtoMapping(source = Customer.class, target = FetchCollisionDto.class)
@DtoMapping(source = Contact.class, target = ContactTypeConverterDto.class)
@DtoMapping(source = Contact.class, target = ContactSetterDto.class)
@DtoMapping(source = Contact.class, target = ContactSetterFluentDto.class)
package org.tests.dtomapping;

import io.ebean.annotation.DtoConverters;
import io.ebean.annotation.DtoMapping;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.ContactStats;
import org.tests.dtomapping.model.ContactSummary;
import org.tests.dtomapping.model.Customer;
