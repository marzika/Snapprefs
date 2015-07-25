package com.marz.snapprefs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getParameterTypes;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;

public class HookSendList {

    static void initSelectAll(final LoadPackageParam lpparam) {
        HookMethods.refreshPreferences();
        /**
         * This method gets called when the SendTo screen is shown. We hook it to display our checkbox.
         */
        findAndHookMethod(Obfuscator.select.SENDTOFRAGMENT_CLASS, lpparam.classLoader, "onActivityCreated", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                CheckBox selectAll;
                try {
                    View title = (View) getObjectField(param.thisObject, Obfuscator.select.SENDTOFRAGMENT_VAR_TOPVIEW);
                    Context c = (Context) callMethod(param.thisObject, "getActivity");
                    selectAll = getCheckbox(c);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    ((RelativeLayout) title.getParent().getParent()).addView(selectAll, params);
                    setAdditionalInstanceField(param.thisObject, Common.select_name, selectAll);
                } catch (Throwable t) {
                    Logger.log("Checkbox init. failed", true);
                    Logger.log(t.toString());
                    return;
                }


                selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean set) {
                        Object hopefullySendToAdapter = getObjectField(param.thisObject, "e");
                        Logger.log("SELECTALL: We have the ArrayAdapter", true);
                        final String adaptersType = getParameterTypes(new Object[]{hopefullySendToAdapter})[0].getCanonicalName();
                        final boolean isSendToAdapter = adaptersType.equals(Obfuscator.select.SENDTOADAPTER_CLASS);

                        if (hopefullySendToAdapter != null && isSendToAdapter) {
                            Logger.log("SELECTALL: AA isn't null and is an AA", true);
                            Object aa = hopefullySendToAdapter;
                            ArrayList friendList;
                            Set FriendSet;
                            List StoryList;

                            try {
                                Logger.log("SELECTALL: We are trying", true);
                                friendList = (ArrayList) getObjectField(aa, Obfuscator.select.SENDTOADAPTER_VAR_LIST); // e or f, was c, in SendToAdapter
                                FriendSet = (Set) getObjectField(param.thisObject, Obfuscator.select.SENDTOFRAGMENT_VAR_SET); //in SendToFragment
                                StoryList = (List) getObjectField(param.thisObject, Obfuscator.select.SENDTOFRAGMENT_VAR_ARRAYLIST);
                                Class<?>[] types = getParameterTypes(friendList.toArray());
                                for (int i = 0; i < types.length; i++) {
                                    Object thingToAdd = friendList.get(i);
                                    if (types[i].getCanonicalName().equals(Obfuscator.select.FRIEND_CLASS)) {
                                        if (set)
                                            FriendSet.add(thingToAdd);
                                        else
                                            FriendSet.remove(thingToAdd);
                                    } else if (types[i].getCanonicalName().equals(Obfuscator.select.POSTTOSTORY_CLASS) && HookMethods.selectStory == true) {
                                        if (set)
                                            StoryList.add(thingToAdd);
                                        else
                                            StoryList.remove(thingToAdd);
                                    } else {
                                        Logger.log("SnapPrefs: Unknown type value at: " + types[i].toString());
                                    }
                                }
                                callMethod(param.thisObject, Obfuscator.select.SENDTOFRAGMENT_ADDTOLIST);
                            } catch (Throwable t) {
                                Logger.log("Your Snapchat is outdated, update it.", true);
                                Logger.log(t.toString());
                            }

                        }
                    }
                });
            }
        });
        //TODO: Hide the Checkbox when the searchbar is opened
            /*
            findAndHookMethod(Common.Class_SendToFragment, lpparam.classLoader, Common.Method_Visible, "com.snapchat.android.fragments.sendto.SendToFragment", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	                View v = (View) getAdditionalInstanceField(param.thisObject, Common.select_name);
	                v.setVisibility(View.VISIBLE);
	            }
	        });

	        findAndHookMethod(Common.Class_SendToFragment, lpparam.classLoader, Common.Method_Invisible, "com.snapchat.android.fragments.sendto.SendToFragment", new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	                View v = (View) getAdditionalInstanceField(param.thisObject, Common.select_name);
	                v.setVisibility(View.INVISIBLE);
	            }
	        });*/
    }

    /**
     * Opens SnapChat's Resources and gets the pretty checkbox, for reuse & consistent appearance
     *
     * @param c SNAPCHAT's context
     * @return A pretty checkbox (hopefully)
     */
    public static CheckBox getCheckbox(Context c) {
        CheckBox cb = new CheckBox(c);
        try {
            //Setting properties from snapchat's res/layout/send_to_item.xml checkbox
            cb.setButtonDrawable(c.getResources().getIdentifier("send_to_button_selector", "drawable", "com.snapchat.android"));
            //May need to scale drawable bitmap...
            cb.setScaleX(0.7F);
            cb.setScaleY(0.7F);
        } catch (Exception e) {
            HookMethods.logging("Snapprefs: Error getting Checkbox");
        }
        return cb;
    }
}
