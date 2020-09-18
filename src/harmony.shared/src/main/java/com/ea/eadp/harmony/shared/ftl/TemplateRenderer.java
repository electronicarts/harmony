/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.ftl;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by VincentZhang on 5/22/2018.
 */
@Component
public class TemplateRenderer implements InitializingBean {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TemplateRenderer.class);

    Version freeMarkerVersion = Configuration.VERSION_2_3_27;
    private Configuration cfg = new Configuration(freeMarkerVersion);

    private Set<Class> templateLoadingClassSet = Collections.newSetFromMap(new ConcurrentHashMap<Class, Boolean>());

    private MultiTemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{});

    public String renderTemplate(Class clz, String templateName, Map dataModel) {
        if (!templateLoadingClassSet.contains(clz)) {
            List<TemplateLoader> templateLoaders = new ArrayList<>();
            for (int i = 0; i < loader.getTemplateLoaderCount(); i++) {
                templateLoaders.add(loader.getTemplateLoader(i));
            }

            templateLoaders.add(new ClassTemplateLoader(clz, "/templates/"));

            loader = new MultiTemplateLoader(templateLoaders.toArray(new TemplateLoader[templateLoaders.size()]));
            cfg.setTemplateLoader(loader);

            templateLoadingClassSet.add(clz);
        }

        String templateFile = templateName + ".ftl";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Writer out = new OutputStreamWriter(baos);
            Template template = cfg.getTemplate(templateFile);
            template.process(dataModel, out);
        } catch (IOException e) {
            logger.error("IOException happened while trying to retrieve email template", e);
        } catch (TemplateException e) {
            logger.error("Template exception happened while trying to parse email template", e);
        }

        return baos.toString();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(freeMarkerVersion).build());
        cfg.setTemplateLoader(loader);
        // cfg.setClassForTemplateLoading(EmailSender.class, "/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

}
