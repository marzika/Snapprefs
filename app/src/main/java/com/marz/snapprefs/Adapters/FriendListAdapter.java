package com.marz.snapprefs.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.ConversationItem;

import java.util.List;

/**
 * Used to reduce calls to the database by storing contents in a hashmap
 * Created by Andre on 26/10/2016.
 */

public class FriendListAdapter extends ArrayAdapter<Object> {

    public FriendListAdapter(Context context, int resource, List<Object> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.chatfriend_item, null);
        }

        ConversationItem item = (ConversationItem) getItem(position);

        if( item != null ) {
            TextView txt_friend_view = (TextView) v.findViewById(R.id.text_list_friend);
            TextView txt_message_count = (TextView) v.findViewById(R.id.txt_message_count);

            txt_friend_view.setText(item.friendName);
            txt_message_count.setText("" + item.messageList.size());
        }

        return v;
    }
}
