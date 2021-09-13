package org.tests.model.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LongJacksonType.class, name = "long"),
        @JsonSubTypes.Type(value = StringJacksonType.class, name = "string")
})
public interface BasicJacksonType<T> {
  T getValue();
}
