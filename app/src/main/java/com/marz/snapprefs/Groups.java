package com.marz.snapprefs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Util.NotificationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.newInstance;

public class Groups {

    public static final ArrayList<Group> groups = new ArrayList<>();
    public static List<Friend> friendList = new ArrayList<>();
    static boolean doneOnce = false;
    static File groupsDir = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Groups");

    public static void initGroups(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        readGroups();
        final Class<?> Friend = findClass(Obfuscator.select.FRIEND_CLASS, lpparam.classLoader);
        final Class<?> Ly = findClass(Obfuscator.groups.STORY_CLASS, lpparam.classLoader);

        XposedHelpers.findAndHookMethod(Obfuscator.groups.STORYARRAY_CLASS, lpparam.classLoader, Obfuscator.groups.STORYARRAY_METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                readGroups();
                ArrayList result = (ArrayList) param.getResult();
                ArrayList<Object> newResult = new ArrayList<>();
                Object edit = XposedHelpers.newInstance(Ly, "edit", "Edit groups");
                XposedHelpers.setAdditionalInstanceField(edit, "editGroups", true);
                if (!result.contains(edit))
                    newResult.add(edit);

                synchronized (groups) {
                    for (Group g : groups) {
                        Object group = XposedHelpers.newInstance(Ly, "group_" + g.name, g.name);
                        XposedHelpers.setAdditionalInstanceField(group, "group", g);
                        if (!result.contains(group))
                            newResult.add(group);
                    }
                }
                newResult.addAll(result);
                param.setResult(newResult);
            }
        });

        XposedHelpers.findAndHookMethod(Obfuscator.groups.STORYSECTION_CLASS, lpparam.classLoader, "onBindViewHolder", findClass("android.support.v7.widget.RecyclerView$u", lpparam.classLoader), int.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                Object element = callMethod(XposedHelpers.getObjectField(param.thisObject, "c"), "get", param.args[1]);
                if (XposedHelpers.getAdditionalInstanceField(element, "editGroups") != null) {
                    CheckBox k = (CheckBox) XposedHelpers.getObjectField(param.args[0], "a");
                    k.setVisibility(View.GONE);
                    k.setOnCheckedChangeListener(null);
                    k.setOnClickListener(null);
                    ((View) XposedHelpers.getObjectField(param.args[0], "itemView")).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentTransaction ft = HookMethods.SnapContext.getFragmentManager().beginTransaction();
                            //ft.setCustomAnimations(R.anim.fade, R.anim.fade); #85(?)
                            Fragment prev = HookMethods.SnapContext.getFragmentManager().findFragmentByTag("dialog");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);

                            // Create and show the dialog.
                            DialogFragment newFragment = GroupDialog.newInstance();
                            newFragment.show(ft, "dialog");
                        }
                    });
                }
                final Group group = (Group) XposedHelpers.getAdditionalInstanceField(element, "group");
                if (group == null) return;
                HashMap<String, Boolean> checks = (HashMap<String, Boolean>) XposedHelpers.getAdditionalInstanceField(param.thisObject, "checks");
                if (checks == null) {
                    checks = new HashMap<>();
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "checks", checks);
                }
                //Here change this color if you want
