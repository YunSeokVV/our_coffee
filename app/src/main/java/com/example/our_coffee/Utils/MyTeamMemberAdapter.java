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

// 자신의 팀원들을 리사이클러뷰로 표현하기위한 어댑터다
public class MyTeamMemberAdapter extends RecyclerView.Adapter<MyTeamMemberAdapter.CustomViewHolder_MyTeamMember> {
    // 자신의 팀 목록을 리사이클러뷰로 표현하기위한 어댑터다
    Context context;

    ArrayList<MyteamMember> mList;



    public MyTeamMemberAdapter(ArrayList<MyteamMember> list, Context context) {
        this.mList = list;
        this.context=context;
    }

    @NonNull
    @Override
    public CustomViewHolder_MyTeamMember onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myteam_list, viewGroup, false);
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView=inflater.inflate(R.layout.myteam_member_list,viewGroup,false);
        //CustomViewHolder_MyTeam viewHolder = new CustomViewHolder_MyTeam(view);
        return new CustomViewHolder_MyTeamMember(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder_MyTeamMember viewholder, int position) {
        final MyteamMember myteamMember=mList.get(position);

        viewholder.member_name.setText(mList.get(position).getMember_name());
        viewholder.coffee_menu.setText(mList.get(position).getCoffee_menu());
        viewholder.detail_option.setText(mList.get(position).getCoffee_option());

        // 음료에 대한 상세옵션 기능을 중단하면서 GONE 처리 했다.
        viewholder.detail_option.setVisibility(View.GONE);

        Glide.with(viewholder.itemView.getContext())
                .load(myteamMember.getImage_url())
                .into(viewholder.member_profile);
        viewholder.member_profile.setClipToOutline(true);
        //viewholder.setOnItemClickListener(listener);

    }




    public class CustomViewHolder_MyTeamMember extends RecyclerView.ViewHolder {
        protected TextView member_name;
        protected TextView coffee_menu;
        protected TextView detail_option;
        protected ImageView member_profile;

        public CustomViewHolder_MyTeamMember(View view) {
            super(view);
            this.member_name = (TextView) view.findViewById(R.id.team_member_name);
            this.coffee_menu = (TextView) view.findViewById(R.id.coffee_menu);
            this.detail_option = (TextView) view.findViewById(R.id.coffee_detail_option);

            this.member_profile = (ImageView) view.findViewById(R.id.team_profile);


        }



    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}