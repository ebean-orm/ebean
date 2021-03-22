package org.example.records;

import javax.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record UserSiteId(UUID userId, UUID siteId) {
}
