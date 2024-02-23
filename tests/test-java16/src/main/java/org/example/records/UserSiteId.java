package org.example.records;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record UserSiteId(UUID userId, UUID siteId) {
}
