package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<TagDTO> tags;
    private Integer commentsCount;

    public PostDTO(Integer id, String title, String text, LocalDateTime createdAt,
                   Integer countLike, Integer ownerId, String ownerName) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.createdAt = createdAt;
        this.countLike = countLike;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }
}