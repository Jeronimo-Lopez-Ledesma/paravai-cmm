package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.regulations.standards.domain.value.StandardCodeValue;
import com.paravai.regulations.standards.domain.value.StandardTitleValue;
import jakarta.validation.constraints.NotBlank;

public class UpdateStandardRequest {

    @NotBlank
    private String authority;

    @NotBlank
    private String code;

    @NotBlank
    private String title;

    private String description; // optional / mutable

    // --- Mapping helpers ---
    public StandardCodeValue toCodeVo() { return StandardCodeValue.of(code); }
    public StandardTitleValue toTitleVo() { return StandardTitleValue.of(title); }
    public String toDescriptionOrNull() { return description; }

    // getters/setters
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
