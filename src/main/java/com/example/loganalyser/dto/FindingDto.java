package com.example.loganalyser.dto;

import lombok.Data;

@Data
public class FindingDto {
    private String severity;
    private String message;
    private String evidence;
}
