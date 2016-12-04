package com.marz.snapprefs.Adapters;

import android.content.Context;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marz.snapprefs.Adapters.LensListAdapter.ViewHolder;
import com.marz.snapprefs.Fragments.LensesFragment.LensItemData;
import com.marz.snapprefs.Lens;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.ArrayList;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class LensListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    public ArrayList<LensItemData> lensDataList;

    public LensListAdapter(Context context, ArrayList<LensItemData> lensDataList ) {
        this.context = context;
        this.lensDataList = lensDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lensholder_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LensItemData lensData = lensDataList.get(position);

        if( lensData == null ) {
            Logger.log("Error pulling lens data", LogType.LENS);
            return;
        }

        //holder.lensIcon.setImageBitmap(lensData.lensIcon);
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    boolean activeState = Lens.getLensDatabase(context).toggleLensActiveState(lensData.lensCode);
                    View lensBackgroundLayout = (View) view.getTag();

                    if(lensBackgroundLayout == null)
                        return;

                    lensBackgroundLayout.setBackgroundResource(activeState ? R.drawable.lens_bg_selected : R.drawable.lens_bg_unselected);
                    lensData.isActive = activeState;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.lensText.setText(lensData.lensName);
        holder.backgroundLayout.setBackgroundResource(lensData.isActive ? R.drawable.lens_bg_selected : R.drawable.lens_bg_unselected);

        AsyncTaskCompat.executeParallel(new LensIconLoader.AsyncLensIconDownloader(),
                 lensData, holder.itemView.getContext(), holder.lensIcon);

    }

    @Override
    public int getItemCount() {
        return lensDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView lensIcon;
        TextView lensText;
        View backgroundLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            this.lensIcon = (ImageView) itemView.findViewById(R.id.lensIconView);
            this.lensText = (TextView) itemView.findViewById(R.id.lensTextView);
            this.backgroundLayout = itemView.findViewById(R.id.lens_background_layout);
            itemView.setTag(this.backgroundLayout);
        }
    }
}
