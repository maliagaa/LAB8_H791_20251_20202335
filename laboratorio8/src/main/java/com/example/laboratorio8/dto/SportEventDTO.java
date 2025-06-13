package com.example.laboratorio8.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportEventDTO {
    private String name;
    private String location;

    @JsonProperty("commenceTime")
    private LocalDateTime startTime;
}
