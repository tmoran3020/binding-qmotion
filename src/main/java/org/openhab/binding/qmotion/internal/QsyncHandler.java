/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.qmotion.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.qmotion.internal.protocol.RemoteController;
import org.openhab.binding.qmotion.internal.protocol.RemoteControllerException;
import org.openhab.binding.qmotion.internal.protocol.ShadeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QsyncHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim Moran - Initial contribution
 */
@NonNullByDefault
public class QsyncHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(QsyncHandler.class);

    @Nullable
    private QsyncConfiguration config;

    public QsyncHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands directly to the bridge
    }

    @Override
    public void initialize() {
        config = getConfigAs(QsyncConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            if (validConfiguration(this.config)) {
                boolean thingReachable = false;
                RemoteController controller = null;
                try {
                    controller = getRemoteController();
                    if (controller == null) {
                        return;
                    }
                    controller.openConnection();
                    thingReachable = true;
                } catch (RemoteControllerException e) {
                    String host = (this.config != null ? this.config.getIpAddress() : "null");
                    logger.debug("Could not connect to QsyncController at [{}])", host);
                } finally {
                    try {
                        if (controller != null) {
                            controller.closeConnection();
                        }
                    } catch (RemoteControllerException e) {
                        logger.debug("Could not close connection");
                    }
                }
                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        });
    }

    private boolean validConfiguration(@Nullable QsyncConfiguration config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Qsync configuration missing");

            return false;
        }

        if (StringUtils.isEmpty(config.getIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Qsync address not specified");

            return false;
        }

        return true;
    }

    public void sendCommand(ShadeCommand command) throws RemoteControllerException {
        RemoteController controller = getRemoteController();
        if (controller == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            throw new RemoteControllerException("Could not get RemoteController");
        }
        controller.sendCommand(command);
    }

    private @Nullable RemoteController getRemoteController() {
        QsyncConfiguration config = this.config;
        if (config == null) {
            return null;
        }
        return new RemoteController(config.getIpAddress());
    }
}
