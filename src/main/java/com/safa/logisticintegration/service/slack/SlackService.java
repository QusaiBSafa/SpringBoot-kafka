package com.safa.logisticintegration.service.slack;

import com.safa.logisticintegration.client.SlackClient;
import com.safa.logisticintegration.data.dto.slack.SlackPostMessage;
import com.safa.logisticintegration.data.dto.slack.SlackPostMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SlackService {

    private final SlackClient slackClient;

    private final SlackConfig slackConfig;

    @Autowired
    public SlackService(SlackClient slackClient, SlackConfig slackConfig) {
        this.slackClient = slackClient;
        this.slackConfig = slackConfig;
    }

    @Async
    public Optional<SlackPostMessageResponse> postErrorMessage(final String message) {
        return Optional.ofNullable(slackClient.sendSlackMessage(slackConfig.getSlackAccessToken(), new SlackPostMessage("ERROR", ":interrobang:", slackConfig.getErrorChannelId(), message)));
    }

}
