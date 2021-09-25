package com.example.our_coffee.Utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//이 파일은 리사이클러뷰의 아이템 사이의 간격을 조절하는 기능을 구현하기위해 만든 파일이다.
//출처 : https://ngh0924.tistory.com/m/28?category=973019
public class RecyclerDecoration_Height extends RecyclerView.ItemDecoration {
    private final int divHeight;

    public RecyclerDecoration_Height(int divHeight){
        this.divHeight = divHeight;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1){
            outRect.bottom = divHeight;
        }
    }

    // 리사이클러뷰가 있는 코드에서 아래 두 코드를 사용해서 아이템간의 간격을 조절한다. 값이 클수록 간격이 늘어난다.
    //RecyclerDecoration_Height decoration_height = new RecyclerDecoration_Height(20);
    //recyclerView.addItemDecoration(decoration_height);

}
