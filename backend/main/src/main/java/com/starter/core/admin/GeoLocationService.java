package com.starter.core.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service for IP-based geolocation using ip-api.com.
 * Free tier: 45 requests/minute (no API key needed).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private static final String IP_API_URL = "http://ip-api.com/json/";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private final ObjectMapper objectMapper;

    /**
     * Lookup location for an IP address.
     *
     * @param ipAddress IP address to lookup
     * @return GeoLocation with coordinates and location info, or null on failure
     */
    public GeoLocation lookup(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || isLocalIp(ipAddress)) {
            log.debug("Skipping geolocation for local/invalid IP: {}", ipAddress);
            return null;
        }

        try {
            String url = IP_API_URL + ipAddress + "?fields=status,country,city,lat,lon";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                IpApiResponse apiResponse = objectMapper.readValue(response.body(), IpApiResponse.class);

                if ("success".equals(apiResponse.getStatus())) {
                    return GeoLocation.builder()
                        .latitude(
                            apiResponse.getLat() != null
                                ? BigDecimal.valueOf(apiResponse.getLat())
                                : null
                        )
                        .longitude(
                            apiResponse.getLon() != null
                                ? BigDecimal.valueOf(apiResponse.getLon())
                                : null
                        )
                        .country(apiResponse.getCountry())
                        .city(apiResponse.getCity())
                        .build();
                }
            }

            log.warn("Geolocation lookup failed for IP {}: status {}", ipAddress, response.statusCode());
            return null;

        } catch (Exception e) {
            log.error("Error during geolocation lookup for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }

    private boolean isLocalIp(String ip) {
        return ip.equals("127.0.0.1")
            || ip.equals("0:0:0:0:0:0:0:1")
            || ip.equals("::1")
            || ip.startsWith("192.168.")
            || ip.startsWith("10.")
            || ip.startsWith("172.16.");
    }

    /** Result of geolocation lookup. */
    @Data
    @lombok.Builder
    public static class GeoLocation {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String country;
        private String city;
    }

    /** Response from ip-api.com. */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class IpApiResponse {
        private String status;
        private String country;
        private String city;
        private Double lat;
        private Double lon;
    }
}
