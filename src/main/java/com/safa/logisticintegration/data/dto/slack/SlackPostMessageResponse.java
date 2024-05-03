package com.safa.logisticintegration.data.dto.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackPostMessageResponse {
    @JsonProperty("ok")
    private boolean success;
    @JsonProperty("ts")
    private String threadId;
}
