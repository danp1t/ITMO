package com.danp1t.controller;

import com.danp1t.interceptor.CacheStatisticsConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cache-statistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CacheStatisticsController {

    @Inject
    private CacheStatisticsConfig cacheStatisticsConfig;

    @POST
    @Path("/enable")
    public Response enableStatistics() {
        cacheStatisticsConfig.enableStatistics();
        return Response.ok("Cache statistics enabled").build();
    }

    @POST
    @Path("/disable")
    public Response disableStatistics() {
        cacheStatisticsConfig.disableStatistics();
        return Response.ok("Cache statistics disabled").build();
    }

    @POST
    @Path("/reset")
    public Response resetStatistics() {
        cacheStatisticsConfig.resetStatistics();
        return Response.ok("Cache statistics reset").build();
    }

    @GET
    @Path("/status")
    public Response getStatus() {
        boolean enabled = cacheStatisticsConfig.isStatisticsEnabled();
        String summary = cacheStatisticsConfig.getStatisticsSummary();

        return Response.ok()
                .entity(new StatisticsStatus(enabled, summary))
                .build();
    }

    public static class StatisticsStatus {
        private boolean enabled;
        private String summary;

        public StatisticsStatus(boolean enabled, String summary) {
            this.enabled = enabled;
            this.summary = summary;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }
}