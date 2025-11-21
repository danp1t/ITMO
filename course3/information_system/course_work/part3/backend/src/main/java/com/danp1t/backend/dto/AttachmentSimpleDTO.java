package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentSimpleDTO {
    private Integer id;
    private String name;
    private String path;
    private String typeAttachmentName;
}