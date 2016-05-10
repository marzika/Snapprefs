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
    public static final int SUPPORTED_VERSION_CODE = 767;
    public static final String SUPPORTED_VERSION_CODENAME = "9.21.1.0";
    public static final String ROOTDETECTOR_CLASS = "abc";
    public static final String[] ROOTDETECTOR_METHODS= {"b", "c", "d", "e"};
    public static final String FRIENDS_BF= "k";

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
        public static final String USER_CLASS = "LZ"; //+
        //ReceivedSnap class
        public static final String RECEIVEDSNAP_CLASS = "LB"; //+
        // ReceivedSnap.getCanonicalDisplayTime()
        public static final String RECEIVEDSNAP_DISPLAYTIME = "G";
        //StorySnap class
        public static final String STORYSNAP_CLASS = "LR"; //+
        //SnapView class
        public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.snapview.SnapView";
        //SnapView.show(ReceivedSnap, ChronologicalSnapProvider, Booleans(?))
        public static final String SNAPVIEW_SHOW = "b"; // prev. a
        //First param of SnapView.show -> bdl, avf
        public static final String SNAPVIEW_SHOW_FIRST = "Sp";
        public static final String STORYVIEW_SHOW_FIRST = "Sp";
        public static final String STORYVIEW_SHOW_SECOND = "Lw";
        //Second param of SnapView.show -> asz, alr
        public static final String SNAPVIEW_SHOW_SECOND = "Lw";
        //Third param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_THIRD = "CG";
        //Fourth param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_FOURTH = "CI";
        //SnapView.hide(SnapViewEventAnalytics.EndReason)
        public static final String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        //ImageResourceView class
        public static final String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public static final String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "c"; // KM
        //LandingPageActivity class
        public static final String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public static final String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public static final String SNAP_ISSCREENSHOTTED = "at";
        //public static final String SNAP_ISSCREENSHOTTED2 = "au";
        //Snap.getTimestamp()
        public static final String SNAP_GETTIMESTAMP = "W";
        //EndReason class
        public static final String ENDREASON_CLASS = "com.snapchat.android.analytics.SnapViewEventAnalytics.EndReason";
        //ImageResource -> Aw prev. avg, apz --  mIsSavedByRecipient:Z
        //ChatMedia instance variable name in ImageResource
        public static final String IMAGERESOURCE_VAR_CHATMEDIA = "c";
        //.model.chat.Chat -> zV.2
        //Chat.getTimeStamp()
        public static final String CHAT_GETTIMESTAMP = "W";
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public static final String STATEFULCHATFEEDITEM_GETSENDER = "j";
        //ScreenshotDetector class -> datetaken
        public static final String SCREENSHOTDETECTOR_CLASS = "OB"; //+
        //ScreenshotDetector.run(List)
        public static final String SCREENSHOTDETECTOR_RUN = "a";
        //SnapStateMessage class
        public static final String SNAPSTATEMESSAGE_CLASS = "aei"; //+
        //SnapStateMessage.setScreenshotCount(Long)
        public static final String SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT = "b";
        //SentSnap Bitmap class
        public static final String SENT_CLASS = "Li"; //+
        //SentSnap Bitmap method
        public static final String SENT_METHOD = "a";
        //ImagesnapRenderer
        public static final String IMAGESNAPRENDERER_CLASS = "Og"; //+
        //ImagesnapRenderer.start()
        public static final String IMAGESNAPRENDERER_START = "c";
        //ImageView instance in ImageSnapRenderer
        public static final String IMAGESNAPRENDERER_VAR_IMAGEVIEW = "h";
        //SnapImageBryo - JPEG_ENCODING_QUALITY
        public static final String SNAPIMAGEBRYO_CLASS = "LK";
        //VideoSnapRenderer.start()
        public static final String VIDEOSNAPRENDERER_CLASS = "Oj";
        //VideoSnapRenderer.show()
        public static final String VIDEOSNAPRENDERER_SHOW = "c";
        //View Instance in VideoSnapRenderer
        public static final String VIDEOSNAPRENDERER_VAR_VIEW = "c";
        public static final String IMAGESNAPRENDERER_SETVIEW = "a";
        public static final String VIDEOSNAPRENDERER_SETVIEW = "a";
        //SwipeUpArrowView is the View containing the Chat element of the Friend's story screen
        public static final String SWIPEUPARROWVIEW_CLASS = "com.snapchat.android.ui.SwipeUpArrowView";
        //SwipeUpArrowView.setLongFormAreaOnClickListener
        public static final String SWIPEUPARROWVIEW_SETONCLICK = "setLongFormAreaOnClickListener";

    }
    //Data-saving
    public class datasaving {
        public static final String DSNAPDOWNLOADER_CLASS = "EV";
        public static final String DSNAPDOWNLOADER_DOWNLOADSNAP = "a";
        public static final String DOWNLOADREQUEST_CLASS = "MS";
        public static final String DYNAMICBYTEBUFFER_CLASS = "aay";
        public static final String NETWORKRESULT_CLASS = "yM";
        public static final String CHANNELDOWNLOADER_CLASS = "FC";
        public static final String CHANNELDOWNLOADER_START = "b";
        public static final String LIVESTORYPRELOAD_CLASS = "OX";
        public static final String LIVESTORYPRELOAD_METHOD = "D_";
        public static final String STORYPRELOAD_CLASS = "Ps";
        public static final String STORYPRELOAD_METHOD = "D_";
    }
    //Spoofing
    public class spoofing {
        //SpeedometerView class
        public static final String SPEEDOMETERVIEW_CLASS = "Sb";
        //SpeedometerView.setSpeed(Float)
        public static final String SPEEDOMETERVIEW_SETSPEED = "a";
        public static final String LOCATION_CLASS = "KK";
        public static final String LOCATION_GETLOCATION = "d";
        public static final String WEATHER_CLASS = "Mg";
        public static final String WEATHER_FIRST = "aff";
        public static final String BATTERY_FILTER = "Ld";
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
        public static final String SENDTOADAPTER_CLASS = "IE";
        //List instance variable in SendToAdapter
        public static final String SENDTOADAPTER_VAR_LIST = "e";
        //Friend class
        public static final String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public static final String POSTTOSTORY_CLASS = "Lu";
        //PostToVenue class
        public static final String POSTTOVENUE_CLASS = "Ly";
    }

    public class sharing {
        //cameraStateEvent class
        public static final String CAMERASTATEEVENT_CLASS = "XA";
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
        public static final String LENSESPROVIDER_CLASS = "wr";
        //getLenses()
        public static final String LENSESPROVIDER_GETLENSES = "f";
    }
    public class stickers {
        //FastZippedAssetReader class
        public static final String ASSETREADER_CLASS = "TB";
        //FastZippedAssetReader.a
        public static final String ASSETREADER_A_CLASS = "TB$a";
        //read()
        public static final String ASSETREADER_READ = "a";
    }
    public class filters {
        //FilterLoader class
        public static final String LOADER_CLASS = "Te";
        //FilterLoader First Param
        public static final String LOADER_FIRST = "PX";
        //added instance
        public static final String FILTER_CLASS = "RY";
        //called Object
        public static final String OBJECT_CLASS = "SR";
        //onSnapCapturedEvent first param
        public static final String CAPTURED_FIRST = "Zs";
        //public.xml - battery_view
        public static final int BATTERY_VIEW = 2130968587;
        //public.xml - battery_icon
        public static final int BATTERY_ICON = 2131558548;
    }
    public class visualfilters {
        public static final String FILTERS_CLASS = "SC";
        public static final String FILTERSLOADER_CLASS = "SD";
        public static final String FILTERSLOADER_2_CLASS = "SG";
        public static final String SETFILTER_B_CLASS = "aiP$b";
        public static final String GREYSCALE_CLASS = "Tb";
        public static final String ADDFILTER_CLASS = "Tc";
        public static final String ADDFILTER_PARAM = "SZ";
        public static final String ADDER_3_PARAM = "SK$3";
        public static final String ADDER_PARAM = "SK";
        public static final String SNAPCHAPTUREDEVENT_CLASS = "Zs";
    }
    public class timer {
        public static final String TAKESNAPBUTTON_CLASS = "com.snapchat.android.ui.camera.TakeSnapButton";
        public static final String TAKESNAPBUTTON_ONDRAW = "onDraw";
        public static final String TAKESNAPBUTTON_BLEAN1 = "b";
        public static final String TAKESNAPBUTTON_BLEAN2 = "d";
        public static final String TAKESNAPBUTTON_TIME = "a";
        public static final String TAKESNAPBUTTON_X = "m";
        public static final String TAKESNAPBUTTON_Y = "n";
    }
    public class chat {
        public static final String CHAT_CLASS = "com.snapchat.android.model.chat.Chat";
        public static final String MESSAGEVIEWHOLDER_CLASS = "com.snapchat.android.fragments.chat.MessageViewHolder";
        public static final String MESSAGEVIEWHOLDER_METHOD = "b";
        public static final String MESSAGEVIEWHOLDER_VAR1 = "F";
        public static final String MESSAGEVIEWHOLDER_VAR2 = "d";
        public static final String MESSAGEVIEWHOLDER_ISSAVED = "ak_";
        public static final String MESSAGEVIEWHOLDER_SAVE = "z";
    }
    public class notification {
        public static final String NOTIFICATION_CLASS_1 = "tX";
        public static final String NOTIFICATION_CLASS_2 = "XU";
    }
    public class icons {
        public static final String ICON_HANDLER_CLASS = "Af";
        public static final String SHOW_LENS = "a";
        public static final String RECORDING_VIDEO = "c";
    }

    public class stories {
        public static final String RECENTSTORY_CLASS = "JM";
        public static final String ALLSTORY_CLASS = "Jy";
        public static final String LIVESTORY_CLASS = "Ps";
        public static final String DISCOVERSTORY_CLASS = "Fl";
    }

    public class groups {
        public static final String STORY_CLASS = "Ly";
        public static final String STORYARRAY_CLASS = "OI";
        public static final String STORYARRAY_METHOD = "a";
        public static final String STORYSECTION_CLASS = "IE";
        public static final String INTERFACE = "II";
        public static final String GETFRINEDMANAGER_METHOD = "e";
        public static final String GETUSERNAME_METHOD = "g";
    }

    public class bus {
        public static final String UPDATEEVENT_CLASS = "YN";
        public static final String GETBUS_CLASS = "Xy";
        public static final String GETBUS_METHOD = "a";
        public static final String BUS_POST = "a";
    }

    public class navbar {
        public static final String FORCENAVBAR_CLASS = "Un";
        public static final String FORCENAVBAR_METHOD = "a";
    }
}