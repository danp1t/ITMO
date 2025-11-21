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
public class PostSimpleDTO {
    private Integer id;
    private String title;
    private LocalDateTime createdAt;
    private Integer countLike;
}