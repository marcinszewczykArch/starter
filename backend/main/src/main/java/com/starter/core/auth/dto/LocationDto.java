package com.starter.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** DTO for GPS location data from browser geolocation API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private BigDecimal latitude;
    private BigDecimal longitude;
}
