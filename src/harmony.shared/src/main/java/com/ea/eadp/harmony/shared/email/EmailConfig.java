/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by juding on 2/24/16.
 */
@Component
public class EmailConfig {

    @Value("${harmony.email.smtp_enabled}")
    private boolean smtpEnabled;

    @Value("${harmony.email.smtp_host}")
    private String smtpHost;

    @Value("${harmony.email.smtp_port}")
    private int smtpPort;

    @Value("${harmony.email.from_addr}")
    private String fromAddr;

    @Value("${harmony.email.to_addr}")
    private String toAddr;

    @Value("${harmony.email.enabled_category}")
    private EmailCategory enabledCategory;

    public boolean isSmtpEnabled() {
        return smtpEnabled;
    }

    public void setSmtpEnabled(boolean smtpEnabled) {
        this.smtpEnabled = smtpEnabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public EmailCategory getEnabledCategory() {
        return enabledCategory;
    }

    public void setEnabledCategory(EmailCategory enabledCategory) {
        this.enabledCategory = enabledCategory;
    }
}
