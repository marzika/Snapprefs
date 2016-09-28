package com.marz.snapprefs.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marz.snapprefs.R;

/**
 * Created by MARZ on 2016. 02. 11..
 */
public class BaseFragment extends Fragment {
    private View view;
    private int layoutId;
    private int fragmentId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        try {
            view = inflater.inflate(R.layout.tab_fragment_layout, null);//cant we just pass TimerSettings in GeneralTabFragment?
        } catch (InflateException ignored) {
            ignored.printStackTrace();
        }
        return view;
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        Fragment f = getActivity().getSupportFragmentManager().findFragmentById(fragmentId);
//        FragmentTransaction tr = getActivity().getSupportFragmentManager().beginTransaction();
//        if (f != null) {
//            tr.remove(f);
//            tr.commit();
//        }
//    }

    public BaseFragment setLayoutId(int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public BaseFragment setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
        return this;
    }

}
