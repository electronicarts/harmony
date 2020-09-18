/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by VincentZhang on 5/29/2018.
 */
class EmailContent {
    private String mailTo;
    private String mailContent;
    private String mailTitle;

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }

    public String getMailTitle() {
        return mailTitle;
    }

    public void setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
            return true;

        if (obj instanceof EmailContent) {
            EmailContent other = (EmailContent) obj;
            if (Objects.equals(other.mailContent, this.mailContent) &&
                    Objects.equals(other.mailTitle, this.mailTitle) &&
                    Objects.equals(other.mailTo, this.mailTo)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mailTitle, mailTo, mailContent);
    }
}

@Component
public class EmailVelocityController {
    @Value("${harmony.email.velocitycontroller.capacity}")
    private int capacity;

    @Value("${harmony.email.velocitycontroller.intervalSec}")
    private int intervalSec;

    private ReentrantLock lock = new ReentrantLock();

    private LinkedHashMap<EmailContent, Long> lastMailSentTime = new LinkedHashMap<EmailContent, Long>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<EmailContent, Long> eldest) {
            return this.size() > capacity;
        }
    };

    public boolean overflow(String mailTo, String title, String content) {
        lock.lock();
        try {
            EmailContent emailContent = new EmailContent();
            emailContent.setMailContent(content);
            emailContent.setMailTo(mailTo);
            emailContent.setMailTitle(title);

            Long lastSentTime = lastMailSentTime.get(emailContent);
            if (null == lastSentTime) {
                // Never sent the email, impossible to overflow.
                return false;
            } else {
                if (lastSentTime + intervalSec * 1000L > System.currentTimeMillis()) {
                    // Last sent time within interval, overflow
                    return true;
                }
                return false;
            }

        } finally { // Unlock no matter what happened.
            lock.unlock();
        }
    }

    public void commit(String mailTo, String title, String content) {
        lock.lock();
        try {
            EmailContent emailContent = new EmailContent();
            emailContent.setMailContent(content);
            emailContent.setMailTo(mailTo);
            emailContent.setMailTitle(title);

            lastMailSentTime.put(emailContent, System.currentTimeMillis());
        } finally {
            lock.unlock();
        }

    }
}
