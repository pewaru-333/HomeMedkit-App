package ru.application.homemedkit.connectionController.models;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VidalData {
    @JsonProperty("phKinetics")
    @JsonSetter(nulls = Nulls.SKIP)
    public String phKinetics = BLANK;
}
