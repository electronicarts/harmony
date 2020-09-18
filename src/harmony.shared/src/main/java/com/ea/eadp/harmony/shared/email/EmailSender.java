/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// Use free marker to send email.
@Component
public class EmailSender {

    @Autowired
    private EmailService emailService;
    @Autowired
    private TemplateRenderer templateRenderer;
    @Autowired
    private HarmonyEnvironment environment;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmailSender.class);

    public void postEmail(EmailCategory emailCategory, Class clz, String templateName, String title, Map dataModel) {
        dataModel.put("universe", environment.getUniverse());
        dataModel.put("cluster", environment.getCluster());
        dataModel.put("mail_title", title);

        String emailContent = templateRenderer.renderTemplate(clz, templateName, dataModel);
        emailService.postEmail(emailCategory, "/" + environment.getUniverse() +
                "/" + environment.getCluster() + ":" + title, emailContent);
    }

    public boolean isTerminated() {
        return emailService.isTerminated();
    }

    public void close() {
        emailService.close();
    }
}
