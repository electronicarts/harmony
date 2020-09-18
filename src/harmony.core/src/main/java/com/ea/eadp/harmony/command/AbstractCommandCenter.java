/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.config.ServiceConfig;
import com.ea.eadp.harmony.config.ServiceConfigRepository;
import com.ea.eadp.harmony.shared.HarmonyUtils;
import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leilin on 10/21/2014.
 */
public abstract class AbstractCommandCenter implements CommandCenter {
    private final static Logger logger = LoggerFactory.getLogger(AbstractCommandCenter.class);

    private Map<Class<?>, CommandHandlerInfo> commandHandlerInfoMap = new HashMap<Class<?>, CommandHandlerInfo>();

    @Value("${command.timeout.connect}")
    private int timeoutConnect;

    @Value("${command.timeout.read}")
    private int timeoutRead;

    @Autowired
    private HarmonyEnvironment localEnvironment;

    @Autowired
    protected ServiceConfigRepository serviceConfigRepository;

    public AbstractCommandCenter() {
        registerCommandHandlers(this);
    }

    @Override
    public HarmonyCommandResult executeCommand(HarmonyCommand command) {
        return executeCommand(command, true);
    }

    @Override
    public HarmonyCommandResult executeCommand(HarmonyCommand command, boolean throwOnError) {
        return executeCommand(command, throwOnError, false);
    }

    @Override
    public HarmonyCommandResult executeCommand(HarmonyCommand command, boolean throwOnError, boolean localOnly) {
        HarmonyCommandResult result;
        try {
            logger.info("Executing command: {}", command);

            // If target not given, use local
            if(command.getTarget() == null || localOnly || localEnvironment.equals(new HarmonyEnvironment(command.getTarget()))){
                logger.info("Executing command {} locally", command);
                result = executeLocalCommand(command);
                logger.info("Complete command with result {} locally", result);
            }else{
                // sending remote command
                ServiceConfig config = serviceConfigRepository.getServiceConfig(command.getTarget());
                logger.info("Sending command {} to remote endpoint {}", command, config.getCommandEndpoint());
                result = executeRemoteCommand(command, config.getCommandEndpoint());
                logger.info("Receiving result {} from remote endpoint {}", result, config.getCommandEndpoint());
            }
            return result;
        } catch (Exception ex) {
            if (throwOnError) {
                if (ex instanceof CommandExecutionFailedException) {
                    throw (CommandExecutionFailedException) ex;
                } else {
                    throw new CommandExecutionFailedException("Failed to execute command:" + command, ex);
                }
            } else {
                return getResultFromException(ex);
            }
        }
    }

    protected HarmonyCommandResult executeRemoteCommand(HarmonyCommand command, String commandEndpoint) {
        RestTemplate restTemplate = new RestTemplate();

        // Set timeout values
        SimpleClientHttpRequestFactory requestFactory =
                (SimpleClientHttpRequestFactory)restTemplate.getRequestFactory();
        requestFactory.setConnectTimeout(timeoutConnect);
        requestFactory.setReadTimeout(timeoutRead);

        HarmonyCommandResult result = restTemplate.postForObject(commandEndpoint, command, HarmonyCommandResult.class);
        return result;
    }

    public HarmonyCommandResult executeLocalCommand(HarmonyCommand command) {
        Class<?> commandType = command.getClass();
        CommandHandlerInfo handlerInfo = commandHandlerInfoMap.get(commandType);

        if (handlerInfo != null) {
            return executeHandler(command, handlerInfo);
        }

        // no handler found
        throw new RuntimeException("No handler found for command: " + commandType);
    }

    protected HarmonyCommandResult getResultFromException(final Exception ex) {
        HarmonyCommandResult result = new HarmonyCommandResult();
        result.setResultType(ResultType.FAILED);
        result.setErrorMessage(HarmonyUtils.exceptionToString(ex));
        return result;
    }

    private void registerCommandHandlers(Object commandHandler) {
        Class<?> handlerType = commandHandler.getClass();

        Class<?> baseType = handlerType;
        while (baseType != null) {
            registerHandlerForType(this, baseType);
            baseType = baseType.getSuperclass();
        }
    }

    private void registerHandlerForType(Object handlerInstance, Class<?> handlerInterface) {
        for (Method method : handlerInterface.getDeclaredMethods()) {
            CommandHandler handlerAnnotation = method.getAnnotation(CommandHandler.class);
            if (handlerAnnotation != null) {
                Class<?> eventType = handlerAnnotation.value();
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

                CommandHandlerInfo eventHandlerInfo = new CommandHandlerInfo(handlerInstance, method, parameterless);
                registerCommandHandler(eventType, eventHandlerInfo);
            }
        }
    }

    private void registerCommandHandler(Class<?> commandType, CommandHandlerInfo eventHandlerInfo) {
        if (commandHandlerInfoMap.containsKey(commandType)) {
            throw new IllegalStateException("Duplicate handlers for command: " + commandType);
        }
        commandHandlerInfoMap.put(commandType, eventHandlerInfo);
    }

    private HarmonyCommandResult executeHandler(HarmonyCommand command, CommandHandlerInfo handlerInfo) {
        try {
            if (handlerInfo.isParameterless()) {
                return (HarmonyCommandResult) handlerInfo.getMethod().invoke(handlerInfo.getInstance());
            } else {
                return (HarmonyCommandResult) handlerInfo.getMethod().invoke(handlerInfo.getInstance(), command);
            }
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to execute command handler", ex);
        }
    }


    private class CommandHandlerInfo {
        private Object instance;
        private Method method;
        private boolean parameterless;

        public Object getInstance() {
            return instance;
        }

        public Method getMethod() {
            return method;
        }

        public boolean isParameterless() {
            return parameterless;
        }

        private CommandHandlerInfo(Object instance, Method method, boolean useEventArgument) {
            this.instance = instance;
            this.method = method;
            this.parameterless = useEventArgument;
        }
    }
}
