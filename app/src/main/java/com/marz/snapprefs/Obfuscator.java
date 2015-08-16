/*
 * Copyright (C) 2014  Sturmen, stammler, Ramis and P1nGu1n
 *
 * This file is part of Keepchat.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.marz.snapprefs;

public class Obfuscator {
    // Snapprefs supports v9.14.1.0 and up
    private static final int SUPPORTED_VERSION_CODE = 711;

    /**
     * Check if Snapprefs is compatible with this Snapchat version.
     *
     * @param versionCode The version code of the current Snapchat version
     * @return Whether it's supported
     */
    public static boolean isSupported(int versionCode) {
        return versionCode == SUPPORTED_VERSION_CODE;
    }

    //SAVING
    public class save {

        //ReceivedSnap class
        public static final String RECEIVEDSNAP_CLASS = "amj"; //prev. akv, akb
        // ReceivedSnap.getCanonicalDisplayTime()
        public static final String RECEIVEDSNAP_DISPLAYTIME = "D"; //prev. G
        //StorySnap class
        public static final String STORYSNAP_CLASS = "amw"; // prev. ali, akm
        //SnapView class
        public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.SnapView";
        //SnapView.show(ReceivedSnap, ChronologicalSnapProvider, Booleans(?))
        public static final String SNAPVIEW_SHOW = "a"; // prev. c
        //First param of SnapView.show -> avf
        public static final String SNAPVIEW_SHOW_FIRST = "avf";
        //Second param of SnapView.show -> alr
        public static final String SNAPVIEW_SHOW_SECOND = "alr";
        //Third param of SnapView.show -> abp
        public static final String SNAPVIEW_SHOW_THIRD = "abp";
        //SnapView.hide(SnapViewEventAnalytics.EndReason)
        public static final String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        //ImageResourceView class
        public static final String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public static final String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "a"; // asj -> prev. aqz, apz
        //LandingPageActivity class
        public static final String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public static final String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public static final String SNAP_ISSCREENSHOTTED = "ap"; //prev. ao
        //Snap.getTimestamp()
        public static final String SNAP_GETTIMESTAMP = "T"; //prev R, U
        //EndReason class
        public static final String ENDREASON_CLASS = "com.snapchat.android.analytics.SnapViewEventAnalytics.EndReason";
        //ImageResource -> aqz prev. apz
        //ChatMedia instance variable name in ImageResource
        public static final String IMAGERESOURCE_VAR_CHATMEDIA = "f";
        //.model.chat.Chat -> anh, prev. alt, akx
        //Chat.getTimeStamp()
        public static final String CHAT_GETTIMESTAMP = "T"; //prev. U
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public static final String STATEFULCHATFEEDITEM_GETSENDER = "j";
    }
    //Sharing

    //Data-saving
    public class datasaving {
        public static final String DSNAPDOWNLOADER_CLASS = "afq"; //prev. aekparamaoh, bjz parambjz, vy paramvy
        public static final String DSNAPDOWNLOADER_DOWNLOADSNAP = "a";
        public static final String DOWNLOADREQUEST_CLASS = "aoh"; //prev. amv
        public static final String DYNAMICBYTEBUFFER_CLASS = "bjz"; //prev. bic
        public static final String NETWORKRESULT_CLASS = "vy"; //prev. vb
    }
    //Spoofing
    public class spoofing {
        //SpeedometerView class
        public static final String SPEEDOMETERVIEW_CLASS = "aus"; //prev. asz
        //SpeedometerView.setSpeed
        public static final String SPEEDOMETERVIEW_SETSPEED = "a";
    }
    //Select-All
    public class select {
        //SendToFragment class
        public static final String SENDTOFRAGMENT_CLASS = "com.snapchat.android.fragments.sendto.SendToFragment";
        //SendToFragment.AddToList()
        public static final String SENDTOFRAGMENT_ADDTOLIST = "i";
        //TopView instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_TOPVIEW = "d";
        //FriendHashSet instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_SET = "l";
        //ArrayList instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_ARRAYLIST = "m";
        //SendToAdapter class
        public static final String SENDTOADAPTER_CLASS = "ajj"; //prev. ahz, ahe
        //List instance variable in SendToAdapter
        public static final String SENDTOADAPTER_VAR_LIST = "e";
        //Friend class
        public static final String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public static final String POSTTOSTORY_CLASS = "ame"; //prev. aku
    }

    public class sharing {
        //snapCapturedEvent class
        public static final String SNAPCAPTUREDEVENT_CLASS = "bhv"; //prev. bfy ->from LandingPageActivity$8
        //snapCaptureContext class
        public static final String SNAPCAPTURECONTEXT_CLASS = "com.snapchat.android.util.eventbus.SnapCaptureContext";
        //BusProvider class
        public static final String BUSPROVIDER_CLASS = "bey";
        //BussProvider.returnBus()
        public static final String BUSPROVIDER_RETURNBUS = "a";
    }
}