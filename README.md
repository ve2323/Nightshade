# Nightshade #

Current: Alpha

Nightshade is a browser created using javaFX that is meant to make life a little easier for developers.
This project is built to allow additional functionality to be implemented with ease should the need arise.

### Features ###

* Basic browser functionality
* View website load in % within tabs
* In-view life
  * Only components in view will be active and once closed all related functionality will seize to exist
* Execute system commands
* Execute javascript
* View application memory usage
* View system information
* Lookup ip-address or domain:
  * Ping all found ip-addresses
  * Traceroute all found ip-addresses
* View current page ip-address
* Get system ip information
* Scrape websites(created using Jsoup functionality):
  * Get everything by specifying domain
  * Get select content by specifying tag
* Firebug toolset


### Additional information ###

* Enable Firebug by using the keycombination ctrl+shift+F in browser view
* Enable extra console by using keycombination ctrl+D in browser view

* Currently window resizing is limited to dragging the window to the edge of screen(when not maximized) which will result in:
  * Right/Left Side: Application will use half of the current screen bounds
  * Top/Bottom: Application will be maximized
