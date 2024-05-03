package com.safa.logisticintegration.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class BranchAddress {

    @Column(name = "pinCode")
    private String pinCode;

    @Column(name = "formattedAddress")
    private String formattedAddress;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "city")
    private String city;

}
