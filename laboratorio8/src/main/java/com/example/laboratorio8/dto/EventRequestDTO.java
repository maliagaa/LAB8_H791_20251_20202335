package com.example.laboratorio8.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDTO {
    private String nombre;
    private String ciudad;
    private LocalDate fecha;
    private String condicionClimatica;
    private Double temperaturaMaxima;
    private Double temperaturaMinima;
}
