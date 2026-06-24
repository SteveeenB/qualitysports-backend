package com.qualitysports.backend.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaCatalogProductoDTO {

    private String id;
    private String title;
    private String description;
    private String availability;
    private String condition;
    private String price;
    private String link;
    @JsonProperty("image_link")
    private String imageLink;
    private String brand;
    private String currency;

    public MetaCatalogProductoDTO() {}

    public String getId()                   { return id; }
    public void   setId(String id)          { this.id = id; }

    public String getTitle()                { return title; }
    public void   setTitle(String title)    { this.title = title; }

    public String getDescription()                    { return description; }
    public void   setDescription(String description)  { this.description = description; }

    public String getAvailability()                    { return availability; }
    public void   setAvailability(String availability) { this.availability = availability; }

    public String getCondition()                  { return condition; }
    public void   setCondition(String condition)  { this.condition = condition; }

    public String getPrice()               { return price; }
    public void   setPrice(String price)   { this.price = price; }

    public String getLink()               { return link; }
    public void   setLink(String link)    { this.link = link; }

    public String getImageLink()                  { return imageLink; }
    public void   setImageLink(String imageLink)  { this.imageLink = imageLink; }

    public String getBrand()               { return brand; }
    public void   setBrand(String brand)   { this.brand = brand; }

    public String getCurrency()                  { return currency; }
    public void   setCurrency(String currency)   { this.currency = currency; }
}
