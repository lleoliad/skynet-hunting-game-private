package org.skynet.service.provider.hunting.obsolete.listener;

import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.context.event.SkynetApplicationReadyEvent;
import org.skynet.commons.context.event.SkynetContextClosedEvent;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ObsoleteContextEventListener {

    @Resource
    private GameEnvironment gameEnvironment;

    @Async("lazyTraceExecutor")
    @EventListener(value = {SkynetApplicationReadyEvent.class}, condition = "#skynetApplicationReadyEvent.uuid!=null")
    public void handleEvent(SkynetApplicationReadyEvent skynetApplicationReadyEvent) throws Exception {
        log.info("ObsoleteContextEventListener skynetApplicationReadyEvent:{}", skynetApplicationReadyEvent.getUuid());
        gameEnvironment.initialize();
    }

    @EventListener(value = {SkynetContextClosedEvent.class}, condition = "#skynetContextClosedEvent.uuid!=null")
    public void handleEvent(SkynetContextClosedEvent skynetContextClosedEvent) throws Exception {
        log.info("ObsoleteContextEventListener skynetContextClosedEvent:{}", skynetContextClosedEvent.getUuid());
    }
}
