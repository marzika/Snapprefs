package com.marz.snapprefs.Adapters;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Adapters.LensListAdapter.ViewHolder;
import com.marz.snapprefs.Fragments.LensesFragment;
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

public class LensListAdapter extends RecyclerView.Adapter<ViewHolder> implements OnLongClickListener {
    private Context context;
    private LensesFragment lensesFragment;
    public ArrayList<LensItemData> lensDataList;

    public LensListAdapter(Context context, ArrayList<LensItemData> lensDataList, LensesFragment lensesFragment) {
        this.context = context;
        this.lensDataList = lensDataList;
        this.lensesFragment = lensesFragment;
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
        holder.itemView.setTag(lensData);
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
        holder.itemView.setOnLongClickListener(this);
        holder.lensText.setText(lensData.lensName);
        holder.backgroundLayout.setBackgroundResource(lensData.isActive ? R.drawable.lens_bg_selected : R.drawable.lens_bg_unselected);

        AsyncTaskCompat.executeParallel(new LensIconLoader.AsyncLensIconDownloader(),
                 lensData, holder.itemView.getContext(), holder.lensIcon);

    }

    @Override
    public int getItemCount() {
        return lensDataList.size();
    }

    @Override
    public boolean onLongClick(final View view) {
        final LensItemData lensItemData = (LensItemData) view.getTag();
        AlertDialog.Builder alertBuilder = new Builder(view.getContext());
        alertBuilder.setTitle("Lens Deletion Confirmation");
        alertBuilder.setMessage("Are you absolutely sure you want to delete lens " + lensItemData.lensName + "?");
        alertBuilder.setNegativeButton("NO", null);
        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if( Lens.getLensDatabase(view.getContext()).deleteLens(lensItemData.lensCode) )
                    Toast.makeText(view.getContext(), "Successfully removed lens", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(view.getContext(), "Problem removing lens", Toast.LENGTH_SHORT).show();

                int position = lensDataList.indexOf(lensItemData);

                if( position == -1 || !lensDataList.remove(lensItemData))
                    return;

                notifyItemRemoved(position);
                lensesFragment.refreshLensCount();
            }
        });

        alertBuilder.create().show();
        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView lensIcon;
        TextView lensText;
        View backgroundLayout;

        ViewHolder(View itemView) {
            super(itemView);

            this.lensIcon = (ImageView) itemView.findViewById(R.id.lensIconView);
            this.lensText = (TextView) itemView.findViewById(R.id.lensTextView);
            this.backgroundLayout = itemView.findViewById(R.id.lens_background_layout);
            itemView.setTag(this.backgroundLayout);
        }
    }
}
