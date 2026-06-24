package com.qualitysports.backend.meta.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "meta")
public class MetaProperties {

    private String pixelId = "";
    private String accessToken = "";
    private String baseUrl = "https://graph.facebook.com/v22.0";
    private boolean enabled = true;
    private String catalogToken = "";
    private String testEventCode = "";

    public String getPixelId()               { return pixelId; }
    public void   setPixelId(String v)       { this.pixelId = v; }

    public String getAccessToken()           { return accessToken; }
    public void   setAccessToken(String v)   { this.accessToken = v; }

    public String getBaseUrl()               { return baseUrl; }
    public void   setBaseUrl(String v)       { this.baseUrl = v; }

    public boolean isEnabled()               { return enabled; }
    public void    setEnabled(boolean v)     { this.enabled = v; }

    public String getCatalogToken()          { return catalogToken; }
    public void   setCatalogToken(String v)  { this.catalogToken = v; }

    public String getTestEventCode()         { return testEventCode; }
    public void   setTestEventCode(String v) { this.testEventCode = v; }
}
