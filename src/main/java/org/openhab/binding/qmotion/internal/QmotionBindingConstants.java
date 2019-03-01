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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link QmotionBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Moran - Initial contribution
 */
@NonNullByDefault
public class QmotionBindingConstants {

    private static final String BINDING_ID = "qmotion";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_QSYNC = new ThingTypeUID(BINDING_ID, "qsync");
    public static final ThingTypeUID THING_TYPE_SHADE_GROUP = new ThingTypeUID(BINDING_ID, "shade-group");

    // List of all Channel ids
    public static final String CHANNEL_SHADE_POSITION = "position";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_QSYNC);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SHADE_GROUP);
    }
}
