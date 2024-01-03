package ru.application.homemedkit.connectionController.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Foiv {
    @JsonProperty("prodFormNormName")
    public String prodFormNormName;
    @JsonProperty("prodDNormName")
    public String prodDNormName;
}
