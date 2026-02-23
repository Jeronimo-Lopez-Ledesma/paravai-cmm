package com.dekra.service.foundation.domaincore.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.MalformedURLException;
import java.net.URL;

@Getter
@EqualsAndHashCode
@Schema(hidden = true)
public class URLValue {
    private final URL value;

    private URLValue(String url) {
        try {
            this.value = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public static URLValue of(String url) {
        return new URLValue(url);
    }

    public String getHost() {
        return value.getHost();
    }

    public String getProtocol() {
        return value.getProtocol();
    }

    public String getPath() {
        return value.getPath();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
