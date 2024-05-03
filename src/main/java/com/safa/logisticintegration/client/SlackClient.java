package com.safa.logisticintegration.client;

import com.safa.logisticintegration.data.dto.slack.SlackPostMessage;
import com.safa.logisticintegration.data.dto.slack.SlackPostMessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * This interface using FeignClient which is the replacement of the restTemplates,
 * Add the external api you want to call in rest apis code structure the same way you create rest api in the rest controller
 *
 * @author Qusai safa
 */
@FeignClient(name = "slackClient", url = "${client.slack.baseUrl}")
public interface SlackClient {

    @PostMapping()
    SlackPostMessageResponse sendSlackMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken, SlackPostMessage message);
}
