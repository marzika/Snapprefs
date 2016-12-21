package com.marz.snapprefs.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marz.snapprefs.Friend;
import com.marz.snapprefs.GroupDialog;
import com.marz.snapprefs.R;

import java.util.List;

/**
 * Created by MARZ on 2016. 04. 14..
 */
public class GroupDataAdapter extends RecyclerView.Adapter<GroupDataAdapter.ViewHolder> {

    private static List<Friend> friendList;
    public Context context;
    public LayoutInflater inflater = null;

    public GroupDataAdapter(Context snapContext) {
        this.context = snapContext;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // method to access in activity after updating selection
    public static List<Friend> getFriendList() {
        return friendList;
    }

    public static void setFriendList(List<Friend> friends) {
        friendList = friends;
    }

    // Create new views
    @Override
    public GroupDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        int group_item = (int) GroupDialog.group_item;
        View itemLayoutView = inflater.inflate(group_item, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {

        final int pos = position;
        viewHolder.tvName = (TextView) viewHolder.itemView.getChildAt(0);
        viewHolder.chkSelected = (CheckBox) viewHolder.itemView.getChildAt(1);

        viewHolder.tvName.setText(friendList.get(position).getDisplayName());
        viewHolder.tvName.setHint(friendList.get(position).getName());

        viewHolder.chkSelected.setChecked(friendList.get(position).isSelected());

        viewHolder.chkSelected.setTag(friendList.get(position));


        viewHolder.chkSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Friend contact = (Friend) cb.getTag();

                contact.setSelected(cb.isChecked());
                friendList.get(pos).setSelected(cb.isChecked());
            }
        });

    }

    // Return the size arraylist
    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;

        public CheckBox chkSelected;

        public RelativeLayout itemView;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            itemView = (RelativeLayout) itemLayoutView;
            tvName = (TextView) itemView.findViewById(R.id.name);
            chkSelected = (CheckBox) itemView.findViewById(R.id.checkBox);
        }

    }
}
