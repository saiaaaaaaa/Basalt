package com.saianoe.basalt.adapters;

import static com.saianoe.basalt.others.Constants.one;
import static com.saianoe.basalt.others.Constants.zero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;

import java.util.ArrayList;
import java.util.List;

/** @noinspection ALL*/
public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadHolder> {

    final Context context;
    final List<String> ids;
    final List<String> titles;
    List<String> checked = new ArrayList<>();

    public DownloadAdapter(Context context, List<String> titles, List<String> ids){
        this.context = context;
        this.titles = titles;
        this.ids = ids;
        for (String a : titles){
            checked.add(zero);
        }
    }

    @NonNull
    @Override
    public DownloadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_item, parent, false);
        return new DownloadHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadHolder holder, int position) {
        holder.t.setText(titles.get(holder.getAdapterPosition()));
        holder.c.setChecked(false);
        if (checked.get(holder.getAdapterPosition()).equals(one)){
            holder.c.setChecked(true);
        }
        holder.container.setOnClickListener(v -> {
            holder.c.setChecked(!holder.c.isChecked());
            if (holder.c.isChecked()){
                checked.add(holder.getAdapterPosition(), one);
                checked.remove(holder.getAdapterPosition() + 1);
            } else {
                checked.add(holder.getAdapterPosition(), zero);
                checked.remove(holder.getAdapterPosition() + 1);
            }
        });
        if (holder.getAdapterPosition() == titles.size() - 1){
            holder.container.setBackground(AppCompatResources.getDrawable(context, R.drawable.list_last_bg));
        } else {
            holder.container.setBackground(AppCompatResources.getDrawable(context, R.drawable.list_bg));
        }
    }

    public List<String> getChecked(){
        return checked;
    }

    public List<String> allChecked(){
        checked.clear();
        for (String a : titles){
            checked.add(one);
        }
        return checked;
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class DownloadHolder extends RecyclerView.ViewHolder{

        final ConstraintLayout container;
        final TextView t;
        final CheckBox c;

        public DownloadHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.item_container);
            t = itemView.findViewById(R.id.item_name);
            c = itemView.findViewById(R.id.item_checker);
        }
    }
}
