/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.entity;

import com.ea.eadp.harmony.entity.LinkLocation;

/**
 * Created by juding on 10/30/2014.
 */
public class MasterLocation extends LinkLocation {
    private String host;
    private String file;
    private long position;

    public MasterLocation() {
    }

    public MasterLocation(String host, String file, long position) {
        this.host = host;
        this.file = file;
        this.position = position;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
