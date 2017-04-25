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

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class Obfuscator implements Serializable {
    public static int SUPPORTED_VERSION_CODE = 1092;
    public final static String SUPPORTED_VERSION_CODENAME = "10.6.6.0";
    public final static String ROOTDETECTOR_CLASS = "jsh";//prev. aAY
    public final static String[] ROOTDETECTOR_METHODS= {"b", "c", "d", "e"};
    public final static String FRIENDS_BF= "l";//prev. j

    /**
     * Check if Snapprefs is compatible with this Snapchat version.
     *
     * @param versionCode The version code of the current Snapchat version
     * @return Whether it's supported
     */
    public static boolean isSupported(int versionCode) {
        return versionCode >= SUPPORTED_VERSION_CODE;
    }

    public static class save {

        //User class
        public final static String USER_CLASS = ""; //prev. DP
        //ReceivedSnap class
        public final static String RECEIVEDSNAP_CLASS = ""; //+ prev Sc
        public final static String OBJECT_MID = "mId";
        public final static String OBJECT_CACHEKEYSUFFIX = "mCacheKeyInstanceSuffix";
        public final static String RECEIVEDSNAP_BEING_SEEN = "c"; // prev. d
        // ReceivedSnap.getCanonicalDisplayTime()
        public final static String RECEIVEDSNAP_DISPLAYTIME = "d";//prev. G
        //StorySnap class
        public final static String STORYSNAP_CLASS = "PO"; //+ prev VK
        //SnapView class
        public final static String SNAPVIEW_CLASS = "com.snapchat.android.ui.snapview.SnapView";
        //SnapView.show(ReceivedSnap, ChronologicalSnapProvider, Booleans(?))
        public final static String SNAPVIEW_SHOW = "b"; // prev. a
        public final static String STORYVIEW_SHOW = "a"; // prev. a
        public final static String STORYVIEW_SHOW_FIRST = "aea";
        public final static String STORYVIEW_SHOW_SECOND = "Vn";
        public final static String STORYVIEW_SHOW_THIRD = "adW";
        //First param of SnapView.show -> bdl, avf
        public final static String SNAPVIEW_SHOW_FIRST = "aea";
        //Second param of SnapView.show -> asz, alr
        public final static String SNAPVIEW_SHOW_SECOND = "Vn";
        //Third param of SnapView.show -> agd, abp
        public final static String SNAPVIEW_SHOW_THIRD = "HQ";
        //Fourth param of SnapView.show -> agd, abp
        public final static String SNAPVIEW_SHOW_FOURTH = "HS";
        //SnapView.hide(SnapViewEventAnalytics.EndReason)->(rm)
        public final static String SNAPVIEW_HIDE = "a";
        //SnapPreviewFragment class
        public final static String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.preview.SnapPreviewFragment";
        public final static String SNAPPREVIEWFRAGMENT_METHOD1 = "a";//prev. l
        //ImageResourceView class
        public final static String IMAGERESOURCEVIEW_CLASS = "com.snapchat.android.ui.ImageResourceView";
        //imageResource instance variable name in ui.ImageResourceView
        public final static String IMAGERESOURCEVIEW_VAR_IMAGERESOURCE = "b";
        //LandingPageActivity class
        public final static String LANDINGPAGEACTIVITY_CLASS = "com.snapchat.android.LandingPageActivity";
        //Snap class
        public final static String SNAP_CLASS = "com.snapchat.android.model.Snap";
        //Snap.isScreenshotted()
        public final static String SNAP_ISSCREENSHOTTED = "at";
        //public final static String SNAP_ISSCREENSHOTTED2 = "au";
        //Snap.getTimestamp()
        public final static String SNAP_GETTIMESTAMP = "i";//prev. S
        //EndReason class
        public final static String ENDREASON_CLASS = "rm";
        //ImageResource -> Aw prev. avg, apz --  mIsSavedByRecipient:Z
        //ChatMedia instance variable name in ImageResource
        public final static String IMAGERESOURCE_VAR_CHATMEDIA = "c";
        //.model.chat.Chat -> zV.2
        //Chat.getTimeStamp()
        public final static String CHAT_GETTIMESTAMP = "S";
        //.model.chat.StatefulChatFeedItem
        //StatefulChatFeedItem.getSender()
        public final static String STATEFULCHATFEEDITEM_GETSENDER = "m";
        //ScreenshotDetector class -> datetaken
        public final static String SCREENSHOTDETECTOR_CLASS = "Tk";//prev. Zm
        //ScreenshotDetector.run(List)
        public final static String SCREENSHOTDETECTOR_RUN = "a";
        //SnapStateMessage class
        public final static String SNAPSTATEMESSAGE_CLASS = "aNt";//prev. auW
        //SnapStateMessage.setScreenshotCount(Long)
        public final static String SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT = "a";//prev. b
        //SentSnap Bitmap class
        public final static String SENT_CLASS = "UW";
        //SentSnap Bitmap method
        public final static String SENT_METHOD = "a";
        //ImagesnapRenderer
        public final static String IMAGESNAPRENDERER_CLASS = "ard";//prev YI
        public final static String IMAGESNAPRENDERER_CLASS2 = IMAGESNAPRENDERER_CLASS + "$1";
        public final static String IMAGESNAPRENDERER_NEW_BITMAP = "a";
        // Inside IMAGESNAPRENDERER, Object is of type extending mediabryo and contains mKey
        public final static String OBJECT_KEYHOLDERCLASSOBJECT = "b"; //.prev b
        public final static String OBJECT_KEYHOLDER_KEY = "b"; //.prev ahZ

        public final static String DECRYPTEDSNAPVIDEO_CLASS = "Sh";//.prev UZ
        public final static String CACHE_CLASS = "amh";//.prev ahZ
        public final static String CACHE_KEYTOITEMMAP = "b"; //.prev mKeyToItemMap
        public final static String CACHE_ITEM_PATH = "a";
        //ImagesnapRenderer.start()
        public final static String IMAGESNAPRENDERER_START = "c";
        //ImageView instance in ImageSnapRenderer
        public final static String IMAGESNAPRENDERER_VAR_IMAGEVIEW = "a";//AspectMaintainedImageView!!
        //SnapImageBryo - JPEG_ENCODING_QUALITY
        public final static String SNAPIMAGEBRYO_CLASS = "aph";//prev. VC
        //VideoSnapRenderer.start()
        public final static String VIDEOSNAPRENDERER_CLASS = "YK";
        //VideoSnapRenderer.show()
        public final static String VIDEOSNAPRENDERER_SHOW = "c";
        //View Instance in VideoSnapRenderer
        public final static String VIDEOSNAPRENDERER_VAR_VIEW = "d";
        public final static String IMAGESNAPRENDERER_SETVIEW = "a";
        public final static String VIDEOSNAPRENDERER_SETVIEW = "a";
        //SwipeUpArrowView is the View containing the Chat element of the Friend's story screen
        public final static String SWIPEUPARROWVIEW_CLASS = "com.snapchat.opera.view.ArrowView";//?
        //SwipeUpArrowView.setLongFormAreaOnClickListener
        public final static String SWIPEUPARROWVIEW_SETONCLICK = "setTouchAreaOnClickListener";//?
        //stories_mystoryoverlaysave_icon
        public final static String STORIES_MYOVERLAYSAVE_ICON = "0x7f02030e";
        // Get Username function of Friend Class
        public static final String GET_FRIEND_USERNAME = "d";//prev. g
        public static final String CLASS_FRIEND_MINI_PROFILE_POPUP_FRAGMENT = "aqT";//prev. com.snapchat.android.fragments.FriendMiniProfilePopupFragment
        // com.snapchat.android.fragments.FriendMiniProfilePopupFragment Friend Field
        public final static String FRIEND_MINI_PROFILE_POPUP_FRIEND_FIELD = "i";// Prev F
        // com.snapchat.android.fragments.FriendMiniProfilePopupFragement getCachedProfilePicutres method
        public final static String FRIEND_MINI_PROFILE_POPUP_GET_CACHED_PROFILE_PICTURES = "e"; //Prev y
        // com.snapchat.android.fragments.FriendMiniProfilePopupFragement FriendsProfileImagesCache field
        public final static String FRIEND_MINI_PROFILE_POPUP_FRIENDS_PROFILE_IMAGES_CACHE = "g";//prev i
        public final static String PROFILE_IMAGES_CACHE_GET_PROFILE_IMAGES = "a";
        public final static String PROFILE_IMAGE_UTILS_PROFILE_IMAGE_SIZE_INNER_CLASS = "com.snapchat.android.util.profileimages.ProfileImageUtils$ProfileImageSize";
        public final static String MINI_PROFILE_SNAPCODE = "l";//Prev q

        // SnapTimerView class
        public final static String CLASS_SNAP_TIMER_VIEW = "com.snapchat.android.ui.SnapTimerView";
        public final static String METHOD_SNAPTIMERVIEW_ONDRAW = "onDraw";

        // StoryTimerView class
        public final static String CLASS_NEW_STORY_TIMER_VIEW = "com.snapchat.android.framework.ui.views.NewConcentricTimerView";
        public final static String CLASS_STORY_TIMER_VIEW = "com.snapchat.android.framework.ui.views.ConcentricTimerView"; //Prev com.snapchat.android.ui.StoryTimerView
        public final static String METHOD_STORYTIMERVIEW_ONDRAW = "onDraw";

        // TextureVideoView
        public final static String CLASS_TEXTURE_VIDEO_VIEW = "com.snapchat.opera.shared.view.TextureVideoView";
        public final static String METHOD_TVV_START = "start";
        public final static String METHOD_TVV_SETLOOPING = "setLooping";

        // SnapCountdownController class
        public final static String CLASS_SNAP_COUNTDOWN_CONTROLLER = "com.snapchat.android.app.shared.feature.feed.controller.SnapCountdownController";//.Prev com.snapchat.android.controller.countdown.SnapCountdownController
        public final static String METHOD_SCC_VAR1 = "a";

        // SnapViewSessionStopReason class
        public final static String CLASS_SNAP_VIEW_SESSION_STOP_REASON = "com.snapchat.android.ui.snapview.SnapViewSessionStopReason";

        // SENT SNAP HANDLING \\

        //Inside SnapPreviewFragment
        public final static String OBJECT_SNAP_EDITOR_VIEW = "b";
        //Inside SnapEditorView
        public final static String OBJECT_MEDIABRYO = "p"; //.prev p
        public final static String CLASS_MEDIABRYO_VIDEO = "apx";//prev. VZ
        public final static String METHOD_GET_SENT_BITMAP = "a";
        //Inside MediaBryo
        public final static String OBJECT_MCLIENTID = "mClientId";
        public final static String OBJECT_MVIDEOURI = "mVideoUri";

        public final static String STORY_DETAILS_PACKET = "aGg";
        public final static String SDP_GET_ENUM_METHOD = "b";
        public final static String SDP_GET_OBJECT = "a";
        public final static String SDP_GET_STRING = "d";

        public final static String STORY_VIEWER_MEDIA_CACHE = "com.snapchat.android.stories.viewer.StoryViewerMediaCache";
        public final static String VIEWING_STORY_METHOD = "a";
        public final static String VIEWING_STORY_VAR4 = "aEm$a";
        public final static String SVMC_STORYLIST_OBJECT = "c";

        public final static String STORY_IMAGE_HOLDER = "gC";

        public final static String STORY_LOADER = "asT";
        public final static String SL_ISVIEWING_METHOD = "i";
        public final static String SL_ON_RESOURCE_READY_METHOD = "onResourceReady";
        public final static String SL_VAR2 = "gt";

        public final static int OPERA_PAGE_VIEW_ID = +2131689491;

        public final static String DIRECTIONAL_LAYOUT_CLASS = "com.snapchat.opera.ui.DirectionalLayout";
        public final static String MCANONICALDISPLAYNAME = "mCanonicalDisplayTime";
    }
    public static class datasaving {
        public final static String DSNAPDOWNLOADER_CLASS = "Df$a";//Prev. KL
        public final static String DSNAPDOWNLOADER_DOWNLOADSNAP = "a";
        public final static String DOWNLOADREQUEST_CLASS = "com.snapchat.android.app.shared.framework.network.manager";//prev. com.snapchat.android.networkmanager.DownloadRequest
        public final static String DYNAMICBYTEBUFFER_CLASS = "aku";
        public final static String DOWNLOADREQUEST_HOLDER_CLASS = "Qq";
        public final static String NETWORKRESULT_CLASS = "Ae";
        public final static String DSNAPDOWNLOAD_CLASS = "Df$a";//prev. KP$a
        public final static String DSNAPDOWNLOAD_PARAM = "mz";//prev. mp
        public final static String DSNAPINTRODOWNLOAD_CLASS = "Df$a";
        public final static String LIVESTORYPRELOAD_CLASS = "asx";
        public final static String LIVESTORYPRELOAD_METHOD = "d";
        public final static String STORYPRELOAD_CLASS = "aaF";
        public final static String STORYPRELOAD_METHOD = "H_";
    }
    public static class spoofing { //DONE
        //SpeedometerView class
        public final static String SPEEDOMETERVIEW_CLASS = "cwn";
        //SpeedometerView.setSpeed(Float)
        public final static String SPEEDOMETERVIEW_SETSPEED = "a";
        public final static String LOCATION_CLASS = "com.snapchat.android.app.shared.location.CurrentLocationProvider";
        public final static String LOCATION_GETLOCATION = "a";
        public final static String LOCATION_GETLOCATION_PARAM = "hcj";
        public final static String WEATHER_CLASS = "ivc";
        public final static String WEATHER_FIRST = "nwl"; //mTempF = b mTempC = a
        public final static String BATTERY_FILTER = "gro";
        public final static String BATTERY_FULL_ENUM = "FULL"; //BatteryLevel = gpz
    }
    public static class select {
        //SendToFragment class
        public final static String SENDTOFRAGMENT_CLASS = "com.snapchat.android.fragments.sendto.SendToFragment";
        //SendToFragment.AddToList()
        public final static String SENDTOFRAGMENT_ADDTOLIST = "b";//prev. l
        //TopView instance variable in SendToFragment
        public final static String SENDTOFRAGMENT_VAR_TOPVIEW = "f";//prev. e
        //FriendHashSet instance variable in SendToFragment
        public final static String SENDTOFRAGMENT_VAR_SET = "m";//prev. l
        //ArrayList instance variable in SendToFragment
        public final static String SENDTOFRAGMENT_VAR_ARRAYLIST = "p";//prev n
        //SendToAdapter class
        public final static String SENDTOADAPTER_CLASS = "afe";//prev OP
        //List instance variable in SendToAdapter
        public final static String SENDTOADAPTER_VAR_LIST = "c";//prev e
        //Friend class
        public final static String FRIEND_CLASS = "com.snapchat.android.model.Friend";
        //PostToStory class
        public final static String POSTTOSTORY_CLASS = "aoZ";//prev. Vl
        //PostToVenue class
        public final static String POSTTOVENUE_CLASS = "apb";//prev. Vp
    }
    public static class sharing { //DONE
        //cameraStateEvent class
        public final static String CAMERASTATEEVENT_CLASS = "gia";
        //snapCapturedEvent class
        public final static String SNAPCAPTUREDEVENT_CLASS = "bhv"; // ??? bhv, bfy ->from LandingPageActivity$8
        //snapCaptureContext class
        public final static String SNAPCAPTURECONTEXT_CLASS = "com.snapchat.android.busevents.SnapCaptureContext";
        //aa_chat_camera_upload
        public final static String UPLOAD_ICON = "0x7f02001a";

        //API, SCREENSHOT # TAKE_PHOTO_METHOD
        public static final String TAKE_PHOTO_METHOD = "ghw$a";
    }
    public static class lens {
        public final static String LENSCLASS = "com.snapchat.android.model.lenses.Lens";

        public final static String CLASS_LENSLIST_TYPE = "aLo";//.prev atz
        public final static String LENSCALLBACK_CLASS = "BT";//.prev AN
        public final static String LENSCALLBACK_ONJSONRESULT_VAR2 = "Qq";//.prev Ae
        //ScheduledLensesProvider class
        public final static String LENSESPROVIDER_CLASS = "wr";
        //getLenses()
        public final static String LENSESPROVIDER_GETLENSES = "f";

        public final static String LENSPREPARESTATECHANGE = "com.snapchat.android.util.eventbus.LensPrepareStateChangedEvent";
        public final static String STATECHANGEPREPARESTATUSENUM = LENSPREPARESTATECHANGE + "$PrepareStatus";
        public final static String AUTHENTICATION_CLASS = "arS";//prev. Zq
        public final static String SIGNITURE_CHECK_METHOD = "a";
    }
    public static class stickers {
        //FastZippedAssetReader class
        public final static String ASSETREADER_CLASS = "agm";
        //FastZippedAssetReader.a
        public final static String ASSETREADER_A_CLASS = "TB$a";//TODO: not right, they changed it to byte[]
        //read()
        public final static String ASSETREADER_READ = "a";
        //SVG class
        public final static String SVG_CLASS = "hc";//prev gZ
    }
    public static class filters {
        //FilterLoader class
        public final static String LOADER_CLASS = "axf";//prev. afM
        //FilterLoader First Param
        public final static String LOADER_FIRST = "amH";//prev. SP
        //added instance
        public final static String FILTER_CLASS = "avV";//prev. adG
        //called Object
        public final static String OBJECT_CLASS = "awU";//prev. afy
        //onSnapCapturedEvent first param
        public final static String CAPTURED_FIRST = "azW";//prev. ajD
        //public.xml - battery_view
        public static int BATTERY_VIEW = 2130968600;
        //public.xml - battery_icon
        public static int BATTERY_ICON = 2131689687;
        public final static String SNAPCHAPTUREDEVENT_CLASS = "";
    }
    public class visualfilters {
        public static final String FILTERS_CLASS = "On";//prev. afg
        public static final String FILTERSLOADER_CLASS = "awB";//prev. afh
        public static final String FILTERSLOADER_2_CLASS = "awH";//prev. afn
        public static final String SETFILTER_B_CLASS = "sK";//prev. rA
        public static final String GREYSCALE_CLASS = "axc";//prev. afJ
        public static final String ADDFILTER_CLASS = "axd";//prev. afK
        public static final String ADDFILTER_PARAM = "axa";//prev. afH
        public static final String ADDER_3_PARAM = "awH";
        public static final String ADDER_CLASS = "awC"; //prev. afi
        public static final String ADDER_PARAM = "awL"; //prev afr
        public static final String SNAPCHAPTUREDEVENT_CLASS = "azW";//prev. ajD
        public static final String FILTERMETRICSPROVIDER_CLASS = "yo";
        public static final String ANNOTATEDMEDIABRYO = "com.snapchat.android.model.AnnotatedMediabryo";
        public static final String VISUALFILTERBASE = "awI";
        public static final String FILTER_GETVIEW = "c";
        public static final String BRYO_SNAPTYPE = "com.snapchat.android.app.feature.messaging.chat.type.SnapType";
        public static final String VISUALFILTER_TYPE = "com.snapchat.android.app.shared.feature.preview.model.filter.VisualFilterType";
        public static final String VISUAL_FILTER_TYPE_CHECK_METHOD = "c";
        public static final String VISUAL_FILTER_TYPE_CHECK_METHOD_PARAMETER_CLASS = "Oi";
    }
    public static class timer { //DONE
        public final static String TAKESNAPBUTTON_CLASS = "com.snapchat.android.ui.camera.TakeSnapButton";
        public final static String TAKESNAPBUTTON_ONDRAW = "onDraw";
        public final static String TAKESNAPBUTTON_BLEAN1 = "c";
        public final static String TAKESNAPBUTTON_BLEAN2 = "e";
        public final static String TAKESNAPBUTTON_TIME = "b";
        public final static String TAKESNAPBUTTON_X = "p";//prev. o
        public final static String TAKESNAPBUTTON_Y = "q";//prev. p
        public final static String RECORDING_MESSAGE_HOOK_CLASS = "crr";
        public final static String RECORDING_MESSAGE_HOOK_METHOD = "handleMessage";
    }
    public static class chat {
        public final static String CHAT_CLASS = "Ie";
        public final static String CONVERSATION_CLASS = "Ig";//prev. com.snapchat.android.model.chat.ChatConversation
        public final static String ABSTRACT_CONVERSATION_CLASS = "IM";
        public final static String CHATFEEDITEM_CLASS = "com.snapchat.android.model.chat.ChatFeedItem";
        public final static String CONVERSATION_LOADOLDCHATS = "a";
        public final static String MESSAGEVIEWHOLDER_CLASS = "com.snapchat.android.app.feature.messaging.chat.impl.viewholder.MessageViewHolder";
        public final static String MESSAGEVIEWHOLDER_METHOD = "b";
        public final static String MESSAGEVIEWHOLDER_VAR1 = "z";
        public final static String MESSAGEVIEWHOLDER_VAR2 = "d";
        public final static String MESSAGEVIEWHOLDER_ISSAVED = "E_";
        public final static String MESSAGEVIEWHOLDER_ISFAILED = "N_";//prev. N
        public final static String MESSAGEVIEWHOLDER_SAVE = "e";//prev. x
        public final static String CHATLAYOUT_CLASS = "Kc";//prev. Nd
        public final static String CHATLAYOUT_INSTANTIATEITEM = "instantiateItem";//prev. Nd
        public final static String BUS_CLASS = "com.squareup.otto.Bus";
        public final static String CONVERSATION_VAR3 = "Ea";//prev. Vr
        public final static String USERNAME_HOLDER_CLASS = "B";
        public final static String HOLDER_USERNAME = "e";
        public final static String SORTED_CHAT_LIST = "L";//prev. w
        public final static String CHAT_FEED_ITEM = "Ii";//prev. Wv
        public final static String CHAT_MEDIA_CLASS = "com.snapchat.android.app.feature.messaging.chat.model2.ChatMedia";
        public final static String ISTYPING_CLASS = "FI";
        public final static String ISTYPING_METHOD = "a";
        public final static String SENT_CHAT_METHOD = "c";
        public final static String SECURE_CHAT_SERVICE_CLASS = "com.snapchat.android.util.chat.SecureChatService";
        public final static String SCS_MESSAGE_METHOD = "a";
        public final static String CHAT_MESSAGE_BASE_CLASS = "aMj";
        public final static String CHAT_MESSAGE_DETAILS_CLASS = "aJE";
    }
    public static class notification {//NEEDS REWORK
        public final static String NOTIFICATION_CLASS_1 = "hgt$b.a.a";//prev. xH
        public final static String NOTIFICATION_CLASS_2 = "iwf";//prev. azz
    }
    public static class icons { //DONE
        public final static String CAPTIONOPENED_CLASS = "cug";
        public final static String CAPTIONOPENED_METHOD = "e";
    }
    public static class stories {
        public final static String RECENTSTORY_CLASS = "ahd";
        public final static String RECENTSTORY_GETUSERNAME = "d";
        public final static String ALLSTORY_CLASS = "agD";
        public final static String ALLSTORY_GETFRIEND = "h";
        public final static String LIVESTORY_CLASS = "aee";
        public final static String DISCOVERSTORY_CLASS = "ati";
        public final static String STORYLIST = "g";
        public final static String RECENTSTORIES_CLASS = "com.snapchat.android.fragments.stories.StoriesFragment";
        public final static String STORIES_FRAGMENT_POPULATEARRAY = "c";
        public final static String FRIENDMANAGER_CLASS = "com.snapchat.android.model.FriendManager";
        public final static String FRIENDMANAGER_RETURNINSTANCE = "h";
        //public final static String DISCOVERSTORY_CLASS = "aay"; new story type "featured?" TODO: look into it
        //search for "AUTO_ADVANCE_RECENT_UPDATES"
        public final static String AUTOADVANCE_CLASS2 = "atJ";
        public final static String AUTOADVANCE_METHOD2 = "a";
        public final static String AUTOADVANCE_CLASS = "aty";
        public final static String AUTOADVANCE_METHOD = "a";

        public final static String TILE_HANDLER_CLASS = "atk";
        public final static String GET_TILES_METHOD = "c";

        public final static String STORY_LOADER_CLASS = "ahm";
        public final static String SL_LOAD_METHOD = "a";
        public final static String VIEWED_STORY_CLASS = "agD";
        public final static String VS_FRIEND_OBJECT = "b";
    }
    public static class groups {
        public final static String STORY_CLASS = "apb";//prev. Vp
        public final static String STORYARRAY_CLASS = "ase";//prev. Zx
        public final static String STORYARRAY_METHOD = "a";
        public final static String STORYSECTION_CLASS = "afe";//prev. OP
        public final static String INTERFACE = "Ml";//prev. BS
        public final static String GETFRIENDMANAGER_METHOD = "h";//prev. e
        public final static String GETUSERNAME_METHOD = "d";//prev. g
        public final static String GETDISPLAYNAME_METHOD = "f";
    }
    public static class bus {
        public final static String UPDATEEVENT_CLASS = "Ds";
        public final static String GETBUS_CLASS = "aju";//prev. RX
        public final static String GETBUS_METHOD = "a";
        public final static String BUS_POST = "a";
    }
    public static class navbar { //DONE
        public final static String FORCENAVBAR_CLASS = "ioe"; // prev. amv
        public final static String FORCENAVBAR_METHOD = "b";
    }
    public static class paint{ //DONE
        public final static String LEGACYCANVASVIEW_A = "gpl"; //NEW CONSTRUCTOR
    }
    public static class flash {
        public final static String ISVISIBLE_FIELD = "mIsVisible";
        public final static String SWIPELAYOUT_FIELD = "p";//prev. n
        public final static String GETRESID_OBJECT = "g";//prev. e
        public final static String ISSCROLLED_METHOD = "c";
        public final static String KEYEVENT_CLASS = "Xk";//prev. CX
        public final static String KEYCODE_FIELD = "a";
        public final static String FLASH_METHOD = "a";//prev. b
        public final static String OVERLAY_FIELD = "z";//prev. y
    }
    public static class friendmojis {
        public final static String ON_FRIENDS_UPDATE_METHOD = "u";//prev. w
        public final static String FRIEND_MANAGER_CLASS = "com.snapchat.android.model.FriendManager";
        public final static String FRIENDS_MAP_FIELD = "mOutgoingFriendsListMap";
        public final static String GET_VALUES_METHOD = "b";
        public final static String IS_IT_ME_METHOD = "n";//prev. k
        public final static String GET_FRIENDMOJI_STRING_METHOD = "q";//Prev. o
    }
    public static class misc { //DONE
        public final static String CAPTIONVIEW = "hmv";
        public final static String CAPTIONVIEW_TEXT_LIMITER = "c";
        public final static String PREFERENCES_CLASS = "com.snapchat.android.app.shared.persistence.UserPrefs";
        public final static String GETUSERNAME_METHOD = "M";

        public final static String ADVANCE_TYPE_CLASS = "klj";
        public final static String NO_AUTO_ADVANCE_OBJECT = "a"; //???
    }
    public static class premium{
        public final static String SNAP_UPDATE_CLASS = "zT";
    }

    public static void writeGsonFile()
    {
        try {
            Writer writer = new FileWriter(Preferences.getSavePath() + "/ObfuscationFile.json");

            GsonBuilder gsonBuilder  = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);
            gsonBuilder.serializeNulls();

            Gson gson = gsonBuilder.create();
            gson.toJson(new Obfuscator(), writer);

            Class[] arrClasses = Obfuscator.class.getClasses();
            for( Class clazz : arrClasses ) {
                Object instance = clazz.newInstance();

                JsonElement jsonElement = gson.toJsonTree(instance);
                JsonObject jsonObject = new JsonObject();
                jsonObject.add(clazz.getSimpleName(), jsonElement);

                writer.write(",\n");
                gson.toJson(jsonObject, writer);
            }

            writer.flush();
            writer.close();
        } catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public static void readJsonFile() {
        FileReader fileReader;

        try {
            fileReader = new FileReader(Preferences.getSavePath() + "/ObfuscationFile.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        JsonElement jelement = new JsonParser().parse(fileReader);
        JsonObject  jobject = jelement.getAsJsonObject();

        Logger.log("Object: " + jobject.toString());

        for( Field field : Obfuscator.class.getFields())
        {
            String name = field.getName();
            Logger.log("Name: " + name + " Val: " + jobject.get(name).toString());
        }
    }

    public static void saveCurrentFile() throws IOException, IllegalAccessException {
        File file = new File(Preferences.getSavePath() + "/ObfuscationFile.json");

        FileOutputStream out = new FileOutputStream(file);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("      ");

        Class[] classes = Obfuscator.class.getClasses();

        writer.beginArray();

        writer.beginObject();

        for( Field field : Obfuscator.class.getFields())
        {
            writeObject(writer, field.getName(), field.get(null));
        }

        for( Class clazz : classes ) {
            writer.name(clazz.getSimpleName());

            Field[] fields = clazz.getFields();

            writer.beginObject();
            for( Field field : fields ) {
                writeObject(writer, field.getName(), field.get(null));
            }
            writer.endObject();
        }
        writer.endObject();
        writer.endArray();
    }

    /*public static void readCurrentFile() throws IOException, IllegalAccessException {
        File file = new File(Preferences.getSavePath() + "/ObfuscationFile.json");

        FileInputStream in = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        Class[] classes = Obfuscator.class.getClasses();

        reader.beginArray();

        reader.beginObject();

        String currentClass = null;

        while(reader.hasNext())
        {
            if( reader.peek() == JsonToken.NAME)
            String fieldName = reader.nextName();

            readAndSet(reader, currentClass, fieldName);
        }

        for( Class clazz : classes ) {
            reader.name(clazz.getSimpleName());

            Field[] fields = clazz.getFields();

            reader.beginObject();
            for( Field field : fields ) {
                writeObject(reader, field.getName(), field.get(null));
            }
            reader.endObject();
        }
        reader.endObject();
        reader.endArray();
    }*/

    public static void writeObject( JsonWriter writer, String name, Object obj) throws IOException {
        if(obj instanceof Boolean)
            writer.name(name).value((boolean) obj);
        else if(obj instanceof Integer)
            writer.name(name).value((int) obj);
        else if(obj instanceof String)
            writer.name(name).value((String) obj);
        else if(obj instanceof Double)
            writer.name(name).value((double) obj);
        else if(obj instanceof Float)
            writer.name(name).value((float) obj);
    }

    public static void readAndSet(JsonReader reader, String className, String fieldName) throws IOException, IllegalAccessException {
        String searchForClass = "com.marz.snapprefs.Obfuscator";

        if( className != null )
            searchForClass += "$" + className;

        Class clazz;

        try {
            clazz = Class.forName(searchForClass);
        } catch (ClassNotFoundException e) {
            Logger.log("Could not find class: " + searchForClass);
            return;
        }

        Field field;

        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Logger.log("No field found of: " + className + " - " + fieldName);
            return;
        }

        Type type = field.getType();

        Logger.log("Reading and setting type: " + type);

        if( reader.peek() == JsonToken.NULL )
            field.set(null, null);
        else if( type == Boolean.class )
            field.set(null, reader.nextBoolean());
        else if( type == int.class)
            field.set(null, reader.nextInt());
        else if(type == String.class)
            field.set(null, reader.nextString());
        else if(type == double.class)
            field.set(null, reader.nextDouble());
        else if(type == float.class)
            field.set(null, reader.nextDouble());
    }

    public static void testReflect()
    {
        Logger.log("ClientID: " + Obfuscator.save.OBJECT_MCLIENTID);

        setObfuscatorValue("save", "OBJECT_MCLIENTID", "FUCKOFF");
        Logger.log("ClientID: " + Obfuscator.save.OBJECT_MCLIENTID);
    }

    public static boolean setObfuscatorValue(String className, String fieldName, String value) {

        try {
            Class clazz = Class.forName("com.marz.snapprefs.Obfuscator$" + className);
            Field field = clazz.getDeclaredField(fieldName);
            field.set(null, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}