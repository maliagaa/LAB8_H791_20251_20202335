package com.example.laboratorio8.controller;

import com.example.laboratorio8.dto.EventRequestDTO;
import com.example.laboratorio8.dto.EventResponseDTO;
import com.example.laboratorio8.dto.SportEventDTO;
import com.example.laboratorio8.dto.WeatherDTO;
import com.example.laboratorio8.entity.Evento;
import com.example.laboratorio8.repository.EventoRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventoController {

    private final RestTemplate restTemplate;
    private final EventoRepository repo;

    @Value("${weather.api.key}")
    private String apiKey;

    @GetMapping("/sports")
    public ResponseEntity<List<SportEventDTO>> getSports(
            @RequestParam String ciudad,
            @RequestParam(defaultValue = "7") int days
    ) {
        String url = String.format(
                "https://api.weatherapi.com/v1/sports.json?key=%s&q=%s&days=%d",
                apiKey, ciudad, days);

        try {
            String raw = restTemplate.getForObject(url, String.class);
            System.out.println("Raw sports JSON: " + raw);

            ExternalSportsResponse resp = new ObjectMapper()
                    .readValue(raw, ExternalSportsResponse.class);

            SportEventDTO[] todos = resp.getAllSports();
            if (todos == null || todos.length == 0) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            LocalDate hoy = LocalDate.now();
            List<SportEventDTO> lista = new ArrayList<>();
            for (SportEventDTO ev : todos) {
                long diff = ChronoUnit.DAYS.between(hoy, ev.getStartTime().toLocalDate());
                if (diff >= 0 && diff <= days) {
                    lista.add(ev);
                }
            }

            return ResponseEntity.ok(lista);

        } catch (HttpClientErrorException.Unauthorized ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/weather")
    public WeatherDTO getWeather(
            @RequestParam String ciudad,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        int dias = (int) ChronoUnit.DAYS.between(LocalDate.now(), fecha) + 1;
        String url = String.format(
                "https://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=%d",
                apiKey, ciudad, dias);

        ExternalForecastResponse resp =
                restTemplate.getForObject(url, ExternalForecastResponse.class);

        var day = Arrays.stream(resp.getForecast().getForecastday())
                .filter(d -> d.getDate().equals(fecha))
                .findFirst()
                .orElseThrow();

        return new WeatherDTO(
                day.getDay().getCondition().getText(),
                day.getDay().getMaxtempC(),
                day.getDay().getMintempC()
        );
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponseDTO> saveEvent(
            @RequestBody EventRequestDTO rq
    ) {
        Evento e = new Evento();
        e.setNombre(rq.getNombre());
        e.setCiudad(rq.getCiudad());
        e.setFecha(rq.getFecha());
        e.setCondicionClimatica(rq.getCondicionClimatica());
        e.setTemperaturaMaxima(rq.getTemperaturaMaxima());
        e.setTemperaturaMinima(rq.getTemperaturaMinima());

        Evento saved = repo.save(e);

        EventResponseDTO resp = new EventResponseDTO(
                saved.getId(),
                saved.getNombre(),
                saved.getCiudad(),
                saved.getCondicionClimatica(),
                saved.getTemperaturaMaxima(),
                saved.getTemperaturaMinima()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponseDTO>> listEvents() {
        var salida = repo.findAll().stream()
                .map(e -> new EventResponseDTO(
                        e.getId(),
                        e.getNombre(),
                        e.getCiudad(),
                        e.getCondicionClimatica(),
                        e.getTemperaturaMaxima(),
                        e.getTemperaturaMinima()
                ))
                .toList();
        return ResponseEntity.ok(salida);
    }


    private static class ExternalSportsResponse {
        private SportEventDTO[] football;
        private SportEventDTO[] cricket;
        private SportEventDTO[] golf;

        public SportEventDTO[] getFootball() { return football; }
        @JsonProperty("football")
        public void setFootball(SportEventDTO[] f) { this.football = f; }

        public SportEventDTO[] getCricket() { return cricket; }
        @JsonProperty("cricket")
        public void setCricket(SportEventDTO[] c) { this.cricket = c; }

        public SportEventDTO[] getGolf() { return golf; }
        @JsonProperty("golf")
        public void setGolf(SportEventDTO[] g) { this.golf = g; }

        public SportEventDTO[] getAllSports() {
            List<SportEventDTO> list = new ArrayList<>();
            if (football != null) Collections.addAll(list, football);
            if (cricket != null) Collections.addAll(list, cricket);
            if (golf    != null) Collections.addAll(list, golf);
            return list.toArray(new SportEventDTO[0]);
        }
    }

    private static class ExternalForecastResponse {
        private Forecast forecast;
        public Forecast getForecast() { return forecast; }
        public void setForecast(Forecast f) { this.forecast = f; }

        static class Forecast {
            private ForecastDay[] forecastday;
            public ForecastDay[] getForecastday() { return forecastday; }
            public void setForecastday(ForecastDay[] f) { this.forecastday = f; }
        }
        static class ForecastDay {
            private LocalDate date;
            private Day day;
            public LocalDate getDate() { return date; }
            public void setDate(LocalDate d) { this.date = d; }
            public Day getDay() { return day; }
            public void setDay(Day d) { this.day = d; }
        }
        static class Day {
            private Condition condition;
            @JsonProperty("maxtemp_c")
            private double maxtempC;
            @JsonProperty("mintemp_c")
            private double mintempC;
            public Condition getCondition() { return condition; }
            public void setCondition(Condition c) { this.condition = c; }
            public double getMaxtempC() { return maxtempC; }
            public void setMaxtempC(double m) { this.maxtempC = m; }
            public double getMintempC() { return mintempC; }
            public void setMintempC(double m) { this.mintempC = m; }
            static class Condition {
                private String text;
                public String getText() { return text; }
                public void setText(String t) { this.text = t; }
            }
        }
    }
}
