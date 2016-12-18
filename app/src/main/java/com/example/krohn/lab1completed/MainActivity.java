package com.example.krohn.lab1completed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import layout.BiddingFragment;
import layout.ChatFragment;
import layout.ScoresFragment;
import layout.TableFragment;

public class MainActivity extends AppCompatActivity implements BiddingFragment.OnFragmentInteractionListener {
    public static GameService gameService;
    public Intent serviceIntent;
    public static RequestQueue requestQueue;
    //instance variables - players
    private Player currPlayer;
    private ArrayList<Player> players;
    //private ImageView selected;

    //gameStatus: 0 = waiting for players, 1 = bidding, 2 = playing cards
    private Game currGame;

    //table instance variables
    private Card trumpCard;
    private ArrayList<Card> currTableCards;
    private ArrayList<Card> currTableDeck;
    private ArrayList<String> chatLog;
    private BiddingFragment biddingFragment;
    private TableFragment tableFragment;
    private ChatFragment chatFragment;
    private ScoresFragment scoresFragment;

    //bidding
    private int biddingNumber;

    //recievers
    private StatusReceiver statusReceiver;
    private PlayerReceiver playerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_landscape_five);
        } else {
            setContentView(R.layout.activity_portrait_five);
        }

        if (savedInstanceState == null) {
            Intent gameIntent = getIntent();
            //get player and game from intent
            currPlayer = (Player) gameIntent.getSerializableExtra("player");
            currPlayer.scores.add(currPlayer.name);
            currGame = (Game) gameIntent.getSerializableExtra("game");

            players = new ArrayList<>();
            getPlayers();

            //start status 0, because anytime you join a game it will be 0 until you update
            currTableCards = new ArrayList<>();
            currTableDeck = new ArrayList<>();

            chatLog = new ArrayList<>();
            getChatLog();
        } else {
            //grab all saved variables
            currPlayer = (Player) savedInstanceState.getSerializable("currPlayer");
            players = (ArrayList<Player>) savedInstanceState.getSerializable("players");
            currGame = (Game) savedInstanceState.getSerializable("currGame");
            trumpCard = (Card) savedInstanceState.getSerializable("trumpCard");
            currTableCards = (ArrayList<Card>) savedInstanceState.getSerializable("currTableCards");
            currTableDeck = (ArrayList<Card>) savedInstanceState.getSerializable("currTableDeck");
            chatLog = savedInstanceState.getStringArrayList("chatLog");
            if (currGame.status == 1) {
                biddingNumber = savedInstanceState.getInt("biddingNumber");
            }
        }
        Toast.makeText(getApplicationContext(), "Welcome, " + currPlayer.name + "!", Toast.LENGTH_SHORT).show();


        if(currGame.status == 0){
            TextView trump = (TextView) findViewById(R.id.textView_trump);
            trump.setText("Waiting for players...");
        }else if(currGame.status == 1){
            //create the bidding fragment
            biddingFragment = new BiddingFragment();
            biddingFragment.setArguments(getIntent().getExtras());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frame_layout_table, biddingFragment);
            fragmentTransaction.commit();
        }else if(currGame.status == 2){
            tableFragment = TableFragment.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout_table, tableFragment);
            fragmentTransaction.commit();
        }
        scoresFragment = ScoresFragment.newInstance(players);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_scoreboard, scoresFragment);
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            chatFragment = ChatFragment.newInstance(currPlayer, chatLog);
            fragmentTransaction.replace(R.id.frame_chat, chatFragment);
        }
        fragmentTransaction.commit();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void getChatLog(){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getChatLog.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response != null){
                                    JSONArray chatLogResponse = response.getJSONArray("array");
                                    for(int i =0; i < chatLogResponse.length(); i++){
                                        chatLog.add(chatLogResponse.getString(i));
                                    }
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("MainActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("MainActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPlayers(){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getPlayersInGame.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response != null){
                                    JSONArray playerResponse = response.getJSONArray("array");
                                    Player player;
                                    for(int i =0; i < playerResponse.length(); i++){
                                        JSONObject row = playerResponse.getJSONObject(i);
                                        player = new Player(row.getInt("id"), row.getString("name"), row.getInt("turn"));
                                        player.scores.add( row.getString("name"));
                                        players.add(player);
                                    }
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("MainActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("MainActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawCards(){
        /* TODO Add draw cards back into the game at status change to 1
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

        */
        //welcome the new user
    }

    @Override
    public void onResume(){
        super.onResume();
        //TODO Add back onResume stuff
    }

    @Override
    public void onStart() {
        super.onStart();

        gameService = new GameService();
        serviceIntent = new Intent(getApplicationContext(), GameService.class);
        serviceIntent.putExtra("playerId", currPlayer.id);
        serviceIntent.putExtra("gameId", currGame.id);
        startService(serviceIntent);
        GameService.imAlive = true;

        statusReceiver = new StatusReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(statusReceiver, new IntentFilter("statusChange"));
        playerReceiver = new PlayerReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(playerReceiver, new IntentFilter("playerFilter"));
    }

    public void onSaveInstanceState(Bundle outState){
        /*TODO Make sure this is right*/
        outState.putSerializable("currPlayer", currPlayer);
        outState.putSerializable("players", players);
        outState.putSerializable("currGame", currGame);
        outState.putSerializable("trumpCard", trumpCard);
        outState.putSerializable("currTableCards", currTableCards);
        outState.putSerializable("currTableDeck", currTableDeck);
        outState.putStringArrayList("chatLog", chatLog);
        if(currGame.status == 1){
            outState.putInt("biddingNumber", biddingNumber);
        }
    }

    public void makeBid(View v){
        /*
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
        */
    }

    public void changeBackground(View v){
            /*
        if(currGame.status == 1) {
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
                        //TODO THIS PROBABLY WONT WORK
                        currPlayer.hand.remove(v.getTag());
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
        */
    }

    @Override
    public void onStop(){
        super.onStop();
        GameService.imAlive = false;
    }

    class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data){
            ArrayList<Player> updatedPlayers = (ArrayList<Player>)data.getSerializableExtra("players");
            if(updatedPlayers != null) {
                boolean contained;
                for (Player rPlayer : updatedPlayers) {
                    contained = false;
                    for(Player currPlayer : players){
                        if(currPlayer.id == rPlayer.id){
                            contained = true;
                            currPlayer.scores = rPlayer.scores;
                        }
                    }
                    if(!contained){
                        players.add(rPlayer);
                    }
                }
                if(scoresFragment != null){
                    scoresFragment.updateScores(players);
                }
            }
        }
    }

    class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data){
            int updatedStatus = data.getIntExtra("status", currGame.status);
            if(updatedStatus != currGame.status){
                currGame.status = updatedStatus;
                changeStatus();
            }
        }
    }

    public void changeStatus(){
        if(currGame.status == 1){
            Toast.makeText(getApplicationContext(), "Bidding", Toast.LENGTH_SHORT).show();
        }else if (currGame.status == 2){
            Toast.makeText(getApplicationContext(), "Playing Round", Toast.LENGTH_SHORT).show();
        }else if (currGame.status == 3){

        }else if (currGame.status == 4){

        }
    }
}