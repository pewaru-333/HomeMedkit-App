package ru.application.homemedkit.connectionController.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MainModel {
    @JsonProperty("codeFounded")
    public boolean codeFounded;
    @JsonProperty("checkResult")
    public boolean checkResult;
    @JsonProperty("category")
    public String category;
    @JsonProperty("cis")
    public String cis;
    @JsonProperty("drugsData")
    public DrugsData drugsData;
}
