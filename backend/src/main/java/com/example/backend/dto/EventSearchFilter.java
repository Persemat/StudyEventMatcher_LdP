package com.example.backend.dto;

import java.util.List;

/**
 * Data Transfer Object representing the multi-parameter search and geospatial
 * filters
 * sent by the client to search for events.
 *
 * @param hideOverlapping flag specifying whether events that conflict with the
 *                        user's
 *                        already booked schedule should be filtered out of the
 *                        results
 */
public record EventSearchFilter(
                String search,
                List<String> tags,
                String location,
                String date,
                Double latitude,
                Double longitude,
                Integer distanceKm,
                Boolean hideOverlapping) {
}