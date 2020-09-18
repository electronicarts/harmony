/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

/**
 * User: leilin
 * Date: 10/8/14
 */
public class EventServiceRegisterListener implements ApplicationListener<ApplicationContextEvent> {
    private final static Logger logger = LoggerFactory.getLogger(EventServiceRegisterListener.class);

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // resolve event service
        EventService eventService = event.getApplicationContext().getBean(EventService.class);

        if (eventService == null) {
            logger.warn("EventService is not initialized");
            return;
        }

        if (event instanceof ContextRefreshedEvent) {
            handleContextRefreshedEvent((ContextRefreshedEvent) event, eventService);
        } else if (event instanceof ContextClosedEvent) {
            try {
                handleContextClosedEvent((ContextClosedEvent) event, eventService);
            } catch (Exception e) {
                logger.error("Exception happened while trying to close service.", e);
            }
        } else {
            logger.warn("Ignoring event " + event);
        }
    }

    private void handleContextRefreshedEvent(ContextRefreshedEvent event, EventService eventService) {
        // register event handlers
        Map<String, Object> eventHandlers = event.getApplicationContext().getBeansWithAnnotation(EventHandlerClass.class);
        for (Object handler : eventHandlers.values()) {
            eventService.registerHandler(handler);
        }

        // raise application started event
        eventService.raiseEvent(new BeforeApplicationStartedEvent(event.getApplicationContext()));
        eventService.raiseEvent(new ApplicationStartedEvent(event.getApplicationContext()));
        eventService.raiseEvent(new AfterApplicationStartedEvent(event.getApplicationContext()));
    }

    private void handleContextClosedEvent(ContextClosedEvent event, EventService eventService) {
        // raise application closed event
        eventService.raiseEvent(new ApplicationClosedEvent(event.getApplicationContext()));

        // raise after application closed Event
        eventService.raiseEvent(new AfterApplicationClosedEvent(event.getApplicationContext()));
    }
}
