package com.ntews.prediction.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    
    @GetMapping("/health")
    public String health() {
        return "Prediction Service is running!";
    }
    
    @GetMapping("/dashboard/summary")
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPredictions", 150);
        summary.put("highRiskAreas", 5);
        summary.put("averageConfidence", 0.85);
        summary.put("lastUpdated", "2026-03-04T12:00:00Z");
        return summary;
    }
    
    @GetMapping("/hotspots")
    public List<Map<String, Object>> getHotspots() {
        List<Map<String, Object>> hotspots = new ArrayList<>();
        
        // Nairobi hotspot
        Map<String, Object> nairobiHotspot = new HashMap<>();
        nairobiHotspot.put("id", "hotspot-001");
        nairobiHotspot.put("locationName", "Nairobi Central Business District");
        nairobiHotspot.put("location", "Nairobi, Kenya");
        nairobiHotspot.put("latitude", -1.2921);
        nairobiHotspot.put("longitude", 36.8219);
        nairobiHotspot.put("severity", "high");
        nairobiHotspot.put("riskLevel", "high");
        nairobiHotspot.put("probability", 0.92);
        nairobiHotspot.put("confidence", 0.92);
        nairobiHotspot.put("threatType", "Terrorism Threat");
        nairobiHotspot.put("type", "terrorism");
        nairobiHotspot.put("radius", 2000);
        hotspots.add(nairobiHotspot);
        
        // Mombasa hotspot
        Map<String, Object> mombasaHotspot = new HashMap<>();
        mombasaHotspot.put("id", "hotspot-002");
        mombasaHotspot.put("locationName", "Mombasa Port Area");
        mombasaHotspot.put("location", "Mombasa, Kenya");
        mombasaHotspot.put("latitude", -4.0435);
        mombasaHotspot.put("longitude", 39.6682);
        mombasaHotspot.put("severity", "medium");
        mombasaHotspot.put("riskLevel", "medium");
        mombasaHotspot.put("probability", 0.75);
        mombasaHotspot.put("confidence", 0.75);
        mombasaHotspot.put("threatType", "Cyber Attack");
        mombasaHotspot.put("type", "cyber");
        mombasaHotspot.put("radius", 1500);
        hotspots.add(mombasaHotspot);
        
        // Kisumu hotspot
        Map<String, Object> kisumuHotspot = new HashMap<>();
        kisumuHotspot.put("id", "hotspot-003");
        kisumuHotspot.put("locationName", "Kisumu City Center");
        kisumuHotspot.put("location", "Kisumu, Kenya");
        kisumuHotspot.put("latitude", -0.1022);
        kisumuHotspot.put("longitude", 34.7617);
        kisumuHotspot.put("severity", "medium");
        kisumuHotspot.put("riskLevel", "medium");
        kisumuHotspot.put("probability", 0.68);
        kisumuHotspot.put("confidence", 0.68);
        kisumuHotspot.put("threatType", "Civil Unrest");
        kisumuHotspot.put("type", "civil_unrest");
        kisumuHotspot.put("radius", 1800);
        hotspots.add(kisumuHotspot);
        
        // Eldoret hotspot
        Map<String, Object> eldoretHotspot = new HashMap<>();
        eldoretHotspot.put("id", "hotspot-004");
        eldoretHotspot.put("locationName", "Eldoret Town");
        eldoretHotspot.put("location", "Eldoret, Kenya");
        eldoretHotspot.put("latitude", 0.5143);
        eldoretHotspot.put("longitude", 35.2698);
        eldoretHotspot.put("severity", "low");
        eldoretHotspot.put("riskLevel", "low");
        eldoretHotspot.put("probability", 0.45);
        eldoretHotspot.put("confidence", 0.45);
        eldoretHotspot.put("threatType", "Organized Crime");
        eldoretHotspot.put("type", "organized_crime");
        eldoretHotspot.put("radius", 1200);
        hotspots.add(eldoretHotspot);
        
        // Garissa hotspot
        Map<String, Object> garissaHotspot = new HashMap<>();
        garissaHotspot.put("id", "hotspot-005");
        garissaHotspot.put("locationName", "Garissa County");
        garissaHotspot.put("location", "Garissa, Kenya");
        garissaHotspot.put("latitude", -0.4528);
        garissaHotspot.put("longitude", 39.6460);
        garissaHotspot.put("severity", "high");
        garissaHotspot.put("riskLevel", "high");
        garissaHotspot.put("probability", 0.88);
        garissaHotspot.put("confidence", 0.88);
        garissaHotspot.put("threatType", "Border Security");
        garissaHotspot.put("type", "border_security");
        garissaHotspot.put("radius", 2500);
        hotspots.add(garissaHotspot);
        
        return hotspots;
    }
}
