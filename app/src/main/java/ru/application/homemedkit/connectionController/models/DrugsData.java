package ru.application.homemedkit.connectionController.models;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugsData {
    @JsonProperty("prodDescLabel")
    @JsonSetter(nulls = Nulls.SKIP)
    public String prodDescLabel = BLANK;
    @JsonProperty("foiv")
    @JsonSetter(nulls = Nulls.SKIP)
    public Foiv foiv = new Foiv();
    @JsonProperty("expireDate")
    @JsonSetter(nulls = Nulls.SKIP)
    public long expireDate = -1L;
    @JsonProperty("vidalData")
    @JsonSetter(nulls = Nulls.SKIP)
    public VidalData vidalData = new VidalData();
}
