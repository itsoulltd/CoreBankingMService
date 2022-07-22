package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MakeDeposit extends MakeTransaction {
    @JsonIgnore private String prefix;
    @JsonIgnore private String to;
    @JsonIgnore private String type;
}
