package com.example.krohn.lab1completed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Random;

import layout.BiddingFragment;
import layout.ChatFragment;
import layout.ScoresFragment;
import layout.TableFragment;

public class MainActivity extends AppCompatActivity {
    public static int MAX_PLAYERS = 2;
    public static RequestQueue requestQueue;
    public static GameService gameService;
    public Intent serviceIntent;
    public int handSize;
    public int turnStart;
    //instance variables - players
    private Player currPlayer;
    private ArrayList<Player> players;
    //private ImageView selected;

    //gameStatus: 0 = waiting for players, 1 = bidding, 2 = playing cards
    public static Game currGame;

    //table instance variables
    private Card trumpCard;
    private RecyclerView currHandView;
    private CardAdapter currHandAdapter;
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
    private TableReceiver tableReceiver;

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
            handSize = 10;
            turnStart = 0;

            players = new ArrayList<>();
            getPlayers();

            //start status 0, because anytime you join a game it will be 0 until you update
            currTableCards = new ArrayList<>();
            currTableDeck = new ArrayList<>();

            currHandView = (RecyclerView) findViewById(R.id.layout_cards);
            currHandAdapter = new CardAdapter(currPlayer.hand);
            LinearLayoutManager horizontalLayoutManagaer
                    = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
            currHandView.setLayoutManager(horizontalLayoutManagaer);
            currHandView.setAdapter(currHandAdapter);

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

        TextView trump = (TextView) findViewById(R.id.textView_trump);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(currGame.status == 0){
            trump.setText("Waiting for players...");
        }else if(currGame.status == 1){
            trump.setText("Trump: ");
            //create the bidding fragment
            biddingFragment = BiddingFragment.newInstance(currPlayer, handSize);
            fragmentTransaction.replace(R.id.frame_layout_table, biddingFragment);
            fragmentTransaction.commit();
        }else if(currGame.status == 2){
            tableFragment = TableFragment.newInstance(currTableCards);
            fragmentTransaction.replace(R.id.frame_layout_table, tableFragment);
            fragmentTransaction.commit();
        }
        scoresFragment = ScoresFragment.newInstance(players);
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

    @Override
    public void onResume(){
        super.onResume();

        if(currGame.status == 1){
            biddingFragment.addValues(handSize);
            TextView trump = (TextView) findViewById(R.id.textView_trump);
        }
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
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(statusReceiver, new IntentFilter("statusFilter"));
        playerReceiver = new PlayerReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(playerReceiver, new IntentFilter("playerFilter"));
        tableReceiver = new TableReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(tableReceiver, new IntentFilter("tableFilter"));
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
        if(currGame.playerTurn == currPlayer.turnNumber){
            String selectedBid = ((Spinner)findViewById(R.id.spinner_bid)).getSelectedItem().toString();
            addBid(selectedBid);
            //switch the bidding screen to the table
            tableFragment = TableFragment.newInstance(currTableCards);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout_table, tableFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }else{
            Toast.makeText(getApplicationContext(), "It is not your turn to bid yet!", Toast.LENGTH_SHORT).show();
        }
    }

