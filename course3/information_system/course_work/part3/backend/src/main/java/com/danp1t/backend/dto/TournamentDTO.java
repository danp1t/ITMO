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
public class TournamentDTO {
    private Integer id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private String address;
    private String link;
    private Integer rangId;
    private String rangName;
}