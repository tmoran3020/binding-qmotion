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

import java.util.EnumSet;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;

/**
 * The position of a shade
 *
 * @author Tim Moran - Initial contribution
 */
public enum ShadePosition {
    POSITION_0(0, (byte) 0x01),
    POSITION_12_5(125, (byte) 0x06),
    POSITION_25(250, (byte) 0x07),
    POSITION_37_5(375, (byte) 0x09),
    POSITION_50(500, (byte) 0x08),
    POSITION_62_5(625, (byte) 0x0b),
    POSITION_75(750, (byte) 0x0c),
    POSITION_87_5(875, (byte) 0x0e),
    POSITION_100(1000, (byte) 0x02);

    // Half the distance to the next bucket for calcs
    private static final int HALF_BUCKET_DISTANCE = 63;

    ShadePosition(int percentTimesTen, byte commandCode) {
        this.percentTimesTen = percentTimesTen;
        this.commandCode = commandCode;
    }

    private int percentTimesTen;
    private byte commandCode;

    public byte getCommandCode() {
        return commandCode;
    }

    public int getPercent() {
        return percentTimesTen / 10;
    }

    public static ShadePosition fromUpDownType(UpDownType upDown) {
        if (upDown == UpDownType.UP) {
            return POSITION_0;
        }
        return POSITION_100;
    }

    public static ShadePosition fromPercentType(PercentType percent) {
        return fromFloat(percent.floatValue());
    }

    private static ShadePosition fromFloat(float input) {
        int percentTimesTen = (int) input * 10;

        EnumSet<ShadePosition> allPositions = EnumSet.allOf(ShadePosition.class);

        for (ShadePosition position : allPositions) {
            if (position.percentTimesTen + HALF_BUCKET_DISTANCE >= percentTimesTen) {
                return position;
            }
        }
        return POSITION_100;
    }
}
