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
    // Snapprefs supports v9.15.1.0 and up
    public static final int SUPPORTED_VERSION_CODE = 736;
    public static final String SUPPORTED_VERSION_CODENAME = "9.17.1.0";

    /**
     * Check if Snapprefs is compatible with this Snapchat version.
     *
     * @param versionCode The version code of the current Snapchat version
     * @return Whether it's supported
     */
    public static boolean isSupported(int versionCode) {
        return versionCode >= SUPPORTED_VERSION_CODE;
    }

    //SAVING
    public class save {

        //User class
        public static final String USER_CLASS = "Ap";
        //ReceivedSnap class
        public static final String RECEIVEDSNAP_CLASS = "zV";
        // ReceivedSnap.getCanonicalDisplayTime()
        public static final String RECEIVEDSNAP_DISPLAYTIME = "G";
        //StorySnap class
        public static final String STORYSNAP_CLASS = "Ak";
        //SnapView class
        public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.snapview.SnapView";
        //SnapView.show(ReceivedSnap, ChronologicalSnapProvider, Booleans(?))
        public static final String SNAPVIEW_SHOW = "b"; // prev. a
        //First param of SnapView.show -> bdl, avf
        public static final String SNAPVIEW_SHOW_FIRST = "FI";
        //Second param of SnapView.show -> asz, alr
        public static final String SNAPVIEW_SHOW_SECOND = "zQ";
        //Third param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_THIRD = "sk";
        //Fourth param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_FOURTH = "sm";
        //SnapView.hide(SnapViewEventAnalytics.EndReason)
        public static final String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        //ImageResourceView class
        public static final String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public static final String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "c"; // DX
        //LandingPageActivity class
        public static final String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public static final String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public static final String SNAP_ISSCREENSHOTTED = "ar";
        //Snap.getTimestamp()
        public static final String SNAP_GETTIMESTAMP = "W";
        //EndReason class
        public static final String ENDREASON_CLASS = "com.snapchat.android.analytics.SnapViewEventAnalytics.EndReason";
        //ImageResource -> Aw prev. avg, apz --  mIsSavedByRecipient:Z
        //ChatMedia instance variable name in ImageResource
        public static final String IMAGERESOURCE_VAR_CHATMEDIA = "a";
        //.model.chat.Chat -> zV.2
        //Chat.getTimeStamp()
        public static final String CHAT_GETTIMESTAMP = "W";
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public static final String STATEFULCHATFEEDITEM_GETSENDER = "j";
        //ScreenshotDetector class -> onLoaderReset
        public static final String SCREENSHOTDETECTOR_CLASS = "CP";
        //ScreenshotDetector.run(List)
        public static final String SCREENSHOTDETECTOR_RUN = "a";
        //SnapStateMessage class
        public static final String SNAPSTATEMESSAGE_CLASS = "Rb";
        //SnapStateMessage.setScreenshotCount(Long)
        public static final String SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT = "b";
        //SentSnap Bitmap class
        public static final String SENT_CLASS = "zD";
        //SentSnap Bitmap method
        public static final String SENT_METHOD = "a";
        //ImagesnapRenderer
        public static final String IMAGESNAPRENDERER_CLASS = "Cw";
        //ImagesnapRenderer.start()
        public static final String IMAGESNAPRENDERER_START = "d";
        //ImageView instance in ImageSnapRenderer
        public static final String IMAGESNAPRENDERER_VAR_IMAGEVIEW = "a";
        //SnapImageBryo - JPEG_ENCODING_QUALITY
        public static final String SNAPIMAGEBRYO_CLASS = "zB";
        //VideoSnapRenderer.start()
        public static final String VIDEOSNAPRENDERER_CLASS = "Cz";
        //VideoSnapRenderer.show()
        public static final String VIDEOSNAPRENDERER_SHOW = "d";
        //View Instance in VideoSnapRenderer
        public static final String VIDEOSNAPRENDERER_VAR_VIEW = "c";

    }
    //Data-saving
    public class datasaving {
        public static final String DSNAPDOWNLOADER_CLASS = "ut";
        public static final String DSNAPDOWNLOADER_DOWNLOADSNAP = "a";
        public static final String DOWNLOADREQUEST_CLASS = "Bh";
        public static final String DYNAMICBYTEBUFFER_CLASS = "NH";
        public static final String NETWORKRESULT_CLASS = "oD";
        public static final String CHANNELDOWNLOADER_CLASS = "uW";
        public static final String CHANNELDOWNLOADER_START = "b";
    }
    //Spoofing
    public class spoofing {
        //SpeedometerView class
        public static final String SPEEDOMETERVIEW_CLASS = "Fu";
        //SpeedometerView.setSpeed(Float)
        public static final String SPEEDOMETERVIEW_SETSPEED = "a";
        public static final String LOCATION_CLASS = "ze";
        public static final String LOCATION_GETLOCATION = "d";
        public static final String WEATHER_CLASS = "Av";
        public static final String WEATHER_FIRST = "RQ";
        public static final String BATTERY_FILTER = "zy";
    }
    //Select-All
    public class select {
        //SendToFragment class
        public static final String SENDTOFRAGMENT_CLASS = "com.snapchat.android.fragments.sendto.SendToFragment";
        //SendToFragment.AddToList()
        public static final String SENDTOFRAGMENT_ADDTOLIST = "h";
        //TopView instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_TOPVIEW = "d";
        //FriendHashSet instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_SET = "k";
        //ArrayList instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_ARRAYLIST = "l";
        //SendToAdapter class
        public static final String SENDTOADAPTER_CLASS = "xq"; //prev. wT
        //List instance variable in SendToAdapter
        public static final String SENDTOADAPTER_VAR_LIST = "e";
        //Friend class
        public static final String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public static final String POSTTOSTORY_CLASS = "zO"; //prev. zn
        //PostToVenue class
        public static final String POSTTOVENUE_CLASS = "zS"; //prev. zr
    }

    public class sharing {
        //cameraStateEvent class
        public static final String CAMERASTATEEVENT_CLASS = "KQ";
        //snapCapturedEvent class
        public static final String SNAPCAPTUREDEVENT_CLASS = "bhv"; //prev. bhv, bfy ->from LandingPageActivity$8
        //snapCaptureContext class
        public static final String SNAPCAPTURECONTEXT_CLASS = "com.snapchat.android.util.eventbus.SnapCaptureContext";
        //BusProvider class
        public static final String BUSPROVIDER_CLASS = "bey";
        //BussProvider.returnBus()
        public static final String BUSPROVIDER_RETURNBUS = "a";
    }
    public class lens {
        //ScheduledLensesProvider class
        public static final String LENSESPROVIDER_CLASS = "qy";
        //getLenses()
        public static final String LENSESPROVIDER_GETLENSES = "e";
    }
    public class stickers {
        //FastZippedAssetReader class
        public static final String ASSETREADER_CLASS = "GV";
        //FastZippedAssetReader.a
        public static final String ASSETREADER_A_CLASS = "GV$a";
        //read()
        public static final String ASSETREADER_READ = "a";
    }
}