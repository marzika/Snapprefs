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
    // Snapprefs supports v9.12.3.0 and up
    private static final int SUPPORTED_VERSION_CODE = 697;

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
        public static final String RECEIVEDSNAP_CLASS = "akb";
        // ReceivedSnap.getCanonicalDisplayTime()
        public static final String RECEIVEDSNAP_DISPLAYTIME = "G"; //prev. F, M, K
        //StorySnap class
        public static final String STORYSNAP_CLASS = "akm";
        //SnapView class
        public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.SnapView";
        //SnapView.show()
        public static final String SNAPVIEW_SHOW = "c";
        //SnapView.hide()
        public static final String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        //ImageResourceView class
        public static final String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public static final String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "a"; //apz
        //LandingPageActivity class
        public static final String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public static final String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public static final String SNAP_ISSCREENSHOTTED = "ao";
        //Snap.getTimestamp()
        public static final String SNAP_GETTIMESTAMP = "U"; //prev W, ae, ac
        //EndReason class
        public static final String ENDREASON_CLASS = "com.snapchat.android.analytics.SnapViewEventAnalytics.EndReason";
        //ChronologicalSnapProvider class
        public static final String CHRONOLOGICALSNAPPROVIDER_CLASS = "ajl";
        //ImageResource -> apz
        //ChatMedia instance variable name in ImageResource
        public static final String IMAGERESOURCE_VAR_CHATMEDIA = "f";
        //.model.chat.Chat -> akx
        //Chat.getTimeStamp()
        public static final String CHAT_GETTIMESTAMP = "U";
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public static final String STATEFULCHATFEEDITEM_GETSENDER = "j";
    }
    //Sharing

    //Data-saving

    //Spoofing

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
        public static final String SENDTOADAPTER_CLASS = "ahe";
        //List instance variable in SendToAdapter
        public static final String SENDTOADAPTER_VAR_LIST = "e";
        //Friend class
        public static final String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public static final String POSTTOSTORY_CLASS = "ajz";
    }
}