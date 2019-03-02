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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteController} is responsible for sending commands to the
 * QsyncController.
 *
 * @see <a
 *      href="https://github.com/devbobo/qmotion/blob/master/Protocol.md">
 *      https://github.com/devbobo/qmotion/blob/master/Protocol.md</a>
 *
 *      Largely borrowed from samsungtv.internal.protocol.RemoteController
 *
 * @author Tim Moran - Initial contribution
 */
public class RemoteController {

    private static final int CONNECTION_TIMEOUT = 500;

    private static Object lock = new Object();

    private final Logger logger = LoggerFactory.getLogger(RemoteController.class);

    private String host;
    private int TCP_PORT = 9760;

    private Socket socket;
    private OutputStream os;

    /**
     * Create and initialize remote controller instance.
     *
     * @param host IP address (or host name) of Qsync controller.
     */
    public RemoteController(String host) {
        this.host = host;
    }

    /**
     * Open Connection to Samsung TV.
     *
     * @throws RemoteControllerException
     */
    public void openConnection() throws RemoteControllerException {
        logger.debug("Open connection to qsync host '{}:{}'", host, TCP_PORT);

        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, TCP_PORT), CONNECTION_TIMEOUT);
        } catch (Exception e) {
            throw new RemoteControllerException("Connection failed", e);
        }

        try {
            os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        } catch (IOException e) {
            throw new RemoteControllerException(e);
        }

        logger.debug("Connection to qsync successfully opened...");
    }

    /**
     * Close connection to QsyncController.
     *
     * @throws RemoteControllerException
     */
    public void closeConnection() throws RemoteControllerException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RemoteControllerException(e);
        }
    }

    public void sendCommand(ShadeCommand command) throws RemoteControllerException {
        sendCommandBytes(command.getCommandBytes());
    }

    /**
     * Send byte command to Qsync Controller.
     *
     * @param command to send
     * @throws RemoteControllerException
     */
    private void sendCommandBytes(byte[] command) throws RemoteControllerException {
        logger.debug("Try to send command: {}", command);

        if (!isConnected()) {
            openConnection();
        }

        try {
            os.write(command);
            os.flush();
        } catch (IOException e) {
            logger.debug("Couldn't send command", e);
            closeConnection();
            throw new RemoteControllerException("Exception in writing to socket", e);
        }

        logger.debug("Command successfully sent");
    }

    /**
     * Send sequence of commands to Qsync Controller
     *
     * @param keys List of commands to send.
     * @throws RemoteControllerException
     */
    private void sendCommandBytes(List<byte[]> commands) throws RemoteControllerException {
        sendCommandBytes(commands, 300);
    }

    /**
     * Send sequence of commands to Qsync Controller
     *
     * @param keys      List of commands to send.
     * @param sleepInMs Sleep between key code sending in milliseconds.
     * @throws RemoteControllerException
     */
    private void sendCommandBytes(List<byte[]> commands, int sleepInMs) throws RemoteControllerException {
        // The qsync controller cannot handle multiple threads, all commands should
        // complete independently
        synchronized (lock) {
            Iterator<byte[]> iterator = commands.iterator();
            while (iterator.hasNext()) {
                sendCommandBytes(iterator.next());

                // Only sleep if there's another command to send
                if (iterator.hasNext()) {
                    try {
                        Thread.sleep(sleepInMs);
                    } catch (InterruptedException e) {
                        // We're not doing anything mission critical, allow thread shutdown
                        return;
                    }
                }
            }

            closeConnection();
        }

        logger.debug("Command(s) successfully sent");
    }

    private boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

}
