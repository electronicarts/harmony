/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command;

import com.ea.eadp.harmony.cluster.entity.exceptions.MethodParseErrorException;
import com.ea.eadp.harmony.command.commands.EnvironmentSetting;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by VincentZhang on 4/20/2018.
 */
@Component
public class CommandProcessor implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    public static String HARMONY_COMMAND_KEY = "harmony.command";

    @Value("${harmony.env.application}")
    private String application;

    @Value("${harmony.env.universe}")
    private String universe;

    @Value("${harmony.env.clusterType}")
    private String clusterType;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private BeanScanner beanScanner;

    @Autowired
    private EnvironmentSetting environmentSetting;

    private boolean cliRunning = true;

    public void startReceivingCommands() {
        logger.debug("Begin listing all defined beans:");

        String prompt = "harmony\\" + universe + "\\" + clusterType + "\\" + application + ">";
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal = null;
        String rightPrompt = null;

        Completer completer = beanScanner.getCompleter();

        DefaultParser p = new DefaultParser();
        p.setEofOnUnclosedQuote(true);
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .parser(p)
                    .build();

            String commandToBeExecuted =  System.getProperty(HARMONY_COMMAND_KEY);
            if(commandToBeExecuted != null){
                ParsedLine pl = reader.getParser().parse(commandToBeExecuted, 0);
                terminal.writer().println(beanScanner.execute(pl));
                terminal.flush();
                cliRunning = false;
            }

            while (cliRunning) {
                try{
                    String line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    line = line.trim();
                    if(environmentSetting.isEcho()){
                        terminal.writer().println(line);
                        terminal.flush();
                    }

                    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                        terminal.writer().println("Quiting command line!");
                        terminal.flush();
                        break;
                    }

                    ParsedLine pl = reader.getParser().parse(line, 0);
                    terminal.writer().println(beanScanner.execute(pl));
                    terminal.flush();
                }catch(EndOfFileException e){
                    terminal.writer().print("Received EOF, quiting");
                    terminal.flush();
                    cliRunning = false;
                }
                catch (Exception e){
                    terminal.writer().print(e.getMessage());
                    terminal.writer().print(ExceptionUtils.getStackTrace(e));
                    terminal.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.exit(ctx, () -> 0);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.debug("Application ready!");
        try {
            beanScanner.scanAllBeans();
            startReceivingCommands();
        } catch (MethodParseErrorException e) {
            System.err.println("Exception happened while trying to parse method:" + e.getMethodName());
            e.printStackTrace();
        }
    }
}
