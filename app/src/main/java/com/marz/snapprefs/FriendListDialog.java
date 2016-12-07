package com.marz.snapprefs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.marz.snapprefs.Util.FileUtils;
import com.marz.snapprefs.Adapters.ViewDataAdapter;

import java.util.List;

/**
 * Created by MARZ on 2016. 04. 14..
 */
public class FriendListDialog extends DialogFragment {
    public static RecyclerView.Adapter mAdapter;
    public static int friend_item;
    public static int name;
    public static int checkBox;
    private static RecyclerView mRecyclerView;
    public List<Friend> friendList;
    private RecyclerView.LayoutManager mLayoutManager;

    static FriendListDialog newInstance() {
        return new FriendListDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Select blocked People from Stories")
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                friendList = ViewDataAdapter.getFriendList();
                                String selected = "";
                                for (Friend friend : friendList) {
                                    if (friend.isSelected()) {
                                        selected = selected + friend.getName() + ";";
                                    }
                                }
                                Toast.makeText(HookMethods.SnapContext, "Restart the app to see changes", Toast.LENGTH_SHORT).show();
                                FileUtils.writeToSDFolder(selected, "blockedstories");
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                onCancel(dialog);
                            }
                        }
                );
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout v = new LinearLayout(HookMethods.SnapContext);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(linearParams);

        mRecyclerView = new RecyclerView(HookMethods.SnapContext);

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int px = HookMethods.px(5);
        params.setMargins(px, px, px, px);

        mRecyclerView.setLayoutParams(params);

        v.addView(mRecyclerView, params);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(HookMethods.SnapContext));

        // create an Object for Adapter
        mAdapter = new ViewDataAdapter(Stories.friendList, HookMethods.SnapContext);

        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);
        alert.setView(v);
        return alert.create();
    }
}
