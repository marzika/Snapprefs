package com.marz.snapprefs.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.marz.snapprefs.Chat;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.MainActivity;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.ConversationItem;
import com.marz.snapprefs.Adapters.FriendListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andre on 15/09/2016.
 */
public class ConversationLogsFragment extends Fragment implements OnFocusChangeListener{
    static List<Object> conversationItemList = new ArrayList<>();
    private ArrayAdapter adapter;
    private View mainView;
    private ListView logList;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.chatlogs_main, container, false);
        logList = (ListView) mainView.findViewById(R.id.logListView);

        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ConversationItem conversationItem = (ConversationItem) adapterView.getItemAtPosition(i);
                Fragment newFragment = new ChatLogsMessagesFragment(conversationItem);

                FragmentTransaction transaction = MainActivity.mFragmentManager.beginTransaction();

                transaction.replace(R.id.containerView, newFragment);
                transaction.addToBackStack("ConversationLogsFragment");
                transaction.commit();
            }
        });

        logList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Logger.log("Long clicked");
                final ConversationItem item = (ConversationItem) logList.getItemAtPosition(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Message");
                builder.setMessage("Are you sure you would like to delete this message?");
                builder.setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("CONFIRM");
                        builder.setMessage("This will remove every message in this conversation. You cannot undo this!\nAre you sure?");
                        builder.setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if( !Chat.getChatDBHelper(getContext()).removeConversation(item.conversationId))
                                    Toast.makeText(getContext(), "Couldn't delete message!", Toast.LENGTH_SHORT).show();
                                else
                                    updateConversationList();
                            }
                        });
                        builder.setNegativeButton("No", null);
                        builder.show();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

                return false;
            }
        });

        return mainView;
    }

    @Override
    public void onFocusChange(View v, boolean b) {
        Logger.log("Focus changed!" + b, LogType.CHAT);

        if(b)
            updateConversationList();
    }

    @Override
    public void onResume() {
        super.onResume();

        Logger.log("Resuming conversation fragment", LogType.CHAT);
        updateConversationList();
    }

    private void updateConversationList() {
        conversationItemList = Chat.getChatDBHelper(getContext()).getAllConversations();

        if (conversationItemList == null) {
            Toast.makeText(getContext(), "No ChatLogs to display!", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new FriendListAdapter(getContext(), R.layout.chatfriend_item, conversationItemList);
        logList.setAdapter(adapter);

        Logger.log("Updated conversation list", LogType.CHAT);
    }
}