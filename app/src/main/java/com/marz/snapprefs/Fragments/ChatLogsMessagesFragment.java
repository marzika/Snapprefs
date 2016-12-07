package com.marz.snapprefs.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.marz.snapprefs.Chat;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.ChatData;
import com.marz.snapprefs.Adapters.ChatLogAdapter;
import com.marz.snapprefs.Util.ConversationItem;

/**
 * Used to reduce calls to the database by storing contents in a hashmap
 * Created by Andre on 26/10/2016.
 * Modified by ethan on 29/10/2016
 */

@SuppressLint("ValidFragment")
public class ChatLogsMessagesFragment extends Fragment implements OnFocusChangeListener {
    private ConversationItem conversation;
    private ChatLogAdapter adapter;
    private ListView logList;

    public ChatLogsMessagesFragment(ConversationItem item) {
        this.conversation = item;
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.chatlogs_messages, container, false);
        logList = (ListView) mainView.findViewById(R.id.list_message_logs);

        logList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final ChatData item = (ChatData) logList.getItemAtPosition(i);
                String message = item.getText();
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text label", message);
                clipboard.setPrimaryClip(clip);
                Logger.log("Copied chat message to clipboard", LogType.CHAT);
                Toast.makeText(getContext(), "Message copied", Toast.LENGTH_SHORT).show();
            }
        });

        logList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Logger.log("Long clicked");
                final ChatData item = (ChatData) logList.getItemAtPosition(i);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Message");
                builder.setMessage("Are you sure you would like to delete this message?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if( !Chat.getChatDBHelper(getContext()).removeChat(item.getMessageId()))
                            Toast.makeText(getContext(), "Couldn't delete message!", Toast.LENGTH_SHORT).show();
                        else
                            updateMessageList();
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
    public void onFocusChange(View v, boolean focus) {
        Logger.log("Focus changed! " + focus, LogType.CHAT);

        if (focus)
            updateMessageList();
    }

    @Override
    public void onResume() {
        super.onResume();

        Logger.log("Resuming conversation fragment", LogType.CHAT);
        updateMessageList();
    }

    private void updateMessageList() {
        conversation.messageList = Chat.getChatDBHelper(getContext()).getAllChatsFrom(conversation.conversationId);

        if (conversation.messageList == null) {
            Toast.makeText(getContext(), "No ChatLogs to display!", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new ChatLogAdapter(getContext(), R.layout.chatmessage_item, conversation.messageList);
        logList.setAdapter(adapter);

        Logger.log("Updated message list contents", LogType.CHAT);
    }
}
