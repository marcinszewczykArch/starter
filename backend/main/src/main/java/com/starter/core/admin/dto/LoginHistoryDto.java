package com.starter.core.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.starter.core.admin.LoginHistory;

import java.math.BigDecimal;
import java.time.Instant;

/** DTO for login history records. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {

    private Long id;
    private Instant loggedInAt;
    private boolean success;
    private String failureReason;

    // Location
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationSource; // GPS, IP
    private String country;
    private String city;

    // Device
    private String ipAddress;
    private String deviceType;
    private String browser;
    private String os;

    /** Convert from entity to DTO. */
    public static LoginHistoryDto fromEntity(LoginHistory entity) {
        return LoginHistoryDto.builder()
            .id(entity.getId())
            .loggedInAt(entity.getLoggedInAt())
            .success(entity.isSuccess())
            .failureReason(entity.getFailureReason())
            .latitude(entity.getLatitude())
            .longitude(entity.getLongitude())
            .locationSource(
                entity.getLocationSource() != null
                    ? entity.getLocationSource().name()
                    : null
            )
            .country(entity.getCountry())
            .city(entity.getCity())
            .ipAddress(entity.getIpAddress())
            .deviceType(entity.getDeviceType())
            .browser(entity.getBrowser())
            .os(entity.getOs())
            .build();
    }
}
