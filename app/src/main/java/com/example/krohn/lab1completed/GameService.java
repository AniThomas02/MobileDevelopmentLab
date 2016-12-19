package com.example.krohn.lab1completed;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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

/**
 * Created by Ani Thomas on 12/16/2016.
 */

public class GameService extends IntentService{
    public static volatile boolean imAlive;
    private RequestQueue requestQueue;
    private int gameStatus, gameId, playerId;

    public GameService() {
        super("GameService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent,flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        playerId = intent.getIntExtra("playerId", 0);
        gameId = intent.getIntExtra("gameId", 0);
        try{
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            while (imAlive){
                Thread.sleep(6000);
                getGame();
                grabPlayers();
                if(gameStatus != 0) {
                    getTable();
                }
            }
        }catch (Exception e){

        }
    }

    private void getGame(){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getGame.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int status = response.getInt("status");
                                int playerTurn = response.getInt("playerTurn");
                                if(status != gameStatus) {
                                    gameStatus = status;
                                }
                                Intent statusChange = new Intent("statusFilter");
                                statusChange.putExtra("status", status);
                                statusChange.putExtra("playerTurn", playerTurn);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(statusChange);
                            }catch (Exception error){
                                Log.i("GameService", error.toString());
                                System.out.println("Error: " + error);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("GameService", error.toString());
                            System.out.println("Error: " + error);
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("GameService", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }

    private void grabPlayers(){
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
                                    JSONArray playerResponse = response.getJSONArray("playerList");
                                    JSONArray statusResponse = response.getJSONArray("statusList");
                                    JSONArray handResponse = response.getJSONArray("handList");
                                    Player player;
                                    String score;
                                    ArrayList<Player> players = new ArrayList<>();
                                    for(int i =0; i < playerResponse.length(); i++){
                                        JSONObject row = playerResponse.getJSONObject(i);
                                        player = new Player(row.getInt("id"), row.getString("name"), row.getInt("turn"));
                                        player.scores.add(player.name);
                                        for (int j = 0; j < statusResponse.length(); j++){
                                            JSONObject statusRow = statusResponse.getJSONObject(j);
                                            if(player.id == statusRow.getInt("playerId")){
                                                score = statusRow.getString("contents");
                                                player.scores.add(score);
                                            }
                                        }
                                        for(int k = 0; k < handResponse.length(); k++){
                                            JSONObject handRow = handResponse.getJSONObject(k);
                                            if(player.id == handRow.getInt("playerId")){
                                                Card tempCard;
                                                for(int m = 1; m <= 10; m++){
                                                    tempCard = new Card(handRow.getInt("card" + m));
                                                    if(tempCard.id != -1){
                                                        player.hand.add(tempCard);
                                                    }
                                                }
                                            }
                                        }
                                        players.add(player);
                                    }
                                    Intent playerIntent = new Intent("playerFilter");
                                    playerIntent.putExtra("players", players);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playerIntent);
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("GameService", error.toString());
                            System.out.println("Error");
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("GameService", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }



    private void getTable(){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getTable.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int trump = response.getInt("trump");
                                ArrayList<Card> tableCards = new ArrayList<>();
                                for(int i = 1; i <= 5; i++){
                                    if(response.getInt("table" + i) != -1){
                                        tableCards.add(new Card(response.getInt("table" + i)));
                                    }
                                }
                                Intent tableIntent = new Intent("tableFilter");
                                tableIntent.putExtra("trump", trump);
                                tableIntent.putExtra("table", tableCards);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(tableIntent);
                            }catch (Exception error){
                                Log.i("GameService", error.toString());
                                System.out.println("Error: " + error);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("GameService", error.toString());
                            System.out.println("Error: " + error);
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("GameService", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }
}
