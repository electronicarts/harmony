/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.service;

import com.ea.eadp.harmony.event.*;
import com.ea.eadp.harmony.shared.event.*;
import com.ea.eadp.harmony.shared.utils.HarmonyRunnable;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by leilin on 10/16/2014.
 */
@EventHandlerClass
@Component
public class ScheduleService extends ServiceSupport {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Value("${monitor.nodeInspection.interval}")
    private long nodeInspectionInterval;

    @Value("${monitor.clusterCheck.interval}")
    private long clusterCheckInterval;

    @Value("${monitor.nodeWarmUp.interval}")
    private long nodeWarmUpInterval;

    private Timer nodeInspectionTimer;

    private Timer clusterCheckTimer;

    private Timer nodeWarmUpTimer;

    @EventHandler(AfterApplicationStartedEvent.class)
    public void onAfterApplicationStarted(AfterApplicationStartedEvent e) {
        // start inspection timer
        nodeInspectionTimer = new Timer("nodeInspectionTimer");
        nodeInspectionTimer.scheduleAtFixedRate(new RaiseEventTask(NodeInspectionEvent.class), nodeInspectionInterval, nodeInspectionInterval);

        // start clusterCheck timer
        clusterCheckTimer = new Timer("clusterCheckTimer");
        clusterCheckTimer.scheduleAtFixedRate(new RaiseEventTask(ClusterCheckEvent.class), clusterCheckInterval, clusterCheckInterval);

        // start warm up timer
        nodeWarmUpTimer = new Timer("nodeWarmUpTimer");
        nodeWarmUpTimer.scheduleAtFixedRate(new RaiseEventTask(NodeWarmUpEvent.class), nodeWarmUpInterval, nodeWarmUpInterval);
    }

    @EventHandler(ApplicationClosedEvent.class)
    public void onApplicationClosed(ApplicationClosedEvent e) {
        logger.info("Shutting down node check timer");
        nodeInspectionTimer.cancel();
        logger.info("Shutting down cluster check timer");
        clusterCheckTimer.cancel();
        logger.info("Shutting down warm up timer");
        nodeWarmUpTimer.cancel();
        logger.info("All clients shutdown!");
        onApplicatoinClosed();
    }

    public class RaiseEventTask extends TimerTask {
        private Class<? extends HarmonyEvent> eventType;

        public RaiseEventTask(Class<? extends HarmonyEvent> eventType) {
            this.eventType = eventType;
        }

        @Override
        public void run() {
            new HarmonyRunnable(HarmonyRunnable.HEALTH_CHECK) {
                @Override
                public void runInternal() {
                    try {
                        rasieEvent(eventType.getConstructor().newInstance());
                    } catch (Exception ex) {
                        logger.error("Error happened at timer", ex);
                    }
                }
            }.run();
        }
    }
}
