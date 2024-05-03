package com.safa.logisticintegration.service.logisticProviders.tawseelLoisticProvider;

import com.safa.logisticintegration.data.dto.order.OrderStatus;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.tawseel.TawseelCreateShipmentRequest;
import com.safa.logisticintegration.data.dto.tawseel.TawseelShipmentPaymentType;
import com.safa.logisticintegration.data.dto.tawseel.TawseelShipmentStatus;
import com.safa.logisticintegration.data.entity.BranchAddress;
import com.safa.logisticintegration.data.entity.LogisticProvider;
import com.safa.logisticintegration.exception.BadRequestException;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderTransformer;
import com.safa.logisticintegration.service.slack.SlackService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.safa.logisticintegration.common.Constants.AWB_PREFIX;

/**
 * Transformer to transform the incoming request to Tawseel Request Format
 */
@Component
@RequiredArgsConstructor
public class TawseelTransformer implements ILogisticProviderTransformer<TawseelCreateShipmentRequest> {

    public static final String ACTUAL_WEIGHT_DEFAULT_VALUE = "0.1";
    public static final String VOLUMETRIC_WEIGHT_DEFAULT_VALUE = "0";
    public static final boolean IS_OTP_VERIFIED = false;

    private final SlackService slackService;

    @Override
    public String mapStatus(String externalStatus) {
        TawseelShipmentStatus tawseelShipmentStatus = TawseelShipmentStatus.getByStatus(externalStatus);
        // In case the status code is not mapped
        if (tawseelShipmentStatus == null) {
            slackService.postErrorMessage(String.format("!!Error: Unknown status (%s) from Tawseel please check and add the correct mapping, by default it is mapped to OUT_FOR_DELIVERY", externalStatus));
            return OrderStatus.OUT_FOR_DELIVERY.getStatus();
        }

        return switch (tawseelShipmentStatus) {
            case OUT_FOR_DELIVERY, RTO_OUT_FOR_DELIVERY -> OrderStatus.OUT_FOR_DELIVERY.getStatus();
            case DELIVERED, RTO_DELIVERED -> OrderStatus.DELIVERED.getStatus();
            case UNDELIVERED -> OrderStatus.FAILED_DELIVERY_ATTEMPT.getStatus();
            default -> OrderStatus.READY_FOR_DELIVERY.getStatus();
        };
    }

    /**
     * Transform to Tawseel order create request
     */
    @Override
    public TawseelCreateShipmentRequest transformToThirdPartyRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest, LogisticProvider logisticProvider) {
        validateCreateShipmentRequest(shipmentCreateIncomingRequest, logisticProvider);
        String[] fullName = shipmentCreateIncomingRequest.getRecipient().split(" ");
        ShipmentCreateIncomingRequest.Location addressDetails = shipmentCreateIncomingRequest.getAddressDetails();
        String address = String.format("%s,%s,%s,%s,%s,%s", addressDetails.getApartment(), addressDetails.getBuilding(), addressDetails.getStreet(), addressDetails.getCity(), addressDetails.getArea(), addressDetails.getCountry());
        BranchAddress branchAddress = logisticProvider.getBranchAddress();
        return TawseelCreateShipmentRequest.builder()
                // Set api key, get it from DB logistic logisticProvider account id
                .apiKey(logisticProvider.getAccountId())
                .transactionId("")
                .airWayBilNo(AWB_PREFIX.concat(shipmentCreateIncomingRequest.getShipmentId()))
                .orderNo(shipmentCreateIncomingRequest.getShipmentId())
                // Set patient name, address details.
                .consigneeFirstName(fullName[0])
                .consigneeLastName(fullName.length > 1 ? fullName[1] : "")
                .consigneeAddress1(address)
                .consigneeAddress2("")
                .destinationCity(addressDetails.getCity())
                .destinationPinCode(branchAddress.getPinCode())
                .state(addressDetails.getArea() != null ? addressDetails.getArea() : addressDetails.getCity())
                .patientPhoneNumber(shipmentCreateIncomingRequest.getPhoneNumber())
                // Set pharmacy name, address
                .vendorName(logisticProvider.getUsername())
                .vendorAddress(branchAddress.getFormattedAddress())
                .vendorCity(branchAddress.getCity())
                .pickupPinCode(branchAddress.getPinCode())
                .vendorPhoneNumber(branchAddress.getPhoneNumber())
                // RTO is the return to origin details, it should be same as vendor name and address
                .rtoVendorName(logisticProvider.getUsername())
                .rtoAddress(branchAddress.getFormattedAddress())
                .rtoCity(branchAddress.getCity())
                .rtoPinCode(branchAddress.getPinCode())
                .rtoPhone(branchAddress.getPhoneNumber())
                // if patient share is greater than zero then pay_type is COD (cash on delivery) else PPD(prepaid)
                .payType(shipmentCreateIncomingRequest.getPatientShare().getValue().compareTo(BigDecimal.ZERO) > 0 ? TawseelShipmentPaymentType.COD.name() : TawseelShipmentPaymentType.PPD.name())
                .itemDescription(String.format("Alma health order request, orderId:%s", shipmentCreateIncomingRequest.getShipmentId()))
                .quantity(String.valueOf(shipmentCreateIncomingRequest.getTotalShipmentQuantity()))
                // Patient share, the amount the patient will pay to the driver
                .collectableValue(shipmentCreateIncomingRequest.getPatientShare().getValue().toString())
                // Original total Price of the order
                .productValue(shipmentCreateIncomingRequest.getTotalPrice().getValue().toString())
                .actualWeight(ACTUAL_WEIGHT_DEFAULT_VALUE)
                .volumetricWeight(VOLUMETRIC_WEIGHT_DEFAULT_VALUE)
                .isOtpVerified(IS_OTP_VERIFIED).build();
    }

    /**
     * Validation on building create shipment request
     * Rules:
     * 1- Recipient is not empty
     * 2- addressDetails is not null
     * 3- logistic provider branch address
     * 4- logistic provider username (vendorName)
     * 5- logistic provider password (secretKey)
     * 6- logistic provider account id (apiKey)
     */
    private void validateCreateShipmentRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest, LogisticProvider logisticProvider) {
        // Patient name validation
        if (StringUtils.isEmpty(shipmentCreateIncomingRequest.getRecipient())) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, recipient field is empty, orderId: (%s)", shipmentCreateIncomingRequest.getShipmentId()));
        }

        if (shipmentCreateIncomingRequest.getAddressDetails() == null) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, addressDetails field is empty, orderId: (%s)", shipmentCreateIncomingRequest.getShipmentId()));
        }

        if (logisticProvider.getBranchAddress() == null) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, logisticProvider branch address column is not set in the DB, logistic provider code: (%s)", logisticProvider.getLogisticProviderCode()));
        }

        if (logisticProvider.getUsername() == null) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, logisticProvider username(vendorName) column is not set in the DB, logistic provider code: (%s)", logisticProvider.getLogisticProviderCode()));
        }

        if (logisticProvider.getAccountId() == null) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, logisticProvider accountId(apiKey) column is not set, logistic provider code: (%s)", logisticProvider.getLogisticProviderCode()));
        }

        if (logisticProvider.getPassword() == null) {
            throw new BadRequestException(String.format("!Error: Tawseel shipment creation failed, logisticProvider password(secretKey) column is not set in DB, logistic provider code: (%s)", logisticProvider.getLogisticProviderCode()));
        }

    }

}
