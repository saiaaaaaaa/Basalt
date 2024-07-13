package com.saianoe.basalt.adapters;

import static com.saianoe.basalt.others.Constants.atSeparator;
import static com.saianoe.basalt.others.Constants.defaultComparator;
import static com.saianoe.basalt.others.Constants.readDB;
import static com.saianoe.basalt.others.Constants.readTable;
import static com.saianoe.basalt.others.Constants.readTableColumns;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;
import com.saianoe.basalt.others.EasySQL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @noinspection ALL*/
public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterHolder> {

    final Context context;
    final List<String> data;
    final OnListItemClick click;
    EasySQL es;
    List<Map<String, String>> values;
    List<String> manga_ids = new ArrayList<>();
    List<String> ch_titles = new ArrayList<>();
    String mangaId;
    List<String> readMangas = new ArrayList<>();

    public ChapterAdapter(Context context, String mangaId, List<String> data, OnListItemClick click){
        this.context = context;
        this.data = data;
        this.click = click;
        this.mangaId = mangaId;
        es = new EasySQL(context);

        values = es.getTableValues(readDB, readTable);

        for (Map<String, String> value : values){
            for (Map.Entry<String, String> entry : value.entrySet()){
                if (entry.getKey().equals(readTableColumns[0])){
                    manga_ids.add(entry.getValue());
                }
                if (entry.getKey().equals(readTableColumns[2])){
                    ch_titles.add(entry.getValue());
                }
            }
        }

        for (int a = 0; a < manga_ids.size(); a++){
            String line = manga_ids.get(a) + atSeparator + ch_titles.get(a);
            if (!readMangas.contains(line)){
                readMangas.add(line);
            }
        }

        readMangas.sort(defaultComparator);

        manga_ids.clear();
        ch_titles.clear();

        for (String a : readMangas){
            String[] split = a.split(atSeparator);
            if (split[0].equalsIgnoreCase(mangaId)){
                manga_ids.add(split[0]);
                ch_titles.add(split[1]);
            }
        }
    }

    @NonNull
    @Override
    public ChapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ChapterHolder(v, click);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterHolder holder, int position) {
        holder.t.setText(data.get(holder.getAdapterPosition()));
        if (holder.getAdapterPosition() == data.size() - 1){
            holder.t.setBackground(AppCompatResources.getDrawable(context, R.drawable.list_last_bg));
        } else {
            holder.t.setBackground(AppCompatResources.getDrawable(context, R.drawable.list_bg));
        }

        if (ch_titles.contains(holder.t.getText())){
            holder.t.setTextColor(context.getResources().getColor(R.color.gray));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnListItemClick{
        void onListItemClick(int position);
    }

    static class ChapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final OnListItemClick click;
        final TextView t;

        public ChapterHolder(@NonNull View itemView, OnListItemClick click) {
            super(itemView);

            this.click = click;

            t = itemView.findViewById(R.id.simple_list_item_text);

            t.setSelected(true);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            click.onListItemClick(getAdapterPosition());
        }
    }
}