    public void addBid(String bid){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("playerId", currPlayer.id);
            params.put("score", bid);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/addScore.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response != null){
                                    int affected = response.getInt("affected");
                                    if(affected >= 0){
                                        if(currGame.playerTurn < MAX_PLAYERS-1){
                                           currGame.playerTurn++;
                                        }else{
                                            currGame.playerTurn = 0;
                                        }
                                        updateTurn(currGame.playerTurn);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Error adding bid :(", Toast.LENGTH_SHORT).show();
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

    public void updateTurn(int turn){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("turnNumber", turn);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/updateTurn.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response != null){
                                    int affected = response.getInt("affected");
                                    if(affected >= 0){
                                        if(currGame.playerTurn == turnStart){
                                            currGame.status++;
                                            changeDatabaseStatus(currGame.status);
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Error adding bid :(", Toast.LENGTH_SHORT).show();
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

    public void changeBackground(View v){
        if(currGame.status == 2 && currGame.playerTurn == currPlayer.turnNumber) {
            /*
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
            */
        }else{
            Toast.makeText(getApplicationContext(), "It isn't time to play a card yet.", Toast.LENGTH_SHORT).show();
        }
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
                if(players.size() != updatedPlayers.size()){
                    if(updatedPlayers.size() == MAX_PLAYERS){
                        changeDatabaseStatus(1);
                    }
                }
                boolean contained;
                for (Player rPlayer : updatedPlayers) {
                    contained = false;
                    for(Player currentPlayer : players){
                        if(currentPlayer.id == rPlayer.id){
                            contained = true;
                            currentPlayer.scores = rPlayer.scores;
                            currentPlayer.hand = rPlayer.hand;
                        }
                    }
                    if(!contained){
                        players.add(rPlayer);
                    }
                }
                if(scoresFragment != null){
                    scoresFragment.updateScores(players);
                }
                if(currGame.status != 0){
                    for (Player player: players){
                        if(player.id == currPlayer.id){
                            currPlayer = player;
                        }
                    }
                    currHandAdapter.changeData(currPlayer.hand);
                    //refreshHand();
                }
            }
        }
    }

    public void changeDatabaseStatus(int newId){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("statusId", newId);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/updateStatus.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response != null){
                                    response.getInt("affected");
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

    public void refreshTrump(){
        TypedArray cardResources = getResources().obtainTypedArray(R.array.my_cards);
        TextView trumpWords = (TextView) findViewById(R.id.textView_trump);
        trumpWords.setText("Trump: ");
        ((ImageView)findViewById(R.id.image_trumpCard)).setImageResource(cardResources.getResourceId(trumpCard.id, 0));
        cardResources.recycle();
    }

    public void refreshHand(){
        for (Player player: players){
            if(player.id == currPlayer.id){
                currPlayer = player;
            }
        }
        TypedArray cardResources = getResources().obtainTypedArray(R.array.my_cards);
        TypedArray handResources = getResources().obtainTypedArray(R.array.my_hand);
        Card temp;
        for(int i = 0; i < currPlayer.hand.size(); i++){
            temp = currPlayer.hand.get(i);
            ((ImageView)findViewById(handResources.getResourceId(i, 0))).setImageResource(cardResources.getResourceId(temp.id, 0));
            (findViewById(handResources.getResourceId(i, 0))).setTag(temp.id);
        }
        cardResources.recycle();
        handResources.recycle();
    }

    class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data){
            int updatedStatus = data.getIntExtra("status", currGame.status);
            currGame.playerTurn = data.getIntExtra("playerTurn", currGame.playerTurn);
            if(updatedStatus != currGame.status){
                currGame.status = updatedStatus;
                changeStatus();
            }
        }
    }

    public void changeStatus(){
        if(currGame.status == 1){
            if(currPlayer.turnNumber == 0){
                drawCards();
            }
            biddingFragment = BiddingFragment.newInstance(currPlayer, handSize);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frame_layout_table, biddingFragment);
            fragmentTransaction.commitAllowingStateLoss();
            Toast.makeText(getApplicationContext(), "Bidding", Toast.LENGTH_SHORT).show();
        }else if (currGame.status == 2){
            Toast.makeText(getApplicationContext(), "Playing Round", Toast.LENGTH_SHORT).show();
        }else if (currGame.status == 3){
            Toast.makeText(getApplicationContext(), "New Round", Toast.LENGTH_SHORT).show();
        }else if (currGame.status == 4){
            Toast.makeText(getApplicationContext(), "End Game", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawCards(){
        //Full Deck of cards to draw from
        ArrayList<Integer> currDeck = new ArrayList<>();
        for(int i= 0; i < 52; i++){
            currDeck.add(i);
        }

        //get the trump card
        Random r = new Random();
        int nextCard = r.nextInt(currDeck.size());
        trumpCard = new Card(nextCard);
        addTrump(trumpCard.id);
        currDeck.remove(currDeck.get(nextCard));
        refreshTrump();
        //get the hand
        for(Player player: players){
            player.hand.clear();
            for(int num = 0; num < handSize; num++){
                nextCard = r.nextInt(currDeck.size());
                player.hand.add(new Card(currDeck.get(nextCard)));
                currDeck.remove(Integer.valueOf(currDeck.get(nextCard)));
            }
            updateHand(player);
        }
    }

    private void addTrump(int trump){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/updateTrump.php?trump=" + trump;
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int affected = response.getInt("affected");
                                if(affected <= 0) {
                                    Toast.makeText(getApplicationContext(), "Trump not updated", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "Error saving trump.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Error accessing db.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHand(final Player player){
        try {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("playerId", player.id);
            for (int i = 0; i < 10; i++) {
                if (i < player.hand.size()) {
                    params.put("card" + i, player.hand.get(i).id);
                } else {
                    params.put("card" + i, -1);
                }
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/updateHand.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int affected = response.getInt("affected");
                                if(affected <= 0) {
                                    Toast.makeText(getApplicationContext(), player.name +"'s hand not updated!", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "Error updating hand.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Error accessing db.", Toast.LENGTH_SHORT).show();
        }
    }

    class TableReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data){
            try {
                int trump = data.getIntExtra("trump", -1);
                if (trump != -1) {
                    trumpCard = new Card(trump);
                    refreshTrump();
                }
                currTableCards = (ArrayList<Card>) data.getSerializableExtra("table");
                if (tableFragment != null) {
                    tableFragment.updateTable(currTableCards);
                }
            }catch (Exception e) {
                Log.i("MainActivity", e.toString());
            }
        }
    }
}