package com.example.weather_rest_clone.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "weather_city")
public class WeatherCity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weather_city_seq")
    @SequenceGenerator(name = "weather_city_seq", sequenceName = "weather_city_seq", allocationSize = 1)
    private int id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "retrieve_date", nullable = false)
    private LocalDate retrieveDate;

    @Setter
    @Column(name = "weather_data_json", nullable = false, length = 2000)
    private String weatherDataJson;


    public WeatherCity(String cityName, LocalDate retrieveDate, String weatherDataJson) {
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
        this.weatherDataJson = weatherDataJson;
    }

    public WeatherCity(int id, String cityName, LocalDate retrieveDate, String weatherDataJson) {
        this.id = id;
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
        this.weatherDataJson = weatherDataJson;
    }
}
