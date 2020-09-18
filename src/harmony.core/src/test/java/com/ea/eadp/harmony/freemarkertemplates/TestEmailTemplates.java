/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.freemarkertemplates;

import com.ea.eadp.harmony.shared.config.HarmonyEnvironment;
import com.ea.eadp.harmony.shared.ftl.TemplateRenderer;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class TestEmailTemplates {

    private TemplateRenderer emailRenderer;

    public TestEmailTemplates() throws Exception {
        emailRenderer = new TemplateRenderer();
        emailRenderer.afterPropertiesSet();
    }

    @Test
    public void testEmailContent() {
        Map dataModel = new HashMap();
        dataModel.put("mail_title", "Running count is wrong!");
        String emailContent = emailRenderer.renderTemplate(getClass(), "testtemplate",
                dataModel);

        Assert.hasText(emailContent, "Content can't be empty");
        Assert.isTrue(emailContent.contains("Running count is wrong"), "Content should contain the text");

        HarmonyEnvironment environment = new HarmonyEnvironment();
        environment.setCluster("TestCluster");
        environment.setUniverse("TestUniverse");
        environment.setApplication("TestApp");
        environment.setClusterType("TestType");
        environment.setNode("TestNode");

        dataModel.put("environment", environment);
        emailContent = emailRenderer.renderTemplate(getClass(),"templateWithEnvironment", dataModel);
        Assert.hasText(emailContent,"TestNode");
    }
}
