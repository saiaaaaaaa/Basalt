package com.saianoe.basalt.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/** @noinspection ALL*/
public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.MangaHolder>{
    final List<String> manga_id;
    final List<String> manga_title;
    final List<String> manga_cover;
    final OnItemClickListener listener;
    final OnItemLongClickListener listenerLong;

    public MangaAdapter(List<String> manga_id, List<String> manga_title, List<String> manga_cover, OnItemClickListener listener, OnItemLongClickListener listenerLong){
        this.manga_id = manga_id;
        this.manga_title = manga_title;
        this.manga_cover = manga_cover;
        this.listener = listener;
        this.listenerLong = listenerLong;
    }

    @NonNull
    @Override
    public MangaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_manga, parent, false);
        return new MangaHolder(view, listener, listenerLong);
    }

    @Override
    public void onBindViewHolder(@NonNull MangaHolder holder, int position) {
        holder.t.setText(manga_title.get(holder.getAdapterPosition()));
        Picasso.get().load(manga_cover.get(holder.getAdapterPosition())).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return manga_id.size();
    }

    public String getTitle(int position){
        return manga_title.get(position);
    }

    public String getCoverURL(int position){
        return manga_cover.get(position);
    }

    public String getItemID(int position){
        return manga_id.get(position);
    }


    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(int position);
    }

    static class MangaHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        final TextView t;
        final ImageView img;
        final OnItemClickListener listener;
        final OnItemLongClickListener listenerLong;

        public MangaHolder(@NonNull View itemView, OnItemClickListener listener, OnItemLongClickListener listenerLong) {
            super(itemView);

            t = itemView.findViewById(R.id.item_layout_manga_title);
            img = itemView.findViewById(R.id.item_layout_manga_cover);

            t.setSelected(true);

            this.listener = listener;
            this.listenerLong = listenerLong;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            listenerLong.onItemLongClick(getAdapterPosition());
            return true;
        }
    }
}
