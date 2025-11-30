package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private Integer id;
    private Integer postId;
    private String name;
    private String path;
    private Integer typeAttachmentId;
    private String typeAttachmentName;
}