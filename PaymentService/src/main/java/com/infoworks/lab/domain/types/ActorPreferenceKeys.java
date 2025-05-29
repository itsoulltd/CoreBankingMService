package com.infoworks.lab.domain.types;

public enum ActorPreferenceKeys {
    AccountBalance("accountBalance"),
    Suspend("suspend"),
    Freelance("freelance"),
    OptZone("optZone"),
    OptMode("optMode"),
    CanRedeem("canRedeem"),
    CanDeposit("canDeposit"),
    ServiceCharge("serviceCharge"),
    BaseCharge("baseCharge");

    private String key;

    ActorPreferenceKeys(String key) {
        this.key = key;
    }

    public String key(){
        return key;
    }
}
