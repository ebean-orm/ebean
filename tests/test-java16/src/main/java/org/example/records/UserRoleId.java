package org.example.records;

import jakarta.persistence.Embeddable;

@Embeddable
public record UserRoleId(Integer userId, String roleId) {
}
