package com.infoworks.lab.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infoworks.lab.domain.validation.constraint.MoneyFormat.Money;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MakeTransactionWithCharge extends MakeTransaction {

    @NotNull(message = "baseCharge has to be not null, you may pass default zero amount. e.g. 0.00 ")
    @Money(message = "baseCharge has to be 0.00 or any combination with at least 2 digit after precision. e.g. 1002001.00 or 1200933.97 etc")
    private String baseCharge;

    public String getBaseCharge() {
        return baseCharge;
    }

    public void setBaseCharge(String baseCharge) {
        this.baseCharge = baseCharge;
    }

    public boolean isBaseChargeValid() {
        BigDecimal decimal = checkBigDecimal(getBaseCharge());
        if (decimal != null) setBaseCharge(decimal.toPlainString());
        return decimal != null;
    }

    /**
     * serviceCharge, which is an agreement b/w company-VS-merchant or company-VS-agent/rider
     * e.g. amount could be 60.00 for both merchant and agent/rider
     */
    @NotNull(message = "serviceCharge has to be not null, you may pass default zero amount. e.g. 0.00 ")
    @Money(message = "serviceCharge has to be 0.00 or any combination with at least 2 digit after precision. e.g. 1002001.00 or 1200933.97 etc")
    private String serviceCharge;

    public String getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public boolean isServiceChargeValid() {
        BigDecimal decimal = checkBigDecimal(getServiceCharge());
        if (decimal != null) setServiceCharge(decimal.toPlainString());
        return decimal != null;
    }

    private BigDecimal checkBigDecimal(String value) {
        try {
            BigDecimal decimal = new BigDecimal(value);
            return decimal;
        } catch (Exception e) {}
        return null;
    }

    @JsonIgnore private String payload;
    @JsonIgnore private Integer status = 200;
    @JsonIgnore private String error;
    @JsonIgnore private String message;
}
