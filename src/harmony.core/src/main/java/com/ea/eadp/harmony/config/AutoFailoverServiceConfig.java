/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.config;

import com.ea.eadp.harmony.config.annotation.ServiceProperty;

/**
 * Created by juding on 11/14/14.
 */
public class AutoFailoverServiceConfig extends VipServiceConfig {
    @ServiceProperty("autofailover.mode")
    private AutoFailoverMode autoFailoverMode;

    @ServiceProperty("autofailover.maxQuota")
    private long autoFailoverMaxQuota;

    @ServiceProperty("autofailover.grace")
    private long autoFailoverGrace;

    @ServiceProperty("autofailover.fresh")
    private long autoFailoverFresh;

    public AutoFailoverMode getAutoFailoverMode() {
        return autoFailoverMode;
    }

    public void setAutoFailoverMode(AutoFailoverMode autoFailoverMode) {
        this.autoFailoverMode = autoFailoverMode;
    }

    public long getAutoFailoverMaxQuota() {
        return autoFailoverMaxQuota;
    }

    public void setAutoFailoverMaxQuota(long autoFailoverMaxQuota) {
        this.autoFailoverMaxQuota = autoFailoverMaxQuota;
    }

    public long getAutoFailoverGrace() {
        return autoFailoverGrace;
    }

    public void setAutoFailoverGrace(long autoFailoverGrace) {
        this.autoFailoverGrace = autoFailoverGrace;
    }

    public long getAutoFailoverFresh() {
        return autoFailoverFresh;
    }

    public void setAutoFailoverFresh(long autoFailoverFresh) {
        this.autoFailoverFresh = autoFailoverFresh;
    }
}