//                ((View) XposedHelpers.getObjectField(param.args[0], "a")).setBackgroundColor(0xFF66FA77);
                final CheckBox check = (CheckBox) XposedHelpers.getObjectField(param.args[0], "a");
                if (!checks.containsKey(group.name)) {
                    checks.put(group.name, false);
                } else {
                    check.setOnCheckedChangeListener(null);
                    check.setChecked(checks.get(group.name));
                }
                final HashMap<String, Boolean> finalChecks = checks;
                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        finalChecks.put(group.name, isChecked);
                        List f = (List) XposedHelpers.getObjectField(param.thisObject, "c");
                        for (String user : group.users) {
                            for (Object ii : f) {
                                if (Friend.isInstance(ii) && ((String) XposedHelpers.getObjectField(ii, "mUsername")).equalsIgnoreCase(user)) {
                                    callMethod(XposedHelpers.getObjectField(param.thisObject, "h"), "a", new Class[]{int.class, findClass(Obfuscator.groups.INTERFACE, lpparam.classLoader), boolean.class}, callMethod(param.args[0], "getAdapterPosition"), ii, isChecked);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    // Updated method & content 9.39.5
    public static void readFriendList(final ClassLoader classLoader,final Group selectedGroup) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Object friendManager = callStaticMethod(findClass("com.snapchat.android.model.FriendManager", classLoader), Obfuscator.groups.GETFRIENDMANAGER_METHOD);
                List friends = (List) XposedHelpers.getObjectField(XposedHelpers.getObjectField(friendManager, "mOutgoingFriendsListMap"), "mList");
                friendList.clear();
                for (int i = 0; i <= friends.size() - 1; i++) {
                    String username = (String) callMethod(friends.get(i), Obfuscator.groups.GETUSERNAME_METHOD);
                    String displayName = (String) callMethod(friends.get(i), Obfuscator.groups.GETDISPLAYNAME_METHOD);
                    if (selectedGroup != null && selectedGroup.users.contains(username)) {
                        friendList.add(new Friend(username, displayName, true));
                    } else {
                        friendList.add(new Friend(username, displayName, false));
                    }
                }
                Collections.sort(friendList, new Friend.friendComparator());
            }
        }).start();
    }

    public static void readGroups() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File groupFolder = groupsDir;
                if (!groupFolder.exists()) {
                    groupFolder.mkdir();
                }
                File[] groupFiles = groupFolder.listFiles();
                if (groupFiles == null || groupFiles.length == 0) {
                    groups.clear();
                    return;
                }
                groups.clear();
                int numGroups = 0;
                for (File group : groupFiles) {
                    String data = FileUtils.readFromSD(group);
                    if (!data.equalsIgnoreCase("0")) {
                        String[] groupData = data.split(";");
                        String name = groupData[0];
                        ArrayList<String> users = new ArrayList<>(Arrays.asList(groupData));
                        users.remove(0);
                        Group currentGroup = new Group(name, users);
                        if (numGroups == 3 && Preferences.getLicence() == 0) {
                            NotificationUtils.showMessage("You cannot have more than 3 groups as a free user", Color.RED, NotificationUtils.LENGTH_SHORT, HookMethods.classLoader);
                            return;
                        }
                        if (Preferences.getLicence() != 0 && !Preferences.getBool(Preferences.Prefs.UNLIM_GROUPS) && numGroups > 3) {
                            NotificationUtils.showMessage("You disabled the option to have more than 3 groups", Color.RED, NotificationUtils.LENGTH_SHORT, HookMethods.classLoader);
                            return;
                        }
                        if (!groups.add(currentGroup))
                            numGroups++; //add limit for Free users
                    }
                }

                Collections.sort(groups, new Groups.groupComparator());
            }
        }).start();
    }

    public static void sendStoriesUpdateEvent() {
        Object updateEvent = newInstance(findClass(Obfuscator.bus.UPDATEEVENT_CLASS, HookMethods.classLoader));
        Object bus = callStaticMethod(findClass(Obfuscator.bus.GETBUS_CLASS, HookMethods.classLoader), Obfuscator.bus.GETBUS_METHOD);
        callMethod(bus, Obfuscator.bus.BUS_POST, updateEvent);
    }

    static class Group {
        public String name;
        public ArrayList<String> users = new ArrayList<>();

        public Group(String name, String... users) {
            this.name = name;
            Collections.addAll(this.users, users);
        }

        public Group(String name, ArrayList<String> users) {
            this.name = name;
            this.users = users;
        }

        public String getName() {
            return this.name;
        }

    }

    public static class groupComparator implements Comparator<Group> {
        @Override
        public int compare(Group o1, Group o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
