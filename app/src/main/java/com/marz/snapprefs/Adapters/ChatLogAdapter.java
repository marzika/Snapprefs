package com.marz.snapprefs.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.ChatData;

import java.util.List;

/**
 * Used to reduce calls to the database by storing contents in a hashmap
 * Created by Andre on 26/10/2016.
 */

public class ChatLogAdapter extends ArrayAdapter<Object> {
    public ChatLogAdapter(Context context, int resource, List<Object> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.chatmessage_item, null);
        }

        ChatData item = (ChatData) getItem(position);

        if( item != null ) {
            TextView txt_friend_view = (TextView) v.findViewById(R.id.text_list_log_friend);
            TextView txt_message_count = (TextView) v.findViewById(R.id.text_list_message);
            TextView txt_date = (TextView) v.findViewById(R.id.text_log_date);

            txt_friend_view.setText(item.getSender());
            txt_message_count.setText(item.getText());
            txt_date.setText(item.getFormattedDate());
        }

        return v;
    }
}
