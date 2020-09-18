/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.entity;

/**
 * Created by juding on 10/21/2014.
 */
public class MasterStatusDB {
    private String file;
    private long position;

    public MasterStatusDB() {
    }

    public MasterStatusDB(String file, long position) {
        this.file = file;
        this.position = position;
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

    @Override
    public String toString() {
        return "MasterStatusDB{" +
                "file='" + file + '\'' +
                ", position=" + position +
                '}';
    }
}
