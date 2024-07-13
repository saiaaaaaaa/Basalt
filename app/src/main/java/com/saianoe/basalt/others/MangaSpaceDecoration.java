package com.saianoe.basalt.others;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.saianoe.basalt.R;

import java.util.Objects;

public class MangaSpaceDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (((GridLayoutManager) Objects.requireNonNull(parent.getLayoutManager())).getSpanCount() == 3){
            View v = parent.getChildAt(0);
            ImageView iv = v.findViewById(R.id.item_layout_manga_cover);

            int screen_width = parent.getWidth();
            float screen_width_div2 = (float) screen_width / 3;
            int img_width = iv.getLayoutParams().width;
            float spaces = screen_width_div2 - img_width;
            float space = spaces / 2;
            float scale = parent.getResources().getDisplayMetrics().density;
            int a = Math.round(space * scale) - (Math.round(space / scale) * 2);
            int b = Math.round(a / scale);
            if ((parent.getChildAdapterPosition(view) + 1) % 3 == 1){
                outRect.right = -b;
            } else if ((parent.getChildAdapterPosition(view) + 1) % 3 == 0){
                outRect.left = -b;
            }

            float spaceScale = space / scale;
            int topBottom = 0;

            while (topBottom < spaceScale){
                topBottom += (int) spaceScale;
            }

            if (state.getItemCount() > 3){
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1 || parent.getChildAdapterPosition(view) == 2){
                    outRect.bottom = topBottom;
                    outRect.top = topBottom * 2;
                } else {
                    outRect.bottom = topBottom;
                    outRect.top = topBottom;
                }

                if (state.getItemCount() % 3 == 0){
                    if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    } else if (parent.getChildAdapterPosition(view) == state.getItemCount() - 2){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    }
                } else {
                    if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    }
                }
            } else {
                outRect.bottom = topBottom * 2;
                outRect.top = topBottom * 2;
            }
        } else {
            View v = parent.getChildAt(0);
            ImageView iv = v.findViewById(R.id.item_layout_manga_cover);

            int screen_width = parent.getWidth();
            float screen_width_div2 = (float) screen_width / 2;
            int img_width = iv.getLayoutParams().width;
            float spaces = screen_width_div2 - img_width;
            float space = spaces / 2;
            float scale = parent.getResources().getDisplayMetrics().density;
            int a = Math.round(space * scale) - (Math.round(space / scale) * 2);
            int b = Math.round(a / scale);
            if (parent.getChildAdapterPosition(view) % 2 == 0){
                outRect.right = -b;
            } else {
                outRect.left = -b;
            }

            float spaceScale = space / scale;
            int topBottom = 0;

            while (topBottom < spaceScale){
                topBottom += (int) spaceScale;
            }

            if (state.getItemCount() > 2){
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1){
                    outRect.bottom = topBottom;
                    outRect.top = topBottom * 2;
                } else {
                    outRect.bottom = topBottom;
                    outRect.top = topBottom;
                }

                if (state.getItemCount() % 2 == 0){
                    if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    } else if (parent.getChildAdapterPosition(view) == state.getItemCount() - 2){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    }
                } else {
                    if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
                        outRect.bottom = topBottom * 2;
                        outRect.top = topBottom;
                    }
                }
            } else {
                outRect.bottom = topBottom * 2;
                outRect.top = topBottom * 2;
            }
        }
    }
}
