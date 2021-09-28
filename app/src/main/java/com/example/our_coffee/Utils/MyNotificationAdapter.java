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

public class MyNotificationAdapter extends RecyclerView.Adapter<MyNotificationAdapter.CustomViewHolder_MyNotification> {

    Context context;

    private ArrayList<MyNotification> mList;
    ArrayList<MyNotification> items=new ArrayList<MyNotification>();
    //리사이클러뷰의 각 아이템을 클릭할 수 있도록 선언한 객체다.
    OnItemClickListener listener;

    //아이템 클릭시 나타나는 이벤트 이 메소드를 사용해서 리사이클러뷰의 각 아이템에 접근할 수 있다.
    public static interface  OnItemClickListener{
        public void onItemClick(CustomViewHolder_MyNotification holder, View view, int position);
    }

    public MyNotificationAdapter(ArrayList<MyNotification> list,Context context) {
        this.mList = list;
        this.context=context;
    }

    @NonNull
    @Override
    public CustomViewHolder_MyNotification onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myteam_list, viewGroup, false);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView=inflater.inflate(R.layout.myteam_list,viewGroup,false);
        //CustomViewHolder_MyTeam viewHolder = new CustomViewHolder_MyTeam(view);
        return new CustomViewHolder_MyNotification(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder_MyNotification viewholder, int position) {
        final MyNotification MyNotification=mList.get(position);

        viewholder.team_name.setText(mList.get(position).getTeam_name());

        Glide.with(viewholder.itemView.getContext())
                .load(MyNotification.getImage_url())
                .into(viewholder.team_profile);
        viewholder.team_profile.setClipToOutline(true);
        //viewholder.setOnItemClickListener(listener);

    }


    //아이템 클릭시 나타나는 이벤트 이 메소드를 사용해서 리사이클러뷰의 각 아이템에 접근할 수 있다.
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener=listener;
    }

    public class CustomViewHolder_MyNotification extends RecyclerView.ViewHolder {
        protected TextView team_name;
        protected ImageView team_profile;

        public CustomViewHolder_MyNotification(View view) {
            super(view);
            this.team_name = (TextView) view.findViewById(R.id.team_name);
            this.team_profile = (ImageView) view.findViewById(R.id.team_profile);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    int position=getBindingAdapterPosition();
                    if(listener != null){
                        listener.onItemClick(CustomViewHolder_MyNotification.this,view,position);
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
