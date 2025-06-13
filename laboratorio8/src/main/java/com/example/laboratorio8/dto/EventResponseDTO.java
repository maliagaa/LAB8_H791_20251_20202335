package com.example.laboratorio8.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String nombre;
    private String ciudad;
    private String condicionClimatica;
    private Double temperaturaMaxima;
    private Double temperaturaMinima;
}
