package com.ntews.intel.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMediaData {
    private String id;
    private String platform;
    private String username;
    private String content;
    private LocalDateTime timestamp;
    private List<String> hashtags;
    private Map<String, Object> metadata;
    private String location;
    private Integer likes;
    private Integer shares;
    private Double sentiment;
}
