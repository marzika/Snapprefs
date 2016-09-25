package com.marz.snapprefs.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.marz.snapprefs.R;

import java.util.ArrayList;

/**
 * Created by Andre on 15/09/2016.
 */
public class ChatLogsFragment extends Fragment {
    private ArrayAdapter adapter;
    private View mainView;
    static ArrayList<String> usernameList = new ArrayList<String>(){{
            add("Hello"); add("Hello2");
        }};

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.chatlogs_main, null);
        ListView logList = (ListView) mainView.findViewById(R.id.logListView);

        adapter=new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_1,
                usernameList);

        logList.setAdapter( adapter );

        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {

            }
        });

        return mainView;
    }

    public void addItem( String item )
    {
        Log.d("[SNAPPREFS]", "Added item: " + item);
        usernameList.add(item);
        adapter.notifyDataSetChanged();
    }

    public void removeItem( String item)
    {
        usernameList.remove(item);
        adapter.notifyDataSetChanged();
    }
}