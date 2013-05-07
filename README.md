assassins
=========

A social location-based mobile game

sinatra/ruby webservice requirements:
-ruby 1.9.2
-vmc
-bundler
-rvm (recommended)


android client requirements:
-android sdk version 17
-google maps v2 api key (v1 is deprecated)


obtaining a google maps v2 key:
https://developers.google.com/maps/documentation/android/start#the_google_maps_api_key

paste your google-provided key into the following file and save it.

PATH_TO_ECLIPSE_PROJECT/res/values/map_key.xml
<resources>
    <string name="map_key">YOUR_KEY_HERE</string>
</resources>


eclipse setup for using compiled annotations:
https://github.com/excilys/androidannotations/wiki/Eclipse-Project-Configuration





