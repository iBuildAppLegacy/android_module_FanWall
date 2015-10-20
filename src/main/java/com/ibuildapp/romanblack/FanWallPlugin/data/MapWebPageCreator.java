/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.FanWallPlugin.data;

import android.util.Log;
import com.ibuildapp.romanblack.FanWallPlugin.data.FanWallMessage;
import com.ibuildapp.romanblack.FanWallPlugin.data.Statics;

import java.util.ArrayList;

/**
 * This class using to prepare Google Map HTML to load in WebView.
 */
public class MapWebPageCreator {

    /**
     * Creates map HTML page with given parameters.
     *
     * @param sourcePage map HTML page template
     * @param items      map location items
     * @return prepared map HTML page
     */
    public static String createMapPage(String sourcePage, ArrayList<FanWallMessage> items) {
        String mapPage = "";

        mapPage = sourcePage.replace("__RePlAcE-Points__", createPoints(items));
        mapPage = replaceCenterCoordinates(mapPage, items);

        return mapPage;
    }

    /**
     * Creates map points HTML fragment
     *
     * @param items map location items
     * @return map points HTML fragment
     */
    private static String createPoints(ArrayList<FanWallMessage> items) {
        String res = "";
        if (items != null) {
            for (int i = 0; i < (items.size()); i++) {
                try {
                    if ((items.get(i).getLatitude() != 1000)
                            && (items.get(i).getLongitude() != 1000)) {
                        String t = "\'";
                        String tr = "\\\'";

                        String temp = "myMap.points.push({\n"
                                + "point: \'" + items.get(i).getAuthor().
                                replaceAll("\n", " ").replaceAll("\'", "\\\\'") + "\',\n"
                                + "latitude:" + items.get(i).getLatitude() + ",\n"
                                + "longitude:" + items.get(i).getLongitude() + ",\n"
                                + "details: \'" + items.get(i).getText().replaceAll("\n", " ").
                                replaceAll("\'", "\\\\'") + "\',\n"
                                + "url: \'" + "\'\n"
                                + "})\n\n";

                        res = res + temp;
                    }
                } catch (Exception e) {
                    Log.e("", "");
                }
            }
        }

        return res;
    }

    /**
     * Replaces map center coordinates.
     *
     * @param sourcePage map HTML page template
     * @param items      map location items
     * @return
     */
    private static String replaceCenterCoordinates(String sourcePage, ArrayList<FanWallMessage> items) {
        String res = "";

        float maxLat = -90;
        float maxLng = -180;
        float minLat = 90;
        float minLng = 180;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if ((items.get(i).getLatitude() != 1000)
                        && (items.get(i).getLongitude() != 1000)) {
                    if (items.get(i).getLatitude() > maxLat) {
                        maxLat = (float) items.get(i).getLatitude();
                    }

                    if (items.get(i).getLongitude() > maxLng) {
                        maxLng = (float) items.get(i).getLongitude();
                    }

                    if (items.get(i).getLatitude() < minLat) {
                        minLat = (float) items.get(i).getLatitude();
                    }

                    if (items.get(i).getLongitude() < minLng) {
                        minLng = (float) items.get(i).getLongitude();
                    }
                }
            }

            float cenLat = (maxLat + minLat) / 2;
            float cenLng = (maxLng + minLng) / 2;

            if (Statics.currentLocation == null) {
                res = sourcePage.replace("__RePlAcE-Lat__", new Float(cenLat).toString());
                res = res.replace("__RePlAcE-Lng__", new Float(cenLng).toString());
            } else {
                res = sourcePage.replace("__RePlAcE-Lat__", Statics.currentLocation.getLatitude() + "");
                res = res.replace("__RePlAcE-Lng__", Statics.currentLocation.getLongitude() + "");
            }
            res = res.replace("__RePlAcE-Zoom__",
                    new Integer(processZoom(minLng, maxLng)).toString());
        }

        return res;
    }

    /**
     * Processes map zoom level depending on map locations coordinates.
     *
     * @param minLongitude minimal map location longitude
     * @param maxLongitude maximal map location longitude
     * @return zoom
     */
    private static int processZoom(float minLongitude, float maxLongitude) {
        int z = 1;
        float gr = Math.abs(maxLongitude - minLongitude);

        if (gr > (120)) {
            z = 1;
        } else if (gr > (60)) {
            z = 2;
        } else if (gr > (30)) {
            z = 3;
        } else if (gr > (15)) {
            z = 4;
        } else if (gr > (8)) {
            z = 5;
        } else if (gr > (4)) {
            z = 6;
        } else if (gr > (2)) {
            z = 7;
        } else if (gr > (1)) {
            z = 8;
        } else if (gr > (0.5)) {
            z = 9;
        } else {
            z = 10;
        }

        return z;
    }
}
