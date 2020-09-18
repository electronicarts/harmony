/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.shared.utils;

/**
 * Created by VincentZhang on 5/8/2018.
 */
public class ConsoleLog {
    private int level = 0;

    public void increaseLevel() {
        level++;
    }

    public void resetLevel() {
        level = 0;
    }

    public void decreaseLevel() {
        level--;
    }

    private String get_tabs() {
        String tabs = "";
        for (int i = 0; i < level; i++) {
            tabs += "\t";
        }
        return tabs;
    }

    public String error(String title, String msg) {
        return get_tabs() + title + ":" + ConsoleColors.RED  + msg + ConsoleColors.RESET + "\n";
    }

    public String ok(String title, String msg) {
        return get_tabs() + title + ":" + ConsoleColors.GREEN + msg + ConsoleColors.RESET + "\n";
    }

    public String info(String msg) {
        return get_tabs() + msg + "\n";
    }

    public String info(String title, String msg) {
        return info(title + ":" + msg);
    }
}
