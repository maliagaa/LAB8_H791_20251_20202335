package com.example.laboratorio8.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String condition;
    private Double maxTempC;
    private Double minTempC;
}
