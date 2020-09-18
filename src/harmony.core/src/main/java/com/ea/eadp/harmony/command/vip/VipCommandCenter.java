/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.command.vip;

import com.ea.eadp.harmony.command.AbstractCommandCenter;
import com.ea.eadp.harmony.command.CommandHandler;
import com.ea.eadp.harmony.command.HarmonyCommandResult;
import com.ea.eadp.harmony.config.VipServiceConfig;
import com.ea.eadp.harmony.shared.config.ServiceEnvironment;
import com.ea.eadp.harmony.utils.VipWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by juding on 11/5/2014.
 */
public class VipCommandCenter extends AbstractCommandCenter {
    private final static Logger logger = LoggerFactory.getLogger(VipCommandCenter.class);

    @CommandHandler(VipCommand.class)
    public HarmonyCommandResult handleVipCommand(VipCommand command) {
        return handle(command);
    }

    private HarmonyCommandResult handle(VipCommand command) {
        ServiceEnvironment serviceEnvironment = command.getTarget();
        VipServiceConfig vipServiceConfig = (VipServiceConfig) serviceConfigRepository.getServiceConfig(serviceEnvironment);
        VipWrapper vipWrapper = new VipWrapper(vipServiceConfig);

        VipAction vipAction = command.getAction();
        String resultMessage = null;
        switch (vipAction) {
            case ADD_VIP:
                vipWrapper.addVip();
                resultMessage = "Done " + command;
                break;
            case DEL_VIP:
                vipWrapper.delVip();
                resultMessage = "Done " + command;
                break;
            case ADD_VIP_READ:
                vipWrapper.addVip(false, false);
                resultMessage = "Done " + command;
                break;
            case DEL_VIP_READ:
                vipWrapper.delVip(false, false);
                resultMessage = "Done " + command;
                break;
            case ADD_VIP_IPT:
                vipWrapper.addVip(true);
                resultMessage = "Done " + command;
                break;
            case DEL_VIP_IPT:
                vipWrapper.delVip(true);
                resultMessage = "Done " + command;
                break;
            case ADD_VIP_READ_IPT:
                vipWrapper.addVip(true, false);
                resultMessage = "Done " + command;
                break;
            case DEL_VIP_READ_IPT:
                vipWrapper.delVip(true, false);
                resultMessage = "Done " + command;
            default:
        }
        // construct the response
        logger.info(resultMessage);
        HarmonyCommandResult ret = new HarmonyCommandResult();
        ret.setResultMessage(resultMessage);
        return ret;
    }
}
