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


public class IncrementSharingStatus {
    public int status_code = 0;
    public String error = "";

    @Override
    public String toString() {
        return "IncrementSharingStatus{" +
                "status_code=" + status_code +
                ", error='" + error + '\'' +
                '}';
    }
}
