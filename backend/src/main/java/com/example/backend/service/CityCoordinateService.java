package com.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CityCoordinateService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Coordinates> coordinatesByCity;

    /**
     * Constructs the CityCoordinateService.
     *
     * @param resourceLoader the resource loader used to fetch the city catalog file
     */
    public CityCoordinateService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Finds coordinates for a city name or UI label.
     *
     * @param cityName city name, optionally with province suffix
     * @return coordinates when the city exists in the catalog
     */
    public Optional<Coordinates> findCoordinates(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return Optional.empty();
        }

        ensureLoaded();
        return Optional.ofNullable(coordinatesByCity.get(normalize(cityName)));
    }

    /**
     * Loads the city catalog once and caches it in memory.
     * <p>
     * This method is thread-safe and lazily evaluates the resource repository.
     * If the catalog data file is missing or contains parsing errors, it fails
     * gracefully
     * by initializing an empty coordinates map to prevent subsequent failures.
     */
    private synchronized void ensureLoaded() {
        if (coordinatesByCity != null) {
            return;
        }

        Map<String, Coordinates> loadedCoordinates = new HashMap<>();
        Resource resource = resourceLoader.getResource("classpath:data/gi_comuni.json");

        if (!resource.exists()) {
            coordinatesByCity = loadedCoordinates;
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            if (root.isArray()) {
                for (JsonNode city : root) {
                    String name = city.path("denominazione_ita").asText(null);
                    String latitude = city.path("lat").asText(null);
                    String longitude = city.path("lon").asText(null);

                    if (name != null && latitude != null && longitude != null) {
                        loadedCoordinates.put(normalize(name), new Coordinates(
                                Double.parseDouble(latitude),
                                Double.parseDouble(longitude)));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            loadedCoordinates.clear();
        }

        coordinatesByCity = loadedCoordinates;
    }

    /**
     * Normalizes city labels for catalog lookup by stripping out province suffixes
     * and converting the text to lowercase.
     *
     * @param cityName raw city name or label containing optional province
     *                 indicators
     * @return a trimmed, standardized lowercase string suitable as a lookup map key
     */
    private String normalize(String cityName) {
        String normalized = cityName.trim();
        int provinceStart = normalized.indexOf(" (");
        if (provinceStart >= 0) {
            normalized = normalized.substring(0, provinceStart);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    /**
     * Represents a geographical point with latitude and longitude coordinates.
     *
     * @param latitude  the latitude value of the location
     * @param longitude the longitude value of the location
     */
    public record Coordinates(double latitude, double longitude) {
    }
}
