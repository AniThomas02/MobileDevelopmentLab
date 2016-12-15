package com.example.krohn.lab1completed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;

import java.util.ArrayList;


/**
 * Created by Ani Thomas on 12/13/2016.
 */
public class ChatLogAdapter extends ArrayAdapter<String>{
    private RequestQueue requestQueue;

    public ChatLogAdapter(Context context, int resource, ArrayList<String> chatLog){
        super(context, resource, chatLog);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String log = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_chat, parent, false);
        }
        //set chat parts to the correct stuff
        String[] parts = log.split(": :");
        TextView playerName = (TextView) convertView.findViewById(R.id.text_chat_player);
        playerName.setText(parts[0] = ":");
        TextView chatContent = (TextView) convertView.findViewById(R.id.text_chat_content);
        chatContent.setText(parts[1]);

        return convertView;
    }
}
