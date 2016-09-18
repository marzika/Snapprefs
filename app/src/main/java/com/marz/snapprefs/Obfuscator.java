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
    public static final int SUPPORTED_VERSION_CODE = 847;
    public static final String SUPPORTED_VERSION_CODENAME = "9.31.1.0";
    public static final String ROOTDETECTOR_CLASS = "ali";
    public static final String[] ROOTDETECTOR_METHODS= {"b", "c", "d", "e"};
    public static final String FRIENDS_BF= "j";

    /**
     * Check if Snapprefs is compatible with this Snapchat version.
     *
     * @param versionCode The version code of the current Snapchat version
     * @return Whether it's supported
     */
    public static boolean isSupported(int versionCode) {
        return versionCode >= SUPPORTED_VERSION_CODE;
    }

    public class save {

        //User class
        public static final String USER_CLASS = "VU"; //+
        //ReceivedSnap class
        public static final String RECEIVEDSNAP_CLASS = "Vt"; //+
        public static final String RECEIVEDSNAP_BEING_SEEN = "d"; //+
        // ReceivedSnap.getCanonicalDisplayTime()
        public static final String RECEIVEDSNAP_DISPLAYTIME = "G";
        //StorySnap class
        public static final String STORYSNAP_CLASS = "VK"; //+
        //SnapView class
        public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.snapview.SnapView";
        //SnapView.show(ReceivedSnap, ChronologicalSnapProvider, Booleans(?))
        public static final String SNAPVIEW_SHOW = "b"; // prev. a
        public static final String STORYVIEW_SHOW = "a"; // prev. a
        public static final String STORYVIEW_SHOW_FIRST = "aea";
        public static final String STORYVIEW_SHOW_SECOND = "Vn";
        public static final String STORYVIEW_SHOW_THIRD = "adW";
        //First param of SnapView.show -> bdl, avf
        public static final String SNAPVIEW_SHOW_FIRST = "aea";
        //Second param of SnapView.show -> asz, alr
        public static final String SNAPVIEW_SHOW_SECOND = "Vn";
        //Third param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_THIRD = "HQ";
        //Fourth param of SnapView.show -> agd, abp
        public static final String SNAPVIEW_SHOW_FOURTH = "HS";
        //SnapView.hide(SnapViewEventAnalytics.EndReason)->(rm)
        public static final String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        //ImageResourceView class
        public static final String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public static final String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "b";
        //LandingPageActivity class
        public static final String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public static final String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public static final String SNAP_ISSCREENSHOTTED = "at";
        //public static final String SNAP_ISSCREENSHOTTED2 = "au";
        //Snap.getTimestamp()
        public static final String SNAP_GETTIMESTAMP = "S";
        //EndReason class
        public static final String ENDREASON_CLASS = "rm";
        //ImageResource -> Aw prev. avg, apz --  mIsSavedByRecipient:Z
        //ChatMedia instance variable name in ImageResource
        public static final String IMAGERESOURCE_VAR_CHATMEDIA = "c";
        //.model.chat.Chat -> zV.2
        //Chat.getTimeStamp()
        public static final String CHAT_GETTIMESTAMP = "S";
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public static final String STATEFULCHATFEEDITEM_GETSENDER = "i";
        //ScreenshotDetector class -> datetaken
        public static final String SCREENSHOTDETECTOR_CLASS = "Zm";
        //ScreenshotDetector.run(List)
        public static final String SCREENSHOTDETECTOR_RUN = "a";
        //SnapStateMessage class
        public static final String SNAPSTATEMESSAGE_CLASS = "auW";
        //SnapStateMessage.setScreenshotCount(Long)
        public static final String SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT = "b";
        //SentSnap Bitmap class
        public static final String SENT_CLASS = "UW";
        //SentSnap Bitmap method
        public static final String SENT_METHOD = "a";
        //ImagesnapRenderer
        public static final String IMAGESNAPRENDERER_CLASS = "YI";
        public static final String IMAGESNAPRENDERER_NEW_BITMAP = "a";
        public static final String DECRYPTEDSNAPVIDEO_CLASS = "UZ";
        public static final String CACHE_CLASS = "ahJ";
        //ImagesnapRenderer.start()
        public static final String IMAGESNAPRENDERER_START = "c";
        //ImageView instance in ImageSnapRenderer
        public static final String IMAGESNAPRENDERER_VAR_IMAGEVIEW = "a";//AspectMaintainedImageView!!
        //SnapImageBryo - JPEG_ENCODING_QUALITY
        public static final String SNAPIMAGEBRYO_CLASS = "VC";
        //VideoSnapRenderer.start()
        public static final String VIDEOSNAPRENDERER_CLASS = "YK";
        //VideoSnapRenderer.show()
        public static final String VIDEOSNAPRENDERER_SHOW = "c";
        //View Instance in VideoSnapRenderer
        public static final String VIDEOSNAPRENDERER_VAR_VIEW = "d";
        public static final String IMAGESNAPRENDERER_SETVIEW = "a";
        public static final String VIDEOSNAPRENDERER_SETVIEW = "a";
        //SwipeUpArrowView is the View containing the Chat element of the Friend's story screen
        public static final String SWIPEUPARROWVIEW_CLASS = "com.snapchat.opera.view.ArrowView";//?
        //SwipeUpArrowView.setLongFormAreaOnClickListener
        public static final String SWIPEUPARROWVIEW_SETONCLICK = "setTouchAreaOnClickListener";//?
        //stories_mystoryoverlaysave_icon
        public static final String STORIES_MYOVERLAYSAVE_ICON = "0x7f02030e";
    }
    public class datasaving {
        public static final String DSNAPDOWNLOADER_CLASS = "KL";
        public static final String DSNAPDOWNLOADER_DOWNLOADSNAP = "a";
        public static final String DOWNLOADREQUEST_CLASS = "com.snapchat.android.networkmanager.DownloadRequest";
        public static final String DYNAMICBYTEBUFFER_CLASS = "aku";
        public static final String NETWORKRESULT_CLASS = "Ae";
        public static final String DSNAPDOWNLOAD_CLASS = "KP$a";
        public static final String DSNAPDOWNLOAD_PARAM = "mp";
        public static final String DSNAPINTRODOWNLOAD_CLASS = "LR$1";
        public static final String LIVESTORYPRELOAD_CLASS = "ZO";
        public static final String LIVESTORYPRELOAD_METHOD = "H_";
        public static final String STORYPRELOAD_CLASS = "aaF";
        public static final String STORYPRELOAD_METHOD = "H_";
    }
    public class spoofing {
        //SpeedometerView class
        public static final String SPEEDOMETERVIEW_CLASS = "adL";
        //SpeedometerView.setSpeed(Float)
        public static final String SPEEDOMETERVIEW_SETSPEED = "a";
        public static final String LOCATION_CLASS = "Up";
        public static final String LOCATION_GETLOCATION = "c";
        public static final String WEATHER_CLASS = "Wa";
        public static final String WEATHER_FIRST = "avT";
        public static final String BATTERY_FILTER = "UQ";
        public static final String BATTERY_FULL_ENUM = "FULL";
    }
    public class select {
        //SendToFragment class
        public static final String SENDTOFRAGMENT_CLASS = "com.snapchat.android.fragments.sendto.SendToFragment";
        //SendToFragment.AddToList()
        public static final String SENDTOFRAGMENT_ADDTOLIST = "l";
        //TopView instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_TOPVIEW = "e";
        //FriendHashSet instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_SET = "l";
        //ArrayList instance variable in SendToFragment
        public static final String SENDTOFRAGMENT_VAR_ARRAYLIST = "n";
        //SendToAdapter class
        public static final String SENDTOADAPTER_CLASS = "OP";
        //List instance variable in SendToAdapter
        public static final String SENDTOADAPTER_VAR_LIST = "e";
        //Friend class
        public static final String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public static final String POSTTOSTORY_CLASS = "Vl";
        //PostToVenue class
        public static final String POSTTOVENUE_CLASS = "Vp";
    }
    public class sharing {
        //cameraStateEvent class
        public static final String CAMERASTATEEVENT_CLASS = "ajd";
        //snapCapturedEvent class
        public static final String SNAPCAPTUREDEVENT_CLASS = "bhv"; //prev. bhv, bfy ->from LandingPageActivity$8
        //snapCaptureContext class
        public static final String SNAPCAPTURECONTEXT_CLASS = "com.snapchat.android.util.eventbus.SnapCaptureContext";
        //BusProvider class
        public static final String BUSPROVIDER_CLASS = "bey";
        //BussProvider.returnBus()
        public static final String BUSPROVIDER_RETURNBUS = "a";
        //aa_chat_camera_upload
        public static final String UPLOAD_ICON = "0x7f020022";
    }
    public class lens {
        //ScheduledLensesProvider class
        public static final String LENSESPROVIDER_CLASS = "wr";
        //getLenses()
        public static final String LENSESPROVIDER_GETLENSES = "f";
    }
    public class stickers {
        //FastZippedAssetReader class
        public static final String ASSETREADER_CLASS = "agm";
        //FastZippedAssetReader.a
        public static final String ASSETREADER_A_CLASS = "TB$a";//TODO: not right, they changed it to byte[]
        //read()
        public static final String ASSETREADER_READ = "a";
        //SVG class
        public static final String SVG_CLASS = "gZ";
    }
    public class filters {
        //FilterLoader class
        public static final String LOADER_CLASS = "afM";
        //FilterLoader First Param
        public static final String LOADER_FIRST = "SP";
        //added instance
        public static final String FILTER_CLASS = "adG";
        //called Object
        public static final String OBJECT_CLASS = "afy";
        //onSnapCapturedEvent first param
        public static final String CAPTURED_FIRST = "ajD";
        //public.xml - battery_view
        public static final int BATTERY_VIEW = 2130968596;
        //public.xml - battery_icon
        public static final int BATTERY_ICON = 2131689648;
    }
    public class visualfilters {
        public static final String FILTERS_CLASS = "afg";
        public static final String FILTERSLOADER_CLASS = "afh";
        public static final String FILTERSLOADER_2_CLASS = "afn";
        public static final String SETFILTER_B_CLASS = "rA";
        public static final String GREYSCALE_CLASS = "afJ";
        public static final String ADDFILTER_CLASS = "afK";
        public static final String ADDFILTER_PARAM = "afH";
        public static final String ADDER_3_PARAM = "afn"; //TODO: find this
        public static final String ADDER_CLASS = "afi"; //TODO: find this
        public static final String ADDER_PARAM = "afr";
        public static final String SNAPCHAPTUREDEVENT_CLASS = "ajD";
    }
    public class timer {
        public static final String TAKESNAPBUTTON_CLASS = "com.snapchat.android.ui.camera.TakeSnapButton";
        public static final String TAKESNAPBUTTON_ONDRAW = "onDraw";
        public static final String TAKESNAPBUTTON_BLEAN1 = "c";
        public static final String TAKESNAPBUTTON_BLEAN2 = "e";
        public static final String TAKESNAPBUTTON_TIME = "b";
        public static final String TAKESNAPBUTTON_X = "n";
        public static final String TAKESNAPBUTTON_Y = "o";
    }
    public class chat { //9.31.1.0
        public static final String CHAT_CLASS = "com.snapchat.android.model.chat.Chat";
        public static final String CONVERSATION_CLASS = "com.snapchat.android.model.chat.ChatConversation";
        public static final String CHATFEEDITEM_CLASS = "com.snapchat.android.model.chat.ChatFeedItem";
        public static final String CONVERSATION_LOADOLDCHATS = "a";
        public static final String MESSAGEVIEWHOLDER_CLASS = "com.snapchat.android.fragments.chat.MessageViewHolder";
        public static final String MESSAGEVIEWHOLDER_METHOD = "b";
        public static final String MESSAGEVIEWHOLDER_VAR1 = "G";
        public static final String MESSAGEVIEWHOLDER_VAR2 = "d";
        public static final String MESSAGEVIEWHOLDER_ISSAVED = "ax_";
        public static final String MESSAGEVIEWHOLDER_ISFAILED = "N";
        public static final String MESSAGEVIEWHOLDER_SAVE = "x";
        public static final String CHATLAYOUT_CLASS = "Nd";
    }
    public class notification {
        public static final String NOTIFICATION_CLASS_1 = "vz";
        public static final String NOTIFICATION_CLASS_2 = "ajh";
    }
    public class icons {
        public static final String ICON_HANDLER_CLASS = "Em";
        public static final String SHOW_LENS = "a";
        public static final String RECORDING_VIDEO = "h";
    }
    public class stories {
        public static final String RECENTSTORY_CLASS = "Qp";
        public static final String ALLSTORY_CLASS = "PT";
        public static final String LIVESTORY_CLASS = "aaF";
        public static final String DISCOVERSTORY_CLASS = "Le";
        public static final String STORYLIST = "g";
        //public static final String DISCOVERSTORY_CLASS = "aay"; new story type "featured?" TODO: look into it
    }
    public class groups {
        public static final String STORY_CLASS = "Vp";
        public static final String STORYARRAY_CLASS = "Zx";
        public static final String STORYARRAY_METHOD = "a";
        public static final String STORYSECTION_CLASS = "OP";
        public static final String INTERFACE = "BS";
        public static final String GETFRIENDMANAGER_METHOD = "e";
        public static final String GETUSERNAME_METHOD = "g";
    }
    public class bus {
        public static final String UPDATEEVENT_CLASS = "Ds";
        public static final String GETBUS_CLASS = "RX";
        public static final String GETBUS_METHOD = "a";
        public static final String BUS_POST = "a";
    }
    public class navbar {
        public static final String FORCENAVBAR_CLASS = "SM";
        public static final String FORCENAVBAR_METHOD = "a";
    }
    public class paint{
        public static final String LEGACYCANVASVIEW_A = "BZ";
    }
}