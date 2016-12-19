package com.example.krohn.lab1completed;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ani Thomas on 12/19/2016.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {
    private ArrayList<Card> cardArrayList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView cardImg;
        public Card currCard;

        public MyViewHolder(View view){
            super(view);
            cardImg = (ImageView) view.findViewById(R.id.list_image_card);
        }
    }

    public CardAdapter(ArrayList<Card> cards){
        this.cardArrayList = cards;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        TypedArray cardResources = holder.itemView.getResources().obtainTypedArray(R.array.my_cards);
        holder.cardImg.setImageResource(cardResources.getResourceId(cardArrayList.get(position).id, 0));
        holder.currCard = cardArrayList.get(position);
        holder.cardImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.itemView.getContext(), holder.currCard.type, Toast.LENGTH_SHORT).show();
            }
        });
        cardResources.recycle();
    }

    @Override
    public int getItemCount() {
        return cardArrayList.size();
    }

    public void changeData(ArrayList<Card> cards){
        cardArrayList = cards;
        notifyDataSetChanged();
    }
}
