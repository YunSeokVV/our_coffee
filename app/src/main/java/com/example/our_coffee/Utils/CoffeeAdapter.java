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

// 커피 설정하면에서 사용하는 리사이클러뷰의 어댑터
public class CoffeeAdapter  extends RecyclerView.Adapter<CoffeeAdapter.CustomViewHolder_CoffeeAdapter> {

    Context context;

    private ArrayList<Coffee> mList;
    ArrayList<Coffee> items=new ArrayList<Coffee>();
    //리사이클러뷰의 각 아이템을 클릭할 수 있도록 선언한 객체다.
    OnItemClickListener listener;

    //아이템 클릭시 나타나는 이벤트 이 메소드를 사용해서 리사이클러뷰의 각 아이템에 접근할 수 있다.
    public static interface  OnItemClickListener{
        public void onItemClick(CustomViewHolder_CoffeeAdapter holder, View view, int position);
    }

    public CoffeeAdapter(ArrayList<Coffee> list,Context context) {
        this.mList = list;
        this.context=context;
    }

    @NonNull
    @Override
    public CustomViewHolder_CoffeeAdapter onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myteam_list, viewGroup, false);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView=inflater.inflate(R.layout.coffee_list,viewGroup,false);
        //CustomViewHolder_MyTeam viewHolder = new CustomViewHolder_MyTeam(view);
        return new CustomViewHolder_CoffeeAdapter(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder_CoffeeAdapter viewholder, int position) {
        final Coffee coffee=mList.get(position);

        viewholder.team_name.setText(mList.get(position).getDrink_name());

        Glide.with(viewholder.itemView.getContext())
                .load(coffee.getDrink_url())
                .into(viewholder.team_profile);
        viewholder.team_profile.setClipToOutline(true);
        //viewholder.setOnItemClickListener(listener);

    }


    //아이템 클릭시 나타나는 이벤트 이 메소드를 사용해서 리사이클러뷰의 각 아이템에 접근할 수 있다.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener=listener;
    }

    public class CustomViewHolder_CoffeeAdapter extends RecyclerView.ViewHolder {
        protected TextView team_name;
        protected ImageView team_profile;

        public CustomViewHolder_CoffeeAdapter(View view) {
            super(view);
            this.team_name = (TextView) view.findViewById(R.id.coffee_name);
            this.team_profile = (ImageView) view.findViewById(R.id.coffee_profile);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    int position=getBindingAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(CustomViewHolder_CoffeeAdapter.this,view,position);
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