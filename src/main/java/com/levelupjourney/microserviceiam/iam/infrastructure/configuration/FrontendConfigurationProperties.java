package com.levelupjourney.microserviceiam.iam.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendConfigurationProperties {

    private boolean production = true;
    private String prodUrl;
    private String devUrl;
    private List<String> prodOrigins = new ArrayList<>();
    private List<String> devOrigins = new ArrayList<>();
    private List<String> callbackPaths = new ArrayList<>();

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getProdUrl() {
        return prodUrl;
    }

    public void setProdUrl(String prodUrl) {
        this.prodUrl = prodUrl;
    }

    public String getDevUrl() {
        return devUrl;
    }

    public void setDevUrl(String devUrl) {
        this.devUrl = devUrl;
    }

    public List<String> getProdOrigins() {
        return prodOrigins;
    }

    public void setProdOrigins(List<String> prodOrigins) {
        this.prodOrigins = new ArrayList<>(prodOrigins);
    }

    public List<String> getDevOrigins() {
        return devOrigins;
    }

    public void setDevOrigins(List<String> devOrigins) {
        this.devOrigins = new ArrayList<>(devOrigins);
    }

    public List<String> getCallbackPaths() {
        return callbackPaths;
    }

    public void setCallbackPaths(List<String> callbackPaths) {
        this.callbackPaths = callbackPaths != null ? new ArrayList<>(callbackPaths) : new ArrayList<>();
    }

    public String getActiveBaseUrl() {
        String activeUrl = isProduction() ? prodUrl : devUrl;
        return normalizeBaseUrl(activeUrl);
    }

    public List<String> getAuthorizedRedirectUris() {
        String baseUrl = getActiveBaseUrl();
        if (callbackPaths.isEmpty()) {
            return baseUrl.isBlank() ? List.of() : List.of(baseUrl);
        }

        return callbackPaths.stream()
                .map(path -> baseUrl + normalizePath(path))
                .collect(Collectors.toList());
    }

    public String getPrimaryRedirectUri() {
        return getAuthorizedRedirectUris().stream()
                .findFirst()
                .orElse(getActiveBaseUrl());
    }

    public List<String> getAllowedOrigins() {
        return isProduction() ? prodOrigins : devOrigins;
    }

    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
