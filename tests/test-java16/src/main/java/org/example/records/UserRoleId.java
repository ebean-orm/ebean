package org.example.records;

import javax.persistence.Embeddable;

@Embeddable
public record UserRoleId(Integer userId, String roleId) {
}
