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
package org.openhab.binding.qmotion.internal.protocol;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.qmotion.internal.ShadePosition;

/**
 * Encapsulates a command to control a shade group
 *
 * @author Tim Moran - Initial contribution
 */
public class ShadeCommand {

    private static byte[] HEADER_1 = new byte[] { 0x1b, 0x00 };
    private static byte[] HEADER_2 = new byte[] { 0x1b, 0x01, 0x00 };
    private static byte[] COMMAND = new byte[] { 0x1b, 0x05, 0x00, 0x00, 0x00, 0x09, 0x0e };

    private int groupId;
    private ShadePosition position;

    public ShadeCommand(int groupId, ShadePosition position) {
        this.groupId = groupId;
        this.position = position;
    }

    private byte[] getActualCommand() {
        byte[] sendCommand = COMMAND.clone();
        sendCommand[5] = (byte) groupId;
        sendCommand[6] = position.getCommandCode();
        return sendCommand;
    }

    public List<byte[]> getCommandBytes() {
        List<byte[]> commands = new ArrayList<byte[]>();

        commands.add(HEADER_1);
        commands.add(HEADER_2);
        commands.add(getActualCommand());

        return commands;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(fromByteArray(HEADER_1));
        sb.append("/n");
        sb.append(fromByteArray(HEADER_2));
        sb.append("/n");
        sb.append(fromByteArray(getActualCommand()));
        sb.append("/n");
        return sb.toString();
    }

    public String fromByteArray(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (byte inputByte : input) {
            sb.append(String.format("%02x", inputByte));
            sb.append(" ");
        }
        return sb.toString();
    }
}
