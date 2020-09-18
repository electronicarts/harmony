/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.email;

import com.ea.eadp.harmony.shared.utils.HarmonyRunnable;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by juding on 2/24/16.
 */
@Component
public class EmailServiceImpl implements InitializingBean, EmailService {
    @Autowired
    private EmailConfig config;

    private static final Logger logger =
            LoggerFactory.getLogger(EmailServiceImpl.class.getName());

    private static final String MSG_TAG = "Email Service: ";

    private Session mailSession;
    private Transport mailTrans;
    private LinkedBlockingQueue<Runnable> mailBlcQue;
    private ThreadPoolExecutor mailExeSvc;

    @Autowired
    EmailVelocityController emailVelocityController;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!config.isSmtpEnabled())
            return;

        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.from", config.getFromAddr());
        mailSession = Session.getInstance(props);
        mailTrans = mailSession.getTransport("smtp");

        /* Create a thread pool with at most 1 thread.
         * The thread will die after idling for 60 seconds.
         * When a new task comes in and there is no thread, a new thread will
         * be created.
         * Use an unbounded queue to buffer new tasks when the thread is busy.
         */
        mailBlcQue = new LinkedBlockingQueue<Runnable>();
        mailExeSvc = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, mailBlcQue);
        mailExeSvc.setThreadFactory(
                new ThreadFactoryBuilder().setNameFormat("email-thread-%d").build()
        );
    }

    private boolean openSession() {
        try {
            mailTrans.connect();
        } catch (Exception e) {
            logger.error(MSG_TAG, e);
            return false;
        }
        return true;
    }

    private boolean closeSession() {
        try {
            mailTrans.close();
        } catch (Exception e) {
            logger.error(MSG_TAG, e);
            return false;
        }
        return true;
    }

    private void sendMail(String mailTo, String mailSbj, String mailTxt) {
        // Connect if not connected yet.
        if (!mailTrans.isConnected() && !openSession()) {
            return;
        }

        try {
            String[] toAddrs = mailTo.split(",");
            InternetAddress[] intAddrs = new InternetAddress[toAddrs.length];
            for (int i = 0; i < toAddrs.length; i++)
                intAddrs[i] = new InternetAddress(toAddrs[i]);

            MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom();
            msg.setRecipients(Message.RecipientType.TO, intAddrs);
            msg.setSubject(mailSbj);
            msg.setSentDate(new Date());
            Multipart multipart = new MimeMultipart("alternative");
            MimeBodyPart htmlPart = new MimeBodyPart();

            // Outlook: We removed extra line breaks from this message.
            mailTxt = "  " + mailTxt.replace("\n", "\n  ");
            htmlPart.setContent(mailTxt, "text/html; charset=utf-8");

            multipart.addBodyPart(htmlPart);
            msg.setContent(multipart);

            mailTrans.sendMessage(msg, intAddrs);
        } catch (Exception e) {
            logger.error(MSG_TAG, e);
        } finally {
            // Disconnect when no task left in the queue.
            if (mailExeSvc.getQueue().isEmpty()) {
                closeSession();
            }
        }
    }

    private void postEmail(final String mailTo, final String mailSbj, final String mailTxt) {
        if (mailExeSvc == null)
            return;
        mailExeSvc.execute(new HarmonyRunnable(HarmonyRunnable.getLogContext()) {
            public void runInternal() {
                if (!emailVelocityController.overflow(mailTo, mailSbj, mailTxt)) {
                    sendMail(mailTo, mailSbj, mailTxt);
                    emailVelocityController.commit(mailTo, mailSbj, mailTxt);
                }
            }
        });
    }

    private void postEmail(final String mailSbj, final String mailTxt) {
        postEmail(config.getToAddr(), mailSbj, mailTxt);
    }

    @Override
    public void postEmail(final EmailCategory mailCtg, final String mailSbj, final String mailTxt) {
        // Do nothing if mailCtg is less than the enabled category
        if (mailCtg.compareTo(config.getEnabledCategory()) < 0)
            return;
        postEmail(mailCtg.name() + " - " + mailSbj,
                mailTxt);
    }

    @Override
    public void close() {
        if (mailExeSvc != null && mailExeSvc.isTerminated() == false) {
            logger.info("Shutting down email service");
            mailExeSvc.shutdown();
        }
    }

    @Override
    public boolean isTerminated() {
        return mailExeSvc.isTerminated();
    }
}
