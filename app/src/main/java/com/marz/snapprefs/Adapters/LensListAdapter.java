package com.marz.snapprefs.Adapters;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Adapters.LensListAdapter.ViewHolder;
import com.marz.snapprefs.Databases.LensDatabaseHelper.LensEntry;
import com.marz.snapprefs.Fragments.LensesFragment;
import com.marz.snapprefs.Fragments.LensesFragment.LensItemData;
import com.marz.snapprefs.Lens;
import com.marz.snapprefs.Logger;
import com.marz.snapprefs.Logger.LogType;
import com.marz.snapprefs.R;
import com.marz.snapprefs.Util.BitmapCache;
import com.marz.snapprefs.Util.LensIconLoader;

import java.util.ArrayList;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class LensListAdapter extends RecyclerView.Adapter<ViewHolder> {
    public ArrayList<LensItemData> lensDataList;
    private Context context;
    private BitmapCache bitmapCache;
    private LensesFragment lensesFragment;

    public LensListAdapter(Context context, ArrayList<LensItemData> lensDataList, LensesFragment lensesFragment,
                           BitmapCache bitmapCache) {
        this.context = context;
        this.lensDataList = lensDataList;
        this.lensesFragment = lensesFragment;
        this.bitmapCache = bitmapCache;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lensholder_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LensItemData lensData = lensDataList.get(position);

        if (lensData == null) {
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

                    if (lensBackgroundLayout == null)
                        return;

                    lensBackgroundLayout.setBackgroundResource(activeState ? R.drawable.lens_bg_selected : R.drawable.lens_bg_unselected);
                    lensData.isActive = activeState;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.itemView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showLensActionMenu(lensData);
                return false;
            }
        });
        holder.lensText.setText(lensData.lensName);
        holder.backgroundLayout.setBackgroundResource(lensData.isActive ? R.drawable.lens_bg_selected : R.drawable.lens_bg_unselected);

        Bitmap cachedBitmap = bitmapCache.getBitmapFromMemCache(lensData.lensCode);
        if(cachedBitmap != null)
            holder.lensIcon.setImageBitmap(cachedBitmap);
        else {
            AsyncTaskCompat.executeParallel(new LensIconLoader.AsyncLensIconDownloader(),
                    lensData, context, holder.lensIcon, bitmapCache);
        }
    }

    @Override
    public int getItemCount() {
        return lensDataList.size();
    }

    private void showLensActionMenu(final LensItemData lensItemData) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.lens_selector_detail_menu, null, false);

        AlertDialog.Builder alertBuilder = new Builder(context);

        Button btnRename = (Button) view.findViewById(R.id.btn_rename_lens);
        btnRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showRenameConfirmation((AlertDialog) view.getTag(), lensItemData);
            }
        });

        Button btnResetName = (Button) view.findViewById(R.id.btn_reset_lens_name);
        btnResetName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog masterDialog = (AlertDialog) view.getTag();
                lensItemData.lensName = Lens.stripLensName(lensItemData.lensCode);

                ContentValues values = new ContentValues();
                values.put(LensEntry.COLUMN_NAME_LENS_NAME, lensItemData.lensName);

                if (Lens.getLensDatabase(context).updateLens(lensItemData.lensCode, values)) {
                    Toast.makeText(context, "Reset lens name back to: " + lensItemData.lensName, Toast.LENGTH_SHORT).show();
                    int position = lensDataList.indexOf(lensItemData);

                    if (position != -1)
                        notifyItemChanged(position);
                    else
                        notifyDataSetChanged();

                    masterDialog.dismiss();
                } else
                    Toast.makeText(context, "Problem resetting lens name", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDelete = (Button) view.findViewById(R.id.btn_delete_lens);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmation((AlertDialog) view.getTag(), lensItemData);
            }
        });

        alertBuilder.setView(view);
        alertBuilder.setNegativeButton("Done", null);

        AlertDialog dialog = alertBuilder.create();

        btnRename.setTag(dialog);
        btnResetName.setTag(dialog);
        btnDelete.setTag(dialog);

        dialog.show();
    }

    private void showRenameConfirmation(final AlertDialog masterDialog, final LensItemData lensItemData) {
        AlertDialog.Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle("Change Lens Name");
        alertBuilder.setMessage("Change lens name from " + lensItemData.lensName);

        final EditText txtName = new EditText(context);
        txtName.setHint("Enter new name");
        alertBuilder.setView(txtName);

        alertBuilder.setNegativeButton("Cancel", null);
        alertBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newName = txtName.getText().toString();

                if (newName.length() > 0) {
                    ContentValues values = new ContentValues();
                    values.put(LensEntry.COLUMN_NAME_LENS_NAME, newName);

                    if (Lens.getLensDatabase(context).updateLens(lensItemData.lensCode, values)) {
                        Toast.makeText(context, "Updated lens name to: " + newName, Toast.LENGTH_SHORT).show();
                        lensItemData.lensName = newName;
                        int position = lensDataList.indexOf(lensItemData);

                        if (position != -1)
                            notifyItemChanged(position);
                        else
                            notifyDataSetChanged();

                        masterDialog.dismiss();
                    }
                } else
                    Toast.makeText(context, "Name must not be blank", Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.create().show();
    }

    private void showDeleteConfirmation(final AlertDialog masterDialog, final LensItemData lensItemData) {
        AlertDialog.Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle("Lens Deletion Confirmation");
        alertBuilder.setMessage("Are you absolutely sure you want to delete lens " + lensItemData.lensName + "?");
        alertBuilder.setNegativeButton("NO", null);
        alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Lens.getLensDatabase(context).deleteLens(lensItemData.lensCode))
                    Toast.makeText(context, "Successfully removed lens", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "Problem removing lens", Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = lensDataList.indexOf(lensItemData);

                if (position == -1 || !lensDataList.remove(lensItemData))
                    return;

                notifyItemRemoved(position);
                lensesFragment.refreshLensCount();
                masterDialog.dismiss();
            }
        });

        alertBuilder.create().show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView lensIcon;
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
