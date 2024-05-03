package com.safa.logisticintegration.data.dto.transcorp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Request for update order send by Transcorp to webhook API
 *
 * @Authour Qusai
 */
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscorpUpdateIncomingRequest {

    @JsonProperty("trigger_id")
    private Long triggerId;

    @JsonProperty("trigger_name")
    private String triggerName;

    @JsonProperty("task_id")
    private Long taskId;

    @JsonProperty("task_awb")
    private String taskAwb;

    @JsonProperty("task_status")
    private String taskStatus;

    @JsonProperty("task_customerorderno")
    private String taskCustomerOrderNo;

    @JsonProperty("task_completeafterdate")
    private String taskCompleteAfterDate;

    @JsonProperty("task_completeaftertime")
    private String taskCompleteAfterTime;

    @JsonProperty("task_completebeforetime")
    private String taskCompleteBeforeTime;

    @JsonProperty("task_consigneename")
    private String taskConsigneeName;

    @JsonProperty("task_conscity")
    private String taskConsCity;

    @JsonProperty("task_conscountryname")
    private String taskConsCountryName;

    @JsonProperty("task_failurereason")
    private String taskFailureReason;

    @JsonProperty("task_conscontactphone")
    private Date taskConsContactPhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("timestamp")
    private Date timestamp;

}
