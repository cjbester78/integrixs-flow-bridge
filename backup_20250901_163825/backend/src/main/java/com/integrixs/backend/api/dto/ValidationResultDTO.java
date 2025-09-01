package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for validation results
 */
@Data
@NoArgsConstructor
public class ValidationResultDTO {
    private boolean valid = true;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> infos = new ArrayList<>();
    
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public void addInfo(String info) {
        this.infos.add(info);
    }
}