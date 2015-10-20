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


public class DetailActivityAdapterData {
    public FanWallMessage message;
    public FanWallMessage header;
    public boolean noComments = false;

    public DetailActivityAdapterData(FanWallMessage message, FanWallMessage header, boolean noComments) {
        this.message = message;
        this.header = header;
        this.noComments = noComments;
    }
}
