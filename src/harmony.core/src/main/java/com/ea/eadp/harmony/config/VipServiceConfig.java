/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.config.annotation.ServiceProperty;

/**
 * Created by leilin on 10/15/2014.
 */
public class VipServiceConfig extends BaseServiceConfig {
    @ServiceProperty("service.vip.writerInterface")
    private String writerInterface;

    @ServiceProperty("service.vip.writerVip")
    private String writerVip;

    @ServiceProperty("service.vip.readerInterface")
    private String readerInterface;

    @ServiceProperty("service.vip.readerVip")
    private String readerVip;

    @ServiceProperty("service.vip.autoethdiscovery")
    private Boolean autoEthDiscovery = true;

    public String getWriterInterface() {
        return writerInterface;
    }

    public void setWriterInterface(String writerInterface) {
        this.writerInterface = writerInterface;
    }

    public String getWriterVip() {
        return writerVip;
    }

    public void setWriterVip(String writerVip) {
        this.writerVip = writerVip;
    }

    public String getReaderInterface() {
        return readerInterface;
    }

    public void setReaderInterface(String readerInterface) {
        this.readerInterface = readerInterface;
    }

    public String getReaderVip() {
        return readerVip;
    }

    public void setReaderVip(String readerVip) {
        this.readerVip = readerVip;
    }

    public void setAutoEthDiscovery(Boolean autoEthDiscovery) {
        this.autoEthDiscovery = autoEthDiscovery;
    }

    public boolean getAutoEthDiscovery() {
        return autoEthDiscovery;
    }
}
