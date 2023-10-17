package org.example.records;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address (String line1, String line2, String city) {
}
