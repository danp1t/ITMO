package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailDTO {
    private Integer id;
    private String name;
    private String email;
    private List<RoleDTO> roles;
    private List<PostSimpleDTO> posts;
    private List<CommentSimpleDTO> comments;
}