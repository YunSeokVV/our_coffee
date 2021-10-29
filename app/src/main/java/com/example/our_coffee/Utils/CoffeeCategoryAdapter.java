package com.example.our_coffee.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.our_coffee.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// 사용자가 원하는 종류의 음료를 고르기 위한 카테고리를 표현하는 리사이클러뷰 어댑터다.
public class CoffeeCategoryAdapter extends RecyclerView.Adapter<CoffeeCategoryAdapter.CustomViewHolder_CoffeeCategoryAdapter> {

    Context context;

    private ArrayList<CoffeeCategory> mList;
    ArrayList<CoffeeCategory> items=new ArrayList<CoffeeCategory>();
    //리사이클러뷰의 각 아이템을 클릭할 수 있도록 선언한 객체다.
    OnItemClickListener listener;

    public void setOnItemClickListener(CoffeeCategoryAdapter.OnItemClickListener listener) {
        this.listener=listener;
    }

    //아이템 클릭시 나타나는 이벤트 이 메소드를 사용해서 리사이클러뷰의 각 아이템에 접근할 수 있다.
    public static interface  OnItemClickListener{
        public void onItemClick(CustomViewHolder_CoffeeCategoryAdapter holder, View view, int position);
    }

    public CoffeeCategoryAdapter(ArrayList<CoffeeCategory> list,Context context) {
        this.mList = list;
        this.context=context;
    }

    @NonNull
    @Override
    public CustomViewHolder_CoffeeCategoryAdapter onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myteam_list, viewGroup, false);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView=inflater.inflate(R.layout.coffee_category_list,viewGroup,false);

        return new CustomViewHolder_CoffeeCategoryAdapter(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder_CoffeeCategoryAdapter viewholder, int position) {

        viewholder.category_name.setText(mList.get(position).getCategory_name());
        viewholder.category_img.setImageResource(mList.get(position).getImg_url());

//        Glide.with(viewholder.itemView.getContext())
//                .load(coffeeCategory.getImage_url())
//                .into(viewholder.team_profile);
//        viewholder.team_profile.setClipToOutline(true);
//        //viewholder.setOnItemClickListener(listener);

    }

    public class CustomViewHolder_CoffeeCategoryAdapter extends RecyclerView.ViewHolder {
        protected TextView category_name;
        protected ImageView category_img;

        public CustomViewHolder_CoffeeCategoryAdapter(View view) {
            super(view);
            this.category_name = (TextView) view.findViewById(R.id.category_name);
            this.category_img = (ImageView) view.findViewById(R.id.category_profile);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    int position=getBindingAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(CustomViewHolder_CoffeeCategoryAdapter.this,view,position);
                    }
                }
            }); //추가된 사항

        }



    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}