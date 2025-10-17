
# The Holy Rail
## The Holy gRail of minecart mods!

The Holy Rail is a minecart improvement mod for Necesse. It adds the following items to the game:

- Powered Tracks - Gives minecarts a boost of speed when powered with wire.
- The Track Torpedo - A minecart with an overall higher top speed.
- Chest Minecarts - Store items inside and send them on their way.
- Station Tracks - Automate Chest Minecart movement all around your settlement and allows Chest Minecarts to automatically interact with nearby chests! This allows you to effectively automate your item movement around your settlement.


## Mod Notes
#### Server Notes
If you want this mod on your dedicated server, then you must download the jar file located [HERE](https://github.com) and place it in your server's Necesse mods folder. More information can be found [here](https://necessewiki.com).

#### Track Torpedo
- They are available at Demonic Workstations or higher. You'll need the following materials:
  - 5 Tungsten Bars
  - 3 Gold Bars
  - 1 Firework Dispenser
- They go roughly 1.7x faster than standard minecarts.

#### Powered Tracks
- They are available at Demonic Workstations or higher. You'll need the following materials:
  - 2 Gold Bars
  - 1 Iron Bar
  - 1 Wood Log
  - 5 Wire
- They require power. If you run over an unpowered Powered Track, you will slow down.
- Powered Tracks will stop empty minecarts and Track Torpedos, but will always send on Chest Minecarts (if the track is powered).
- They work with all current forms of power generation (sensors, levers, etc)
- AFK mob farms are possible! See the [mod's video](https://youtu.be/x6dosWzmnfY) for an example.

#### Chest Minecart
- They are available at any workstation. You'll need the following materials:
  - 1 Minecart
  - 1 Storagebox
- They are designed to work with both Powered Tracks and Station Tracks.
- They can hold any item and have 20 slots.

#### Station Tracks
- They are available at Demonic Workstations or higher. You'll need the following materials:
  - 1 Tungsten Bar
  - 1 Iron Bar
  - 1 Wood Log
  - 4 Wire
- They are designed to work only with Chest Minecarts. Any other minecart will treat the station track like a normal track.
- They are fully configurable. Right-click the Station Track after placing it down to configure it. Want to have Chest Minecarts automatically pull from one nearby chest? Or do you want Chest Minecarts to automatically dump their contents into nearby chests? This is where you configure that.
- Station Tracks have two modes: Middle Of The Line and End Of The Line
  - Middle Of The Line mode is set when the Station Track is **UNPOWERED**
    - This mode will stop a Chest Minecart as you have configured it, and then send it in the direction it was facing when it stopped.
  - End Of The Line mode is set when the Station Track is **POWERED**
    - This mode will stop a Chest Minecart as you have configured it, and then it will turn it around and send it back in the direction whence it came.
    - This mode will be indicated by small yellow stripes along the rail.

## Version Updates

### 1.0.1

- Added support for Necesse 1.0.1
  
### 0.8.2

- Added support for Necesse 0.33.0

### 0.8.1

- Added support for Necesse 0.32.0
- Adjusted deprecations
- Fixed various style warnings

### 0.8.0

- Added support for Necesse 0.29.0
- Updated the Italian translation

### 0.7.1

- Updated to support Necesse version 0.26.1
- Italian Language support added, thanks to chrisguglia!

### 0.7

- Updated to support Necesse version 0.26.0

### 0.6.8

- Updated to support Necesse version 0.25.0

### 0.6.713

- Updated to support Necesse version 0.24.0

### 0.6.701

- Fixed a bug that would lock a player in if they attempted to mount/dismount from the Track Torpedo when equipped as a mount and used the mount hotkey.

### 0.6.7

- Added compatibility for Necesse version 0.23.1

### 0.6.6

- Added compatibility for Necesse version 0.23

### 0.6.5

- Added compatibility for Necesse version 0.22.1

### 0.6.4

- Added compatibility for Necesse version 0.22

### 0.6.3

- **Bugfixes**
  - Fixed a bug that could cause a crash in 0.6.2.

### 0.6.2

- **Bugfixes**
  - Fixed a null entity error that could occur when powering station tracks.

### 0.6.1

- **Additions**
  - Added some text to the Station Track configuration form that gives user feedback on current Station Track mode.

### 0.6.0

- **Additions**
  - Added Chest Minecarts
  - Added Station Tracks
    - See FAQ for more info.

- **Bugfixes**
  - Fixed desync issues with minecart positions.

- **Other**
  - Completely retooled how powered rails add speed to minecarts.
  - Versioning schema for the mod changed to the following pattern:
    - *Necesse Major Version* . *The Holy Rail Major Version* . *The Holy Rail Minor Version*
      - Holy Rail major versions will be reserved for additions/overhauls.
      - Holy Rail minor versions will be for small bugfixes/hotfixes/patches.

## FAQ

### Can Station Tracks be powered?

Yes! In fact, they are designed to be used both powered and unpowered.  Powered Station Tracks are meant to be at the ends of your hauling lines because they automatically turn carts around after they've been stationed.  Unpowered Station Tracks send minecarts in the direction they were originally going after being stationed.

### Can I configure a Station Track to be ignored?

Yep. Set the Station Track to wait for 0 seconds. The Station Track will then act as a normal rail.

### Have any more questions?  Reach out to me on Steam @kn0wmad1c or on the Necesse discord
