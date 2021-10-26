package com.example.our_coffee.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.our_coffee.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GatherTeamAdapter extends RecyclerView.Adapter<GatherTeamAdapter.CustomViewHolder_GatherTeam> {

    Context context;
    private ArrayList<GatherTeam> mList;

    public GatherTeamAdapter(ArrayList<GatherTeam> list, Context context) {
        this.mList = list;
        this.context=context;
    }

    @NonNull
    @Override
    public GatherTeamAdapter.CustomViewHolder_GatherTeam onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView=inflater.inflate(R.layout.gather_list,viewGroup,false);
        return new CustomViewHolder_GatherTeam(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GatherTeamAdapter.CustomViewHolder_GatherTeam viewholder, int position) {

        viewholder.item.setText(mList.get(position).getMenu());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    public class CustomViewHolder_GatherTeam extends RecyclerView.ViewHolder {
        protected TextView item;

        public CustomViewHolder_GatherTeam(@NonNull View view) {
            super(view);
            this.item = (TextView) view.findViewById(R.id.item);
        }
    }
}
