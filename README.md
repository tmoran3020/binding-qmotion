# Qmotion Binding

This openHAB binding controls Qmotion shades via a Qmotion [qsync](http://www.qmotionshades.com/products/25-controls/218-qsync) controller. Different shade groups defined within the qsync controller can be individually controlled for up/down and partial positions in increments of 12.5%.

Note: This binding relies upon shade groups already defined in the qsync application. You cannot create new shade groups with this binding.


## Supported Things

This binding supports the following Things:
* **qsync** - The Qmotion qsync controller
* **shade-group** - The shade group to control

## Discovery

Discovery is not yet implemented, although it is possible to achieve in the protocol.

## Thing Configuration

### Qsync

Qsync controllers require the IP address or hostname to the controller. This can be easily seen on the first page in the qsync application. It is recommended to utilize a static IP address for the qsync controller, if possible, as discovery is not yet implemented.

Example:

```
Thing qmotion:qsync:livingroom [ ipAddress="192.168.1.2" ]
```

### Shade Group

Shade groups require the channel id of the shade group as shown in the qsync application. Push the information (circled i) button next to the shade group and the channel id will be listed as "Channel:9", for example.

Please note you must define the bridge (the qsync controller) that this shade group is associated with.

Example:

```
Thing qmotion:shade-group:topwindows (qmotion:qsync:livingroom) [ id="9" ]
```

## Channels

The only defined channel for the binding is the position of the shade-group, defined as an item type "Rollershutter".

Example:

```
Rollershutter LivingRoomTopWindows { channel="motion:shade-group:topwindows:position" }
```

## Full Example

qmotion.things:

```
Thing qmotion:qsync:livingroom [ ipAddress="192.168.1.2" ]
Thing qmotion:shade-group:topwindows (qmotion:qsync:livingroom) [ id="9" ]
Thing qmotion:shade-group:bottomwindows (qmotion:qsync:livingroom) [ id="10" ]
```

qmotion:items:

```
Rollershutter LivingRoomTopWindows { channel="motion:shade-group:topwindows:position" }
Rollershutter LivingRoomBottomWindows { channel="motion:shade-group:bottomwindows:position" }
```

qmotion.sitemap:

```
Default item= LivingRoomTopWindows label="Shades"
Slider item= LivingRoomTopWindows label="Shades"
```

## Known Limitations

Please note that this is a very limited protocol. There is no status update capability, it is purely fire and forget. Therefore, the position of the shade group could get out of sync with reality if you use a local remote control or directly control the shade group using the qsync application.

Also, please note that the STOP functionality is not implemented. It is theoretically possible in the protocol - the shades stop if you send a command in the opposite direction during movement, so for example if you're going from all the way UP to all the way DOWN and you issue an UP command, they will stop. However, it is difficult to implement reliably as there is no way to know if the shades are still moving so you'll likely move them somewhere unexpected rather than stopping them.

At the current time, only control of shade groups is utilized, but the finding of qsync devices and shade groups could be implemented as a discovery service based on that protocol.

## Credits

This binding is based on the work of [David Parry](https://github.com/devbobo) who reverse engineered the [protocol](https://github.com/devbobo/qmotion/blob/master/Protocol.md). 

Also, much of the controller code is based on the work of Pauli Anttila in the Samsung binding.