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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.qmotion.internal.protocol.RemoteControllerException;
import org.openhab.binding.qmotion.internal.protocol.ShadeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QmotionShadeGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim Moran - Initial contribution
 */
@NonNullByDefault
public class QmotionShadeGroupHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QmotionShadeGroupHandler.class);

    @Nullable
    private ShadeGroupConfiguration config;

    public QmotionShadeGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!QmotionBindingConstants.CHANNEL_SHADE_POSITION.equals(channelUID.getId())) {
            logger.warn("Invalid channel passed to QmotionShadeGroup [{}]", channelUID.getId());
            return;
        }

        try {
            if (command instanceof PercentType) {
                ShadePosition position = ShadePosition.fromPercentType((PercentType) command);
                setPosition(position);
            } else if (command instanceof UpDownType) {
                ShadePosition position = ShadePosition.fromUpDownType((UpDownType) command);
                setPosition(position);
            } else if (command instanceof StopMoveType) {
                logger.warn("Qmotion shades do not support StopMove commands");
            } else if (command instanceof RefreshType) {
                // This is a fire and forget protocol, cannot refresh unfortunately.
            } else {
                logger.warn("Did not understand type of command [{}]", command);
            }
        } catch (RemoteControllerException e) {
            logger.warn("Exception when trying to set shade position!");
            logger.debug("Exception when trying to set shade position!", e);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ShadeGroupConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = false;
            if (validConfiguration(config)) {
                // TODO: Call bridge and get list of groups, confirm this group is good
                thingReachable = true;
            }
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

    }

    private boolean validConfiguration(@Nullable ShadeGroupConfiguration config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Qsync configuration missing");

            return false;
        }

        if (StringUtils.isEmpty(config.getId())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Shade id not specified");

            return false;
        }

        try {
            getShadeId();
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Shade id cannot be parsed as an integer");
        }

        return true;
    }

    private void setPosition(ShadePosition position) throws RemoteControllerException {
        QsyncHandler bridge;
        if ((bridge = getBridgeHandler()) == null) {
            return;
        }

        ShadeCommand command = new ShadeCommand(getShadeId(), position);
        bridge.sendCommand(command);

        updatePosition(position);

    }

    private void updatePosition(ShadePosition position) {
        updateState(QmotionBindingConstants.CHANNEL_SHADE_POSITION, new PercentType(position.getPercent()));
    }

    protected @Nullable QsyncHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("Thing {} must belong to a hub", getThing().getThingTypeUID().getId());
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof QsyncHandler)) {
            logger.debug("Thing {} belongs to the wrong hub type", getThing().getThingTypeUID().getId());
            return null;
        }
        return (QsyncHandler) handler;
    }

    private int getShadeId() throws NumberFormatException {
        String shadeId = getConfigAs(ShadeGroupConfiguration.class).id;

        return Integer.valueOf(shadeId);
    }

}
