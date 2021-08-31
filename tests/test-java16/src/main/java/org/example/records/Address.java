package org.example.records;

import javax.persistence.Embeddable;

@Embeddable
public record Address (String line1, String line2, String city) {
}
