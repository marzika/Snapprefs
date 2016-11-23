package com.marz.snapprefs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.XModuleResources;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.marz.snapprefs.Preferences.Prefs;
import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

/**
 * Created by MARZ on 2016. 04. 12..
 */
public class Stories {
    public static List<String> peopleToHide = new ArrayList<>();

    public static List<Friend> friendList = new ArrayList<>();
    public static HashMap<Class<?>, Callable> filterMap = new HashMap<>();

    static void initStories(final XC_LoadPackage.LoadPackageParam lpparam) {
        readBlockedList();

        buildFilterMap(lpparam);

        findAndHookMethod(Obfuscator.stories.STORY_LOADER_CLASS, lpparam.classLoader, Obfuscator.stories.SL_LOAD_METHOD, List.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                ArrayList<Object> originalList = (ArrayList<Object>) param.args[0];
                ArrayList<Object> iterativeList = new ArrayList<>(originalList);

                for (Object storyItemObject : iterativeList) {
                    Class<?> storyItemClass = storyItemObject.getClass();
                    Callable callable = filterMap.get(storyItemClass);

                    if (callable == null) {
                        Logger.log("Unhandled story item: " + storyItemClass.getCanonicalName());
                        return;
                    }

                    callable.callBackMethod(originalList, storyItemObject);
                }
            }
        });


        if( Preferences.getBool(Prefs.AUTO_ADVANCE)) {
            Class ExitEventTypeClass = findClass("com.snapchat.android.framework.analytics.perf.ExitEvent", lpparam.classLoader);
            final Object ExitEvent_AUTO_ADVANCE = getStaticObjectField(ExitEventTypeClass, "AUTO_ADVANCE");
            findAndHookMethod(Obfuscator.stories.AUTOADVANCE_CLASS2, lpparam.classLoader, Obfuscator.stories.AUTOADVANCE_METHOD2, ExitEventTypeClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Object exitEvent = param.args[0];

                    if (exitEvent == ExitEvent_AUTO_ADVANCE) {
                        Logger.log("Skipped auto advance");
                        param.setResult(null);
                    }
                }
            });
        }
    }

    private static void readFriendList(final ClassLoader classLoader) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Object friendManager = XposedHelpers.callStaticMethod(XposedHelpers.findClass(Obfuscator.stories.FRIENDMANAGER_CLASS, classLoader), Obfuscator.stories.FRIENDMANAGER_RETURNINSTANCE);
                List friends = (List) getObjectField(getObjectField(friendManager, "mOutgoingFriendsListMap"), "mList");
                friendList.clear();
                for (int i = 0; i <= friends.size() - 1; i++) {
                    String username = (String) callMethod(friends.get(i), Obfuscator.save.GET_FRIEND_USERNAME);
                    if (peopleToHide.contains(username)) {
                        friendList.add(new Friend(username, "", true));
                    } else {
                        friendList.add(new Friend(username, "", false));
                    }
                }
                Collections.sort(friendList, new Friend.friendComparator());
            }
        }).start();

    }

    public static void readBlockedList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String read = FileUtils.readFromSDFolder("blockedstories").replaceAll("\n", "");
                if (!read.equals("0")) {
                    peopleToHide = Arrays.asList(read.split(";"));
                }
            }
        }).start();

    }

    public static void addSnapprefsBtn(XC_InitPackageResources.InitPackageResourcesParam resparam, final XModuleResources mResources) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "stories", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final FrameLayout relativeLayout = (FrameLayout) liparam.view.findViewById(liparam.res.getIdentifier("top_panel", "id", Common.PACKAGE_SNAP));
                final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("myfriends_action_bar_search_button", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                final FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
                //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams2.topMargin = HookMethods.px(8.0f);
                layoutParams2.rightMargin = HookMethods.px(115.0f);
                final ImageView snapPrefsBtn = new ImageView(HookMethods.SnapContext);
                snapPrefsBtn.setImageDrawable(mResources.getDrawable(R.drawable.story_filter));
                snapPrefsBtn.setScaleX(0.75f);
                snapPrefsBtn.setScaleY(0.75f);
                Logger.log("Adding Snapprefs button to story section");
                snapPrefsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        readFriendList(HookMethods.classLoader);
                        FragmentTransaction ft = HookMethods.SnapContext.getFragmentManager().beginTransaction();
                        Fragment prev = HookMethods.SnapContext.getFragmentManager().findFragmentByTag("dialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        // Create and show the dialog.
                        DialogFragment newFragment = FriendListDialog.newInstance();
                        newFragment.show(ft, "dialog");
                    }
                });

                HookMethods.SnapContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        relativeLayout.addView(snapPrefsBtn, layoutParams2);
                    }
                });
            }
        });
    }

    private static void buildFilterMap(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> recentStory = XposedHelpers.findClass(Obfuscator.stories.RECENTSTORY_CLASS, lpparam.classLoader);
        Class<?> allStory = XposedHelpers.findClass(Obfuscator.stories.ALLSTORY_CLASS, lpparam.classLoader);
        Class<?> liveStory = XposedHelpers.findClass(Obfuscator.stories.LIVESTORY_CLASS, lpparam.classLoader);
        Class<?> featuredStory = XposedHelpers.findClass(Obfuscator.stories.DISCOVERSTORY_CLASS, lpparam.classLoader);

        filterMap.put(recentStory, new Callable() {
            public void callBackMethod(ArrayList<Object> objectList, Object obj) {
                if (Preferences.getBool(Prefs.HIDE_PEOPLE)) {
                    String friendUsername = (String) callMethod(obj, Obfuscator.stories.RECENTSTORY_GETUSERNAME);

                    if (friendUsername != null && peopleToHide.contains(friendUsername)) {
                        Logger.log("Contains blocked friend.... Removing " + StringUtils.obfus(friendUsername));
                        objectList.remove(obj);
                    }
                }
            }
        });

        filterMap.put(allStory, new Callable() {
            public void callBackMethod(ArrayList<Object> objectList, Object obj) {
                if (Preferences.getBool(Prefs.HIDE_PEOPLE)) {
                    String friendUsername = (String) callMethod(obj, Obfuscator.stories.RECENTSTORY_GETUSERNAME);

                    if (friendUsername != null && peopleToHide.contains(friendUsername)) {
                        Logger.log("Contains blocked friend.... Removing " + StringUtils.obfus(friendUsername));
                        objectList.remove(obj);
                    }
                }
            }
        });

        filterMap.put(liveStory, new Callable() {
            public void callBackMethod(ArrayList<Object> objectList, Object obj) {
                if (Preferences.getBool(Prefs.HIDE_LIVE)) {
                    Logger.log("Blocking Live Story");
                    objectList.remove(obj);
                }
            }
        });

        filterMap.put(featuredStory, new Callable() {
            public void callBackMethod(ArrayList<Object> objectList, Object obj) {
                if (Preferences.getBool(Prefs.DISCOVER_UI)) {
                    Logger.log("Blocking Featured Story");
                    objectList.remove(obj);
                }
            }
        });
    }

    interface Callable {
        void callBackMethod(ArrayList<Object> objectList, Object obj);
    }
}
