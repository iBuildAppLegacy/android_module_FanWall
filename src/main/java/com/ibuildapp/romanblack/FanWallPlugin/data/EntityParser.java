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

import android.graphics.Color;
import android.util.Log;
import android.util.Xml;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class using for module xml data parsing.
 */
public class EntityParser {

    private float near = 180;
    private String moduleId = "0";
    private String appId = "0";
    private String canEdit = "all";
    private int color1 = Color.parseColor("#4d4948");// background
    private int color2 = Color.parseColor("#fff58d");// category header
    private int color3 = Color.parseColor("#fff7a2");// text header
    private int color4 = Color.parseColor("#ffffff");// text
    private int color5 = Color.parseColor("#bbbbbb");// date

    /**
     * Psrses module XML data.
     *
     * @param xml - module xml data to parse
     */
    public void parse(String xml) {
        try {
            Xml.parse(xml, new SaxHandler());
        } catch (Exception ex) {
            Log.d("", "");
        }
    }

    /**
     * @return parsed color 1 of color scheme
     */
    public int getColor1() {
        return color1;
    }

    /**
     * @return parsed color 2 of color scheme
     */
    public int getColor2() {
        return color2;
    }

    /**
     * @return parsed color 3 of color scheme
     */
    public int getColor3() {
        return color3;
    }

    /**
     * @return parsed color 4 of color scheme
     */
    public int getColor4() {
        return color4;
    }

    /**
     * @return parsed color 5 of color scheme
     */
    public int getColor5() {
        return color5;
    }

    /**
     * Sax handler that handle XML configuration tags and prepare module data structure.
     */
    private class SaxHandler extends DefaultHandler {

        private StringBuilder sb = new StringBuilder();

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            sb.append(new String(ch, start, length));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (localName.equalsIgnoreCase("canedit")) {
                canEdit = sb.toString().trim();
            } else if (localName.equalsIgnoreCase("near")) {
                near = new Float(sb.toString().trim()).floatValue();
            } else if (localName.equalsIgnoreCase("module_id")) {
                moduleId = sb.toString().trim();
            } else if (localName.equalsIgnoreCase("app_id")) {
                appId = sb.toString().trim();
            } else if (localName.equalsIgnoreCase("color1")) {
                color1 = Color.parseColor(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("color2")) {
                color2 = Color.parseColor(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("color3")) {
                color3 = Color.parseColor(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("color4")) {
                color4 = Color.parseColor(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("color5")) {
                color5 = Color.parseColor(sb.toString().trim());
            }

            sb.setLength(0);
        }
    }

    ;


    public float getNear() {
        return near;
    }

    /**
     * Returns the module ID.
     *
     * @return the module ID
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Returns if user can post new posts to the wall.
     *
     * @return "yes" or "no"
     */
    public String getCanEdit() {
        return canEdit;
    }

    /**
     * Returns the application ID.
     *
     * @return the application ID
     */
    public String getAppId() {
        return appId;
    }
}
