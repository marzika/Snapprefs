Snapprefs - (Current Supported Version: 9.39.5)
=========
[![Build Status](http://www.snapprefs-builds.com/job/SNAPPREFS%20-%20Official%20-%20stable/badge/icon)](http://www.snapprefs-builds.com/job/SNAPPREFS%20-%20Official%20-%20stable/) [![Join the chat on Slack](https://snapprefs-dev2.herokuapp.com/badge.svg)](https://snapprefs-dev.herokuapp.com)

Overview
-------
Snapprefs is a project which offers enhanced features for Snapchat through the [Xposed framework](http://forum.xda-developers.com/showthread.php?t=1574401).

[Want to translate Snapprefs into your native language?](http://osl2hw5.oneskyapp.com/collaboration/project?id=154335)

[Snapprefs on XDA-Developers](http://forum.xda-developers.com/xposed/modules/app-snapprefs-ultimate-snapchat-utility-t2947254)

Want to be up to date with our Projects? [Visit our website!](http://snapprefs.com)

Current features are:

- Save images/stories/chat images
  - Sweep to save
  - Save button
  - Autosave
- Save profile images
- Share from Gallery
- Groups
- Select All
- Better quality images
- Custom
  - PNG filters
  - Visual Filters (9, like Instagram's)
  - Stickers
- Screenshot detection bypass for chat messages, images and videos
- Unlimited caption text
- Hiding best friends
- Text
  - Font (OTF and TTF)
  - Color(HSV)
  - Alignment(left, center, right)
  - Size(1-250)
  - Style(bold, italic, bold and italic, normal)
  - Gradient
- Background
  - Color(HSV)
  - Opacity(0%-100%)
  - Gradient
- Caption opacity customization
- Paint Tools
  - Color(HSV)
  - Thickness
  - Opacity (0%-100%)
  - Gradient
  - Color History
  - Shape
  - Eraser
  - Blur
- Spoofing
  - Location for Geofilters
  - Speed
  - Temperature
- Hide Discover
- Hide Live Stories
- Block people from Stories
- Show recording time
- 10+ sec. recording
- Force to show Lenses on older devices
- Hide icon from every launcher
- Now Playing filter

Credits
-------

[Keepchat for their amazing work](https://github.com/P1nGu1n/Keepchat)

[Snapshare for their amazing work](https://github.com/P1nGu1n/Snapshare)

andrerm124 for working countless hours on the project

m1kep for providing the build server and additional features

manvir for his inspirational [SnapColors app](http://repo.xposed.info/module/com.manvir.snapcolors)

elesbb for his inspirational [SnapChat Full Caption app](http://repo.xposed.info/module/com.elesbb.snapchatfullcaption)

chiralcode for his [ColorPicker](https://github.com/chiralcode/Android-Color-Picker)

[Yoganandh for his color collection](https://gist.github.com/VenomVendor/6857539)

cketti for his [ckChangelog library](https://github.com/cketti/ckChangeLog) (Apache License 2.0)

ddmanfire for implementing array saving [#1](https://github.com/ddmanfire/Snapprefs/commit/dc2e199c74a3729f0c50365597577a37fb312b2e) [#2](https://github.com/ddmanfire/Snapprefs/commit/03fa8783cd3d21dabdfebbd572eb7481fccbe48b)

XPrivacy for the [installation instructions](https://github.com/M66B/XPrivacy/blob/master/README.md#installation)

Installation
------------------
*Note: Obviously, you need __root access__ on your phone.*

1. Requirements:
	* Android version 4.1 - 6.0.1 (Jelly Bean, KitKat, Lollipop, Marshmallow); verify via *System Settings* > *About phone* > *Android version*
1. **Make a backup**
1. If you haven't already, root your device; the rooting procedure depends on your device's brand and model.
	* You can find a guide [here](http://www.androidcentral.com/root) for most devices
1. Enable *System settings* > *Security* > *Unknown sources*
1. Install the [Xposed framework](http://forum.xda-developers.com/xposed)
	* For Android 4.1 through 4.4.4 see [this XDA thread](http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053). If this does not work for you (red error text in Xposed Installer -> Framework), see [this XDA thread](http://forum.xda-developers.com/xposed/xposed-android-4-4-4-t3249895)
	* For Android 5.x see [this XDA thread](http://forum.xda-developers.com/showthread.php?t=3034811)
	* For Android 5.0.x Touchwiz ROMs see [this XDA thread](http://forum.xda-developers.com/xposed/unofficial-xposed-samsung-lollipop-t3113463)
	* For Android 5.1 Touchwiz ROMs see [this XDA thread](http://forum.xda-developers.com/xposed/unofficial-xposed-samsung-lollipop-t3180960)
	* For Android 6.0.x see [this XDA thread](http://forum.xda-developers.com/showthread.php?t=3034811)
1. Download and install Snapprefs from [here](http://repo.xposed.info/module/com.marz.snapprefs)
1. Enable Snapprefs in the Xposed installer
1. Start Snapprefs one time
1. Reboot
1. Customize the options
1. Kill Snapchat after enabling/disabling options 

Latest automatic builds (provided by m1kep):
-------
Available as Jenkins artifacts:

[Official Stable Branch](http://bit.ly/SPOfficialStable)

[Official Development Branch](http://bit.ly/SPOfficialDevelopment)

License
-------
The whole project is licensed under the GNU GPLv3, which can be found in the LICENSE file.
The license is also included with the app in the /assets/ folder.

*Copyright (C) 2014-2016 Marcell Mészáros*

Snapprefs is in no way affiliated with, authorized, maintained, sponsored or endorsed by the Snapchat Inc. or any of its affiliates or subsidiaries. This is an independent application.
