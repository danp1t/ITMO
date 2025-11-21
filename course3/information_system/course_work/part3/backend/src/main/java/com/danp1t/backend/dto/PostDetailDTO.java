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
public class PostDetailDTO {
    private Integer id;
    private String title;
    private String text;
    private LocalDateTime createdAt;
    private Integer countLike;
    private AccountSimpleDTO owner;
    private List<AttachmentSimpleDTO> attachments;
    private List<TagDTO> tags;
    private List<CommentSimpleDTO> comments;
}