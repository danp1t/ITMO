package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Integer id;
    private String title;
    private String text;
    private LocalDateTime createdAt;
    private Integer countLike;
    private Integer ownerId;
    private String ownerName;
}