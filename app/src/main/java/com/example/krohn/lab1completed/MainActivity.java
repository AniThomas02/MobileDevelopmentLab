package com.example.krohn.lab1completed;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

import static com.example.krohn.lab1completed.R.id.*;

public class MainActivity extends AppCompatActivity implements BiddingFragment.OnFragmentInteractionListener, TableFragment.OnFragmentInteractionListener {
    private ImageView selected;
    private int tablePlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_landscape_five);
        } else {
            setContentView(R.layout.activity_portrait_five);
        }

        BiddingFragment biddingFragment = new BiddingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.table_Frame, biddingFragment);
        fragmentTransaction.commit();

        //get cards and hand views into arrays
        TypedArray cards = getResources().obtainTypedArray(R.array.my_cards);
        TypedArray hand = getResources().obtainTypedArray(R.array.my_hand);

        //Full Deck of cards to draw from
        ArrayList<Integer> deck = new ArrayList<>();
        for(int i= 0; i < 52; i++){
            deck.add(i);
        }
        //get the trump card
        Random r = new Random();
        int spot = r.nextInt(deck.size());
        ((ImageView)findViewById(R.id.image_trumpCard)).setImageResource(cards.getResourceId(spot, 0));

        //fill the deck
        for(int num = 0; num<10; num++){
            spot = r.nextInt(deck.size());
            deck.remove(Integer.valueOf(spot)); //Want to remove the Integer 7, not spot 7.
            ((ImageView)findViewById(hand.getResourceId(num, 0))).setImageResource(cards.getResourceId(spot, 0));
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        selected = null;
    }

    //User makes a bid using the biddingFragment
    public void makeBid(int bid){

    }

    //Add a card to the tableFragment
    public void addToTable(int card){

    }

    //Add a chat into the
    public void addChat(View v){
        //add the text to the correct location and clear out the edittext
        EditText text = (EditText) findViewById(R.id.editText_chat);
        String addText = text.getText().toString();
        text.setText("");
        TextView chatWindow = (TextView) findViewById(text_chat);
        chatWindow.append("Player: " + addText + "\n");
    }

    public void changeBackground(View v){
        if(selected == null){
            //if no card were chosen yet
            v.setBackgroundColor(Color.rgb(50, 100, 50));
            selected = (ImageView)v;
        } else {
            if((ImageView)v == selected){
                //chose a selected card
                v.setBackgroundColor(0);
                selected = null;

                //copy card from hand to table
                TypedArray table = getResources().obtainTypedArray(R.array.my_table_cards);
                ((ImageView)findViewById(table.getResourceId(tablePlace, 0))).setImageDrawable(((ImageView) v).getDrawable());

                //remove card from hand
                ((LinearLayout)v.getParent()).removeView(v);

                //go to next spot in table for next card to be placed
                tablePlace++;
            } else{
                //chose a new card
                selected.setBackgroundColor(0);
                v.setBackgroundColor(Color.rgb(50, 100, 50));
                selected = (ImageView)v;
            }
        }
    }
}