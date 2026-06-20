package com.qualitysports.backend.heka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "heka")
public class HekaProperties {

    private String apiKey;
    private String email;
    private String password;
    private String channel;
    private String baseUrl;
    private String warehouseId;
    private String cityOrigin;
    private int defaultWeight;
    private int defaultHeight;
    private int defaultLong;
    private int defaultWidth;

    public String getApiKey()       { return apiKey; }
    public void setApiKey(String v) { this.apiKey = v; }

    public String getEmail()        { return email; }
    public void setEmail(String v)  { this.email = v; }

    public String getPassword()         { return password; }
    public void setPassword(String v)   { this.password = v; }

    public String getChannel()          { return channel; }
    public void setChannel(String v)    { this.channel = v; }

    public String getBaseUrl()          { return baseUrl; }
    public void setBaseUrl(String v)    { this.baseUrl = v; }

    public String getWarehouseId()          { return warehouseId; }
    public void setWarehouseId(String v)    { this.warehouseId = v; }

    public String getCityOrigin()           { return cityOrigin; }
    public void setCityOrigin(String v)     { this.cityOrigin = v; }

    public int getDefaultWeight()           { return defaultWeight; }
    public void setDefaultWeight(int v)     { this.defaultWeight = v; }

    public int getDefaultHeight()           { return defaultHeight; }
    public void setDefaultHeight(int v)     { this.defaultHeight = v; }

    public int getDefaultLong()             { return defaultLong; }
    public void setDefaultLong(int v)       { this.defaultLong = v; }

    public int getDefaultWidth()            { return defaultWidth; }
    public void setDefaultWidth(int v)      { this.defaultWidth = v; }
}
