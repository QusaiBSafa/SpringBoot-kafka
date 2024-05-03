package com.safa.logisticintegration.data.dto.tawseel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for creating tawseel shipment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TawseelCreateShipmentRequest {

    /**
     * Provided by tawseel for our account
     */
    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("airwaybilno")
    private String airWayBilNo;

    @JsonProperty("order_no")
    private String orderNo;

    @JsonProperty("consignee_first_name")
    private String consigneeFirstName;

    @JsonProperty("consignee_last_name")
    private String consigneeLastName;

    @JsonProperty("consignee_address1")
    private String consigneeAddress1;

    @JsonProperty("consignee_address2")
    private String consigneeAddress2;

    @JsonProperty("destination_city")
    private String destinationCity;

    @JsonProperty("destination_pincode")
    private String destinationPinCode;

    @JsonProperty("state")
    private String state;

    @JsonProperty("telephone1")
    private String patientPhoneNumber;

    /**
     * Account name, this defined by tawseel
     */
    @JsonProperty("vendor_name")
    private String vendorName;

    /**
     * Pharmacy address
     */
    @JsonProperty("vendor_address")
    private String vendorAddress;

    /**
     * Pharmacy city
     */
    @JsonProperty("vendor_city")
    private String vendorCity;

    /**
     * This defined by tawseel as identifier for the account and location
     */
    @JsonProperty("pickup_pincode")
    private String pickupPinCode;

    /**
     * Pharmacy phone address
     */
    @JsonProperty("vendor_phone1")
    private String vendorPhoneNumber;

    /**
     * RTO : return to origin, in case of failed delivery, the order will be returned to this location name
     */
    @JsonProperty("rto_vendor_name")
    private String rtoVendorName;

    /**
     * RTO : return to origin, in case of failed delivery, the order will be returned to this location address
     */
    @JsonProperty("rto_address")
    private String rtoAddress;

    /**
     * RTO : return to origin, in case of failed delivery, the order will be returned to this location city
     */
    @JsonProperty("rto_city")
    private String rtoCity;

    /**
     * RTO : return to origin, in case of failed delivery, the order will be returned to this pincode
     * pincode : is predefined code given by tawseel as identifier for the location and the account
     */
    @JsonProperty("rto_pincode")
    private String rtoPinCode;
    /**
     *
     */
    @JsonProperty("rto_phone")
    private String rtoPhone;
    /**
     * Payment type, the current supported type is COD which means Cash On Delivery
     */
    @JsonProperty("pay_type")
    private String payType;

    @JsonProperty("item_description")
    private String itemDescription;

    /**
     * Order quantity
     */
    @JsonProperty("qty")
    private String quantity;
    /**
     * The price that will be taken from the customer (patient) on delivery which equal to patient share if the order is not paid before delivery(paid by link)
     * If the order is already paid this should be set to 0
     */
    @JsonProperty("collectable_value")
    private String collectableValue;

    /**
     * The orginal totla price of the order
     */
    @JsonProperty("product_value")
    private String productValue;

    @JsonProperty("actual_weight")
    private String actualWeight;

    @JsonProperty("volumetric_weight")
    private String volumetricWeight;

    /**
     * if this is true, you should set the otp Field (which should be sent to the patient), and the driver will ask the patient about the otp in delivery
     */
    @JsonProperty("is_otp_verified")
    private boolean isOtpVerified;

    @JsonProperty("otp")
    private String otp;

}
