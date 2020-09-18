/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.event;

import com.ea.eadp.harmony.shared.utils.HarmonyRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: leilin
 * Date: 10/8/14
 */
@Component
public class EventServiceImpl implements EventService {
    private final static Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private ExecutorService eventExeSvc = Executors.newFixedThreadPool(1);
    private ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventHandlerInfo>> eventMap = new ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventHandlerInfo>>();

    public void raiseEvent(final HarmonyEvent event) {
        //logger.info("Raise event : {}", ObjectMarshaller.marshal(event));
        handleEvent(event);
    }

    public void raiseEventAsync(final HarmonyEvent event) {
        eventExeSvc.submit(new HarmonyRunnable(HarmonyRunnable.getLogContext()) {
            public void runInternal() {
                handleEvent(event);
            }
        });
    }

    private void handleEvent(HarmonyEvent event) {
        CopyOnWriteArrayList<EventHandlerInfo> handlers = eventMap.get(event.getClass());
        if (handlers != null) {
            for (EventHandlerInfo handlerInfo : handlers) {
                executeHandler(event, handlerInfo);
            }
        }
    }

    public void registerHandler(Object handlerInstance) {
        Class<?> handlerType = handlerInstance.getClass();

        Class<?> baseType = handlerType;
        while (baseType != null) {
            registerHandlerForType(handlerInstance, baseType);
            baseType = baseType.getSuperclass();
        }

        for (Class<?> handlerInterface : handlerType.getInterfaces()) {
            if (!handlerInterface.isAnnotationPresent(EventHandlerClass.class)) {
                continue;
            }

            registerHandlerForType(handlerInstance, handlerInterface);
        }
    }

    private void registerHandlerForType(Object handlerInstance, Class<?> handlerInterface) {
        for (Method method : handlerInterface.getDeclaredMethods()) {
            EventHandler eventHandlerAnnotation = method.getAnnotation(EventHandler.class);
            if (eventHandlerAnnotation != null) {
                Class<?> eventType = eventHandlerAnnotation.value();
                Class<?>[] paramTypes = method.getParameterTypes();
                boolean parameterless = false;
                if (paramTypes == null || paramTypes.length == 0) {
                    parameterless = true;
                } else if (paramTypes.length == 1) {
                    if (!paramTypes[0].isAssignableFrom(eventType)) {
                        throw new IllegalStateException("Type defined in EventHandler annotation must be same as method parameter Type");
                    }
                } else {
                    throw new IllegalStateException("Method with EventHandler annotation cannot have more than one parameters");
                }

                EventHandlerInfo eventHandlerInfo = new EventHandlerInfo(handlerInstance, method, parameterless);
                registerEventHandler(eventType, eventHandlerInfo);
            }
        }
    }

    public void unregisterHandler(Object handlerInstance) {
        ArrayList<EventHandlerInfo> handlersToRemove = new ArrayList<EventHandlerInfo>();
        for (CopyOnWriteArrayList<EventHandlerInfo> handlers : eventMap.values()) {
            for (EventHandlerInfo eventHandlerInfo : handlers) {
                if (handlerInstance == eventHandlerInfo.getInstance()) {
                    handlersToRemove.add(eventHandlerInfo);
                }
            }
            handlers.removeAll(handlersToRemove);
            handlersToRemove.clear();
        }
    }

    private void registerEventHandler(Class<?> eventType, EventHandlerInfo eventHandlerInfo) {
        CopyOnWriteArrayList<EventHandlerInfo> handlers = eventMap.get(eventType);
        if (handlers == null) {
            handlers = new CopyOnWriteArrayList<EventHandlerInfo>();
            CopyOnWriteArrayList<EventHandlerInfo> existingHandlers = eventMap.putIfAbsent(eventType, handlers);
            handlers = existingHandlers == null ? handlers : existingHandlers;
        }

        handlers.add(eventHandlerInfo);
    }

    private void executeHandler(Object event, EventHandlerInfo handlerInfo) {
        try {
            if (handlerInfo.isParameterless()) {
                handlerInfo.getMethod().invoke(handlerInfo.getInstance());
            } else {
                handlerInfo.getMethod().invoke(handlerInfo.getInstance(), event);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to execute event handler", ex);
        }
    }

    @Override
    public void close() {
        if (eventExeSvc != null && !eventExeSvc.isTerminated()) {
            logger.info("Shutting down event service!");
            eventExeSvc.shutdown();
        }
    }

    private class EventHandlerInfo {
        private Object instance;
        private Method method;
        private boolean parameterless;

        private EventHandlerInfo(Object instance, Method method, boolean useEventArgument) {
            this.instance = instance;
            this.method = method;
            this.parameterless = useEventArgument;
        }

        public Object getInstance() {
            return instance;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isParameterless() {
            return parameterless;
        }
    }
}
