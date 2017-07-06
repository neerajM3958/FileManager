package com.blogspot.afoxtutorials.filemanager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neerajMalhotra on 15-06-2017.
 */

public class CustomRecyclerView extends RecyclerView.Adapter<CustomRecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    int bal = 0;
    private List<DataGetSetter> mList;
    private Context mContext;
    private List<File> mClipBoard;
    private ItemClickCallback itemClickCallback;

    public CustomRecyclerView(Context con, List<DataGetSetter> list) {
        mList = list;
        mContext = con;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DataGetSetter currentData = mList.get(position);
        if (currentData.ismSelected() && position != 0) {
            holder.rootView.setBackgroundColor(new Themer(mContext).fetchDarkerBackgroundColor());
            holder.dropMenu.setVisibility(View.INVISIBLE);
        } else {
            holder.rootView.setBackgroundColor(new Themer(mContext).fetchColorListItemBackground());
            holder.dropMenu.setVisibility(View.VISIBLE);
        }
        if (currentData.getmFileType() == R.drawable.ic_image_24dp) {
            Picasso.with(mContext).load(mList.get(position).getmFile()).resize(50, 50).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(currentData.getmFileType());
            DrawableCompat.setTint(holder.imageView.getDrawable(), new Themer(mContext).fetchAccentColor());
        }
        holder.nameTextView.setText(currentData.getmFileName());
        if (currentData.getmLastModified().equals("Parent Folder") || currentData.getmLastModified().equals("Root disabled")) {
            holder.nameTextView.setTextSize(24);
            holder.dateTextView.setText("");
            holder.dropMenu.setVisibility(View.INVISIBLE);
        } else {
            holder.dateTextView.setText(currentData.getmLastModified());
            holder.nameTextView.setTextSize(14);
        }
        holder.sizeTextView.setText("");
        if (!currentData.getIsDirecotory() && currentData != mList.get(0)) {
            String size = (currentData.getmFileSize());
            holder.sizeTextView.setText(size);
        }
        if (!mList.get(0).ismSelected()) {
            bal = 0;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mList.get(position).getmFileName().charAt(0));
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public interface ItemClickCallback {
        void onItemClick(int p, String action, boolean report);

        void importClipBoard(List<File> list);

        void onItemLongClick(int p, String action, boolean isLognClicked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView nameTextView, dateTextView, sizeTextView, dropMenu;
        ImageView imageView;
        View rootView, container;

        public ViewHolder(View itemView) {
            super(itemView);
            dropMenu = (TextView) itemView.findViewById(R.id.list_item_drop_menu);
            imageView = (ImageView) itemView.findViewById(R.id.property_icon);
            nameTextView = (TextView) itemView.findViewById(R.id.property_name);
            dateTextView = (TextView) itemView.findViewById(R.id.property_date);
            sizeTextView = (TextView) itemView.findViewById(R.id.property_size);
            rootView = itemView.findViewById(R.id.list_item);
            container = itemView.findViewById(R.id.list_item_container);
            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
            dropMenu.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final File file = mList.get(getAdapterPosition()).getmFile();
            switch (v.getId()) {
                case R.id.list_item_drop_menu:
                    if (mList.get(0).ismSelected()) {
                        break;
                    }
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, dropMenu);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.options_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.list_item_menu_rename:
                                    AlertDialog.Builder dilogBox = new AlertDialog.Builder(mContext);
                                    dilogBox.setTitle("Rename");
                                    final EditText editText = new EditText(mContext);
                                    editText.setText(file.getName());
                                    editText.setSelectAllOnFocus(true);
                                    dilogBox.setView(editText);
                                    dilogBox.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String s = editText.getText().toString().trim();
                                            if (!s.isEmpty()) {
                                                boolean b = new OperationHandler(true).reName(file, s);
                                                if (b) {
                                                    mList.get(getAdapterPosition()).setmFileName(s);
                                                    itemClickCallback.onItemClick(getAdapterPosition(), "rename", b);
                                                }
                                            }
                                        }
                                    });
                                    dilogBox.setNegativeButton(android.R.string.cancel, null);
                                    dilogBox.setCancelable(true);
                                    dilogBox.show();
                                    break;
                                case R.id.list_item_menu_move:
                                    itemClickCallback.onItemClick(getAdapterPosition(), "move", true);
                                    break;
                                case R.id.list_item_menu_copy:
                                    itemClickCallback.onItemClick(getAdapterPosition(), "copy", true);
                                    break;
                                case R.id.list_item_menu_delete:
                                    new AlertDialog.Builder(mContext)
                                            .setTitle("Delete")
                                            .setMessage("Do you really want to delete " + file.getName() + " ?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    OperationHandler opH = new OperationHandler(true);
                                                    Boolean b = opH.delete(file);
                                                    String s = b ? "Deleted Successully" : "Can't Delete File";
                                                    Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
                                                    itemClickCallback.onItemClick(getAdapterPosition(), "delete", b);
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, null).show();
                                    break;
                                case R.id.list_item_menu_properties:
                                    AlertDialog.Builder dailogBox = new AlertDialog.Builder(mContext).setTitle("Details");
                                    dailogBox.setCancelable(false)
                                            .setPositiveButton("Ok", null).show();

                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();
                    break;
                case R.id.list_item_container:
                    if (getAdapterPosition() == 0) {
                        bal = 0;
                    }
                    if (bal == 0) {
                        mList.get(0).setSelected(false);
                        mClipBoard = new ArrayList<File>();
                        itemClickCallback.onItemClick(getAdapterPosition(), "null", false);
                    } else if (mList.get(0).ismSelected()) {
                        checkListItem();
                        itemClickCallback.onItemClick(getAdapterPosition(), "longClicked", true);
                        itemClickCallback.importClipBoard(mClipBoard);
                        if (bal == 0) {
                            mList.get(0).setSelected(false);
                            itemClickCallback.onItemClick(getAdapterPosition(), "longClicked", false);
                        }
                    }
                    notifyDataSetChanged();
                    break;
                default:

                    return;
            }

        }

        @Override
        public boolean onLongClick(View v) {
            mList.get(0).setSelected(true);
            if (bal == 0) {
                mClipBoard = new ArrayList<File>();
            }
            checkListItem();
            notifyDataSetChanged();
            itemClickCallback.onItemLongClick(getAdapterPosition(), "longClicked", true);
            itemClickCallback.importClipBoard(mClipBoard);
            return true;
        }

        void checkListItem() {
            DataGetSetter currentData = mList.get(getAdapterPosition());
            if (mList.get(getAdapterPosition()).ismSelected()) {
                currentData.setSelected(false);
                mClipBoard.remove(currentData.getmFile());
                bal--;
            } else {
                if (getAdapterPosition() != 0) {
                    currentData.setSelected(true);
                    mClipBoard.add(currentData.getmFile());
                }
                bal++;
            }
        }
    }
}

