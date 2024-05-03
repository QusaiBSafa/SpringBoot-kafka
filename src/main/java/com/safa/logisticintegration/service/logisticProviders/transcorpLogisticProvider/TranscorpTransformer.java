package com.safa.logisticintegration.service.logisticProviders.transcorpLogisticProvider;

import com.safa.logisticintegration.data.dto.order.OrderStatus;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderRequest;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpTriggerType;
import com.safa.logisticintegration.data.entity.LogisticProvider;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderTransformer;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.safa.logisticintegration.common.Constants.*;

/**
 * Transformer to transform the incoming request to Transcorp  Request Format
 */
@Component
public class TranscorpTransformer implements ILogisticProviderTransformer<TranscorpOrderRequest> {

    public static final String SEPERATOR = "_";

    @Override
    public TranscorpOrderRequest transformToThirdPartyRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest,
                                                              LogisticProvider provider) {
        final TranscorpOrderRequest transcorpOrderRequest = new TranscorpOrderRequest();
        final String orderId = shipmentCreateIncomingRequest.getShipmentId();
        transcorpOrderRequest.setReferenceNumber(orderId);
        transcorpOrderRequest.setCustomerOrderNumber(orderId);
        // Awb will be taken as a second level check after referenceId
        transcorpOrderRequest.setAwb(AWB_PREFIX.concat(SEPERATOR).concat(orderId));
        transcorpOrderRequest.setType(TYPE);
        transcorpOrderRequest.setDeliveryType(DELIVERY_TYPE);
        transcorpOrderRequest.setTotalShipmentQuantity(
                shipmentCreateIncomingRequest.getTotalShipmentQuantity());
        transcorpOrderRequest.setCompanyId(
                TRANSCORP_COMPANY_ID); // Represents transcorp at their service provider

        transcorpOrderRequest.setDeliveryDate(
                getDate(shipmentCreateIncomingRequest.getDeliveryStartTime()));

        // Required by Transcorp to send them the StartTime on 9:00AM UTC
        transcorpOrderRequest.setDeliveryStartTime("09:00");

        // Required by Transcorp to send them the EndTime on 9:00PM UTC
        transcorpOrderRequest.setDeliveryEndTime("21:00");

        TranscorpOrderRequest.Location location = new TranscorpOrderRequest.Location();
        location.setCity(shipmentCreateIncomingRequest.getAddressDetails().getCity());
        String apartment = getValueWithSeprator(
                shipmentCreateIncomingRequest.getAddressDetails().getApartment(), "-");
        String building = getValueWithSeprator(
                shipmentCreateIncomingRequest.getAddressDetails().getBuilding(), "-");
        String street = getValueWithSeprator(
                shipmentCreateIncomingRequest.getAddressDetails().getStreet(), "");

        location.setAddressLine1(apartment.concat(building).concat(street));
        location.setDistrict(shipmentCreateIncomingRequest.getAddressDetails().getArea());
        location.setCountryCode(shipmentCreateIncomingRequest.getAddressDetails().getCountry());
        location.setContactPhone(shipmentCreateIncomingRequest.getPhoneNumber());

        TranscorpOrderRequest.Consignee consignee = new TranscorpOrderRequest.Consignee();
        consignee.setName(shipmentCreateIncomingRequest.getRecipient());
        consignee.setLocation(location);
        transcorpOrderRequest.setConsignee(consignee);
        transcorpOrderRequest.setCustomerId(provider.getCustomerId());


        return transcorpOrderRequest;
    }


    private String getDate(Date deliveryTime) {
        SimpleDateFormat localDateFormat = new SimpleDateFormat("YYYY-MM-dd");
        return localDateFormat.format(deliveryTime);
    }

    private String getValueWithSeprator(String value, String c) {
        return value != null ? value.concat(c) : "";
    }

    @Override
    public String mapStatus(String transcorpStatus) {
        TranscorpTriggerType transcorpTriggerType = TranscorpTriggerType.valueOf(transcorpStatus);
        return switch (transcorpTriggerType) {
            case TASK_OUT_FOR_DELIVERY, OUT_FOR_DELIVERY, IN_TRANSIT -> OrderStatus.OUT_FOR_DELIVERY.getStatus();
            case TASK_FAILED, FAILED, RESCHEDULED -> OrderStatus.FAILED_DELIVERY_ATTEMPT.getStatus();
            case TASK_COMPLETED, DELIVERED, COMPLETED -> OrderStatus.DELIVERED.getStatus();
            default -> OrderStatus.READY_FOR_DELIVERY.getStatus();
        };
    }
}
