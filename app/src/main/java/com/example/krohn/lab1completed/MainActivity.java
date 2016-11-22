package com.example.krohn.lab1completed;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import layout.BiddingFragment;
import layout.TableFragment;

import static com.example.krohn.lab1completed.R.id.*;

public class MainActivity extends AppCompatActivity implements BiddingFragment.OnFragmentInteractionListener, TableFragment.OnFragmentInteractionListener {
    //instance variables
    public String currUser = "";
    private ImageView selected;
    private int trumpId;
    private boolean bidding = true;
    private BiddingFragment biddingFragment;
    private TableFragment tableFragment;
    private ArrayList<Integer> currHand = new ArrayList<>();
    private ArrayList<Integer> currDeck = new ArrayList<>();
    private ArrayList<Integer> currTable = new ArrayList<>();
    private String currScore = "";//new ArrayList<>();
    private String chatLog = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_landscape_five);
        } else {
            setContentView(R.layout.activity_portrait_five);
        }

        if(savedInstanceState == null){
            //Get the username and put in scoreboard
            Intent gameIntent = getIntent();
            currUser = gameIntent.getStringExtra("user");
            TextView myScore = (TextView) findViewById(R.id.text_score1);
            myScore.setText(currUser);
            currScore = currUser;

            //drawable resources
            TypedArray cardResources = getResources().obtainTypedArray(R.array.my_cards);
            TypedArray handResources = getResources().obtainTypedArray(R.array.my_hand);

            //Full Deck of cards to draw from
            currDeck = new ArrayList<>();
            for(int i= 0; i < 52; i++){
                currDeck.add(i);
            }

            //get the trump card
            Random r = new Random();
            trumpId = r.nextInt(currDeck.size());
            ((ImageView)findViewById(R.id.image_trumpCard)).setImageResource(cardResources.getResourceId(trumpId, 0));
            //get the hand
            for(int num = 0; num < 10; num++){
                int temp = r.nextInt(currDeck.size());
                currHand.add(currDeck.get(temp)); //Add to the current hand
                currDeck.remove(Integer.valueOf(temp)); //Want to remove the Integer 7, not spot 7.
                ((ImageView)findViewById(handResources.getResourceId(num, 0))).setImageResource(cardResources.getResourceId(temp, 0));
                (findViewById(handResources.getResourceId(num, 0))).setTag(temp);
            }
            //welcome the new user
            Toast.makeText(getApplicationContext(), "Welcome, " + currUser + "!", Toast.LENGTH_SHORT).show();
        }else{
            //grab all saved variables
            currUser = savedInstanceState.getString("username");
            trumpId = savedInstanceState.getInt("trumpCard");
            bidding = savedInstanceState.getBoolean("bidding");
            currHand = savedInstanceState.getIntegerArrayList("currHand");
            currDeck = savedInstanceState.getIntegerArrayList("currDeck");
            currTable = savedInstanceState.getIntegerArrayList("currTable");
            currScore = savedInstanceState.getString("currScore");
            chatLog = savedInstanceState.getString("chatLog");
        }
        //figure out which fragment needs to be created
        if(bidding){
            //create the bidding fragment
            biddingFragment = new BiddingFragment();
            biddingFragment.setArguments(getIntent().getExtras());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.table_Frame, biddingFragment);
            fragmentTransaction.commit();
        }else{
            tableFragment = TableFragment.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.table_Frame, tableFragment);
            fragmentTransaction.commit();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        selected = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        TextView myScore = (TextView) findViewById(R.id.text_score1);
        myScore.setText(currScore);
        //drawable resources
        TypedArray cardResources = getResources().obtainTypedArray(R.array.my_cards);
        TypedArray handResources = getResources().obtainTypedArray(R.array.my_hand);
        //put cards into place
        ((ImageView)findViewById(R.id.image_trumpCard)).setImageResource(cardResources.getResourceId(trumpId, 0));
        int i;
        for(i = 0; i < currHand.size(); i++){
            ((ImageView)findViewById(handResources.getResourceId(i, 0))).setImageResource(cardResources.getResourceId(currHand.get(i), 0));
            (findViewById(handResources.getResourceId(i, 0))).setTag(currHand.get(i));
        }
        for(; i < 10; i++){
            ImageView temp = (ImageView)findViewById(handResources.getResourceId(i, 0));
            ((LinearLayout) temp.getParent()).removeView(temp);
        }
        //set up specifics for landscape/portrait
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            TextView chatWindow = (TextView) findViewById(text_chat);
            chatWindow.setText(chatLog);
        }
        if(bidding) {
            biddingFragment.addValues(10);
        }else{
            TypedArray tableResources = getResources().obtainTypedArray(R.array.my_table_cards);
            for(int currTableIndex = 0; currTableIndex < currTable.size(); currTableIndex++){
                ((ImageView) findViewById(tableResources.getResourceId(currTableIndex, 0))).setImageResource(cardResources.getResourceId(currTable.get(currTableIndex), 0));
            }
        }
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putString("username", currUser);
        outState.putInt("trumpCard", trumpId);
        outState.putBoolean("bidding", bidding);
        outState.putIntegerArrayList("currHand", currHand);
        outState.putIntegerArrayList("currDeck", currDeck);
        outState.putIntegerArrayList("currTable", currTable);
        outState.putString("currScore", currScore);
        outState.putString("chatLog", chatLog);
    }

    //Add a chat into the
    public void addChat(View v){
        //add the text to the correct location and clear out the edittext
        EditText text = (EditText) findViewById(R.id.editText_chat);
        String addText = text.getText().toString();
        text.setText("");
        TextView chatWindow = (TextView) findViewById(text_chat);
        chatWindow.append(currUser + ": " + addText + "\n");
        chatLog = chatWindow.getText().toString();
    }

    public void makeBid(View v){
        String selectedBid = ((Spinner)findViewById(R.id.spinner_bid)).getSelectedItem().toString();
        TextView myScore = (TextView) findViewById(R.id.text_score1);
        String message = currUser + "\n" + selectedBid;
        myScore.setText(message);

        //switch the bidding screen to the table
        tableFragment = TableFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.table_Frame, tableFragment);
        fragmentTransaction.commit();
        //ensure that the bidding phase is over
        bidding = false;
}

    public void changeBackground(View v){
        if(!bidding) {
            if (selected == null) {
                //if no card were chosen yet
                v.setBackgroundColor(Color.parseColor("#33b5e5"));
                selected = (ImageView) v;
            } else {
                if (v == selected) {
                    //chose a selected card
                    v.setBackgroundColor(Color.parseColor("#5360ae"));
                    selected = null;
                    if(currTable.size() < 5){
                        //copy card from hand to table
                        TypedArray tableResources = getResources().obtainTypedArray(R.array.my_table_cards);
                        ((ImageView) findViewById(tableResources.getResourceId(currTable.size(), 0))).setImageDrawable(((ImageView) v).getDrawable());
                        currTable.add((Integer) v.getTag());

                        //remove card from hand
                        currHand.remove(v.getTag());
                        ((LinearLayout) v.getParent()).removeView(v);
                    }
                } else {
                    //chose a new card
                    selected.setBackgroundColor(Color.parseColor("#5360ae"));
                    v.setBackgroundColor(Color.parseColor("#33b5e5"));
                    selected = (ImageView) v;
                }
            }
        }
    }
}