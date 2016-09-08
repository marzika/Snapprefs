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

import com.marz.snapprefs.Util.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by MARZ on 2016. 04. 12..
 */
public class Stories {
    public static List<String> peopleToHide = new ArrayList<>();

    public static List<Friend> friendList = new ArrayList<>();

    static void initStories(final XC_LoadPackage.LoadPackageParam lpparam) {
        readBlockedList();
        findAndHookMethod("com.snapchat.android.fragments.stories.StoriesFragment", lpparam.classLoader, "D", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ArrayList f = (ArrayList) XposedHelpers.getObjectField(param.thisObject, Obfuscator.stories.STORYLIST);
                List<Class> types = new ArrayList<Class>();
                Class<?> recentStory = XposedHelpers.findClass(Obfuscator.stories.RECENTSTORY_CLASS, lpparam.classLoader);
                Class<?> allStory = XposedHelpers.findClass(Obfuscator.stories.ALLSTORY_CLASS, lpparam.classLoader);
                Class<?> liveStory = XposedHelpers.findClass(Obfuscator.stories.LIVESTORY_CLASS, lpparam.classLoader);
                Class<?> discoverStory = XposedHelpers.findClass(Obfuscator.stories.DISCOVERSTORY_CLASS, lpparam.classLoader);
                types.add(recentStory);
                types.add(allStory);
                types.add(liveStory);
                types.add(discoverStory);

                for (int i = f.size() - 1; i >= 0; i--) {
                    Object o = f.get(i);
                    if (o.getClass() == recentStory && Preferences.mHidePeople) {
                        String username = (String) XposedHelpers.callMethod(o, "b");
                        for (String person : peopleToHide) {
                            if (username.equals(person)) {
                                Logger.log("removing from recents" + username);
                                f.remove(i);
                            }
                        }
                    } else if (o.getClass() == allStory && Preferences.mHidePeople) {
                        Object friend = XposedHelpers.callMethod(o, "h");
                        String username = (String) XposedHelpers.callMethod(friend, "g");
                        for (String person : peopleToHide) {
                            if (username.equals(person)) {
                                Logger.log("removing " + username);
                                f.remove(i);
                            }
                        }
                    } else if (o.getClass() == liveStory && Preferences.mHideLive) {
                        f.remove(i);
                    } else if (o.getClass() == discoverStory && Preferences.mDiscoverUI) {
                        f.remove(i);
                    } else if (!types.contains(o.getClass())){
                        Logger.log("Found an unexpected entry at stories TYPE: " + o.getClass().getCanonicalName());
                    }
                }
                XposedHelpers.setObjectField(param.thisObject, Obfuscator.stories.STORYLIST, f);
            }
        });
    }

    private static void readFriendList(ClassLoader classLoader) {
        final Object friendManager = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.snapchat.android.model.FriendManager", classLoader), "e");
        List friends = (List) XposedHelpers.getObjectField(XposedHelpers.getObjectField(friendManager, "mOutgoingFriendsListMap"), "mList");
        friendList.clear();
        for (int i = 0; i <= friends.size() - 1; i++) {
            String username = (String) XposedHelpers.callMethod(friends.get(i), "g");
            if (peopleToHide.contains(username)) {
                friendList.add(new Friend(username, true));
            } else {
                friendList.add(new Friend(username, false));
            }
        }
        Collections.sort(friendList, new Friend.friendComparator());
    }

    private static void readBlockedList() {
        String read = FileUtils.readFromSDFolder("blockedstories").replaceAll("\n", "");
        if (!read.equals("0")) {
            peopleToHide = Arrays.asList(read.split(";"));
        }
    }

    public static void addSnapprefsBtn(XC_InitPackageResources.InitPackageResourcesParam resparam, final XModuleResources mResources) {
        try {
            resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "stories", new XC_LayoutInflated() {
                public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                    final FrameLayout relativeLayout = (FrameLayout) liparam.view.findViewById(liparam.res.getIdentifier("top_panel", "id", Common.PACKAGE_SNAP));
                    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("myfriends_action_bar_search_button", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                    final FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
                    //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    layoutParams2.topMargin = HookMethods.px(8.0f);
                    layoutParams2.rightMargin = HookMethods.px(115.0f);
                    final ImageView spbtn = new ImageView(HookMethods.SnapContext);
                    spbtn.setImageDrawable(mResources.getDrawable(R.drawable.story_filter));
                    spbtn.setScaleX(0.75f);
                    spbtn.setScaleY(0.75f);
                    Logger.log("Adding Snapprefs button to story section");
                    spbtn.setOnClickListener(new View.OnClickListener() {
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
                            relativeLayout.addView(spbtn, layoutParams2);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
