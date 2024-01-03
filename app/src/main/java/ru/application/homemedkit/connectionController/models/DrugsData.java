package ru.application.homemedkit.connectionController.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugsData {
    @JsonProperty("prodDescLabel")
    public String prodDescLabel;
    @JsonProperty("foiv")
    public Foiv foiv;
    @JsonProperty("expireDate")
    public long expireDate;
    @JsonProperty("vidalData")
    public VidalData vidalData;
}
