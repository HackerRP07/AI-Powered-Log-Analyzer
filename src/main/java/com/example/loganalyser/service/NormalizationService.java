package com.example.loganalyser.service;

import com.example.loganalyser.util.RedactionUtil;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

    public String normalize(String rawText) {
        if (rawText == null) {
            return "";
        }
        return RedactionUtil.redactSensitiveData(rawText.trim());
    }
}
