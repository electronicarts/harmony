/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.mysql.config;

import com.ea.eadp.harmony.config.BaseServiceConfigFactory;
import org.springframework.stereotype.Component;

/**
 * Created by leilin on 10/16/2014.
 */
@Component
public class MySqlServiceConfigFactory extends BaseServiceConfigFactory<MySqlServiceConfig> {
    public MySqlServiceConfigFactory() {
        super(MySqlServiceConfig.class);
    }
}
