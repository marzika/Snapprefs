package com.marz.snapprefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Util.NotificationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MARZ on 2016. 04. 14..
 */
public class GroupDialog extends DialogFragment {
    public static RecyclerView.Adapter mAdapter;
    public static int name;
    public static int group_item;
    private static RecyclerView mRecyclerView;
    public List<Friend> friendList;
    private RecyclerView.LayoutManager mLayoutManager;

    static GroupDialog newInstance() {
        return new GroupDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Click on a Group to edit it")
                .setPositiveButton("Done",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(HookMethods.SnapContext, "Reopen the SendTo screen to see the changes", Toast.LENGTH_LONG).show();
                                onCancel(dialog);
                                Groups.sendStoriesUpdateEvent();
                            }
                        }
                );
        LinearLayout.LayoutParams linearparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout v = new LinearLayout(HookMethods.SnapContext);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(linearparams);

        final File[] files = Groups.groupsDir.listFiles();
        Arrays.sort(files);
        final int N = Groups.groups.size(); // total number of textviews to add
        for (int i = 0; i < N; i++) {
            // create a new textview
            final TextView rowTextView = new TextView(HookMethods.context);

            // set some properties of rowTextView or something
            rowTextView.setText(files[i].getName());
            rowTextView.setPadding(HookMethods.px(5), HookMethods.px(5), HookMethods.px(5), HookMethods.px(5));
            rowTextView.setTextSize(20);
            rowTextView.setTextColor(Color.BLACK);

            // add the textview to the linearlayout
            v.addView(rowTextView);
            rowTextView.setId(i);
            final int id_ = rowTextView.getId();
            rowTextView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    FragmentTransaction ft = HookMethods.SnapContext.getFragmentManager().beginTransaction();
                    Fragment prev = HookMethods.SnapContext.getFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    DialogFragment newFragment = GroupDialogList.newInstance((String) rowTextView.getText());
                    Groups.readGroups();
                    GroupDialogList.setGroup(Groups.groups.get(id_));
                    newFragment.show(ft, "dialog");
                    Groups.sendStoriesUpdateEvent();
                }
            });
        }
        Button add = new Button(HookMethods.SnapContext);
        add.setText("Add new Group");
        add.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        boolean shouldShowAdd = true;
        if (Groups.groups.size() == 3 && Preferences.mLicense == 0) {
            shouldShowAdd = false;
        }
        if (Preferences.mLicense != 0 && Preferences.mUnlimGroups == false) {
            shouldShowAdd = false;
        }
        if(shouldShowAdd){
            v.addView(add);
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = HookMethods.SnapContext.getFragmentManager().beginTransaction();
                //ft.setCustomAnimations(R.anim.fade, R.anim.fade); #85(?)
                Fragment prev = HookMethods.SnapContext.getFragmentManager().findFragmentByTag("dialog_group");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = GroupDialogList.newInstance("");
                GroupDialogList.setGroup(null);
                newFragment.show(ft, "dialog_group");
            }
        });
        alert.setView(v);
        return alert.create();
    }
}
