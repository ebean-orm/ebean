package org.example.domain;

import jakarta.persistence.MappedSuperclass;

/**
 * Intermediate generic mapped superclass used to verify that type-variable resolution
 * composes correctly across two levels of generic inheritance:
 * {@code Concrete extends GenericMiddleModel<Long> extends GenericBaseModel<Long>}.
 */
@MappedSuperclass
public abstract class GenericMiddleModel<T> extends GenericBaseModel<T> {
}
