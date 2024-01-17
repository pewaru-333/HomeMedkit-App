package ru.application.homemedkit.connectionController.models;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Foiv {
    @JsonProperty("prodFormNormName")
    @JsonSetter(nulls = Nulls.SKIP)
    public String prodFormNormName = BLANK;
    @JsonProperty("prodDNormName")
    @JsonSetter(nulls = Nulls.SKIP)
    public String prodDNormName = BLANK;
}
