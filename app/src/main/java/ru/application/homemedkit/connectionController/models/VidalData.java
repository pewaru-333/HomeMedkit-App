package ru.application.homemedkit.connectionController.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VidalData {
    @JsonProperty("phKinetics")
    public String phKinetics;
}
