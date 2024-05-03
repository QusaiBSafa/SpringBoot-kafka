package com.safa.logisticintegration.service.slack;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SlackConfig {

    private static final String BEARER = "Bearer ";

    @Value("${slack.channel.error}")
    private String errorChannelId;

    @Value("Bearer ${slack.token}")
    private String slackAccessToken;

}
