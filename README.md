[DataDecoy](http://bjr24.github.com/DataDecoy/)
=========

DataDecoy-GPS edition
---------

We were annoyed because all these Android apps require your GPS location just for location-based advertisements.
In response we modified the Android platform to allow a user to choose which applications get real location information and which get false information.

The CSE622 project pitch that started it all on [YouTube](https://www.youtube.com/watch?v=Ox1YeVRTVaQ)

Team Members
---------
* Guru Prasad Srinivasa
* Denise Blady
* Eric Lehner
* Brian Rosenberg

Installation
---------
This image is Android JellyBean 4.1.1. It is specific to the Samsung Nexus S 3G edition

1. Download [system.img](https://github.com/downloads/bjr24/DataDecoy/system.img)
2. Connect the phone
3. run: `adb reboot bootloader`
4. If the bootloader is locked, run: `fastboot oem unlocked`
5. `fastboot flash system /path/to/system.img`
6. Download and install the [UI app](https://github.com/downloads/bjr24/DataDecoy/Data%20Decoy.apk)

