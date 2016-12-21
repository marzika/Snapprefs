package com.marz.snapprefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.marz.snapprefs.Groups.Group;
import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Adapters.GroupDataAdapter;

import java.io.File;
import java.util.List;

/**
 * Created by MARZ on 2016. 04. 19..
 */
public class GroupDialogList extends DialogFragment {
    public static RecyclerView.Adapter mAdapter;
    public static int friend_item;
    public static String name;
    public static int checkBox;
    public static Group group;
    private static RecyclerView mRecyclerView;
    public List<Friend> friendList;
    private RecyclerView.LayoutManager mLayoutManager;

    static GroupDialogList newInstance(String groupName) {
        name = groupName;
        return new GroupDialogList();
    }

    static void setGroup(Group selectedGroup) {
        group = selectedGroup;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout v = new LinearLayout(HookMethods.SnapContext);
        v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(linearParams);

        final EditText eText = new EditText(HookMethods.SnapContext);
        eText.setText(name);
        eText.setHint("Type in your Group's name");
        eText.setSingleLine();
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Select people")
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Groups.sendStoriesUpdateEvent();
                                onCancel(dialog);
                            }
                        }
                );
        mRecyclerView = new RecyclerView(HookMethods.SnapContext);

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int px = HookMethods.px(5);
        params.setMargins(px, px, px, px);

        mRecyclerView.setLayoutParams(params);
        v.addView(eText);

        v.addView(mRecyclerView, params);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(HookMethods.SnapContext));

        // create an Object for Adapter
        Groups.readFriendList(HookMethods.classLoader, group);
        GroupDataAdapter.setFriendList(Groups.friendList);
        mAdapter = new GroupDataAdapter(HookMethods.SnapContext);

        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.invalidate();

        eText.setVisibility(View.VISIBLE);
        alert.setView(v);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (eText.length() != 0) {
                            FileUtils.deleteSDFile(new File(Groups.groupsDir + "/" + name));
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(HookMethods.SnapContext, "Name is empty", Toast.LENGTH_SHORT).show();
                        }
                        Groups.readGroups();
                    }
                });
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (eText.length() != 0) {
                            FileUtils.deleteSDFile(new File(Groups.groupsDir + "/" + name));
                            friendList = GroupDataAdapter.getFriendList();
                            String selected = eText.getText().toString() + ";";
                            int numSelected = 0;
                            for (Friend friend : friendList) {
                                if (friend.isSelected()) {
                                    numSelected++;
                                    selected = selected + friend.getName() + ";";
                                }
                            }
                            if (numSelected != 0) {
                                File toWrite = new File(Groups.groupsDir + "/" + eText.getText().toString());
                                FileUtils.writeToSDFile(selected, toWrite);
                                Groups.sendStoriesUpdateEvent();
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(HookMethods.SnapContext, "Select at least one user", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(HookMethods.SnapContext, "Name is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }
}
