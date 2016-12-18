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
                checkGameStatus();
                if(gameStatus == 0){
                    searchForPlayers();
                }else if (gameStatus == 1){

                }else if (gameStatus == 2){

                }
            }
        }catch (Exception e){

        }
    }

    private void checkGameStatus(){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/checkStatus.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int status = response.getInt("status");
                                if(status != gameStatus){
                                    gameStatus = status;
                                    Intent statusChange = new Intent("statusChange");
                                    statusChange.putExtra("status", status);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(statusChange);
                                }
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
            Log.i("PrivateEventActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB for events.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchForPlayers(){
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
            Log.i("MainActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error Accessing DB.", Toast.LENGTH_SHORT).show();
        }
    }

    private void function(){}
}
