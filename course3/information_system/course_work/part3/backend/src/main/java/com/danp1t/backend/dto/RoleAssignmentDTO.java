package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleAssignmentDTO {
    private Integer accountId;
    private Integer roleId;
}