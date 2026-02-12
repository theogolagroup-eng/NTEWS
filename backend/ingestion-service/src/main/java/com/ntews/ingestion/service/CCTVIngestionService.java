package com.ntews.ingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CCTVIngestionService {
    
    public void ingestCCTVData() {
        log.info("CCTV ingestion service - placeholder implementation");
    }
    
    public void ingestFromAllCameras() {
        log.info("Ingesting data from all CCTV cameras");
    }
}
