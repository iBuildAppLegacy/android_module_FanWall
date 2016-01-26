Use our code to save yourself time on cross-platform, cross-device and cross OS version development and testing
-# android module FanWall
FanWall widget is intended for communication of users inside the app.

General features:

-- making posts on the wall;
-- making comments to existing messages;
-- viewing of the last messages and comments;
-- viewing of photo gallery;
-- access to a map with user location.
-
-**XML Structure declaration**

Tags:

-- title - widget name. Title is being displayed on navigation panel when widget is launched.
-- colorskin - this is root tag to set up color scheme. Contains 5 elements (color[1-5]). Each widget may set colors for elements of the interface using the color scheme in any order, however generally color1 - background color, color3 - titles color, color4 - font color, color5 - date or price color.
-- canedit - defines access rights of the user of a mobile application.
-Values:
-all - users may add posts and comment them on the wall;
-read - users may only view posts and comments to posts on the wall.
-- near - distance to the nearest geolocations (in miles) where posts were made on the wall. The posts, which places within the specified distance from the user's current location are displayed when you click on "near me" button in a mobile application.
-- module_id - widget identifier in iBuildApp system.
-- app_id - app identifier in iBuildApp system.
Example:

-
-    <data>
-    <canedit>all</canedit>
-    <near>1</near>
-    <module_id>1</module_id>
-    <app_id>99999999</app_id>
-    <colorskin>
-        <color1><![CDATA[#23660f]]></color1>
-        <color2><![CDATA[#fbff94]]></color2>
-        <color3><![CDATA[#b7ffa2]]></color3>
-        <color4><![CDATA[#ffffff]]></color4>
-        <color5><![CDATA[#fbff94]]></color5>
-    </colorskin>
-    </data>