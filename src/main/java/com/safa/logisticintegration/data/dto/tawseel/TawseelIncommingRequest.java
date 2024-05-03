package com.safa.logisticintegration.data.dto.tawseel;


import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TawseelIncommingRequest {
    @JsonProperty("ref_no")
    private String referenceNumber;

    @JsonProperty("awbno")
    private String awbNumber;

    @JsonProperty("order_no")
    @JsonAlias("orderno")
    private String orderNumber;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_description")
    private String statusDescription;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("updated_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date updatedDate;

}
