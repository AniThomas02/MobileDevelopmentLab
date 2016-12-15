package com.example.krohn.lab1completed;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity{
    private RequestQueue myRequestQueue;
    public static int MAX_PLAYERS = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myRequestQueue = Volley.newRequestQueue(getApplicationContext());

        setContentView(R.layout.activity_login);
    }

    public void onCheckboxClicked(View view) {
        if(((CheckBox) view).isChecked()){
            ((Button) findViewById(R.id.button_login)).setText(R.string.register_checkbox);
        }else{
            ((Button) findViewById(R.id.button_login)).setText(R.string.login_checkbox);
        }
    }

    public void registerOrLogin(View view){
        CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox_registering);
        String username = ((EditText)findViewById(R.id.editText_username)).getText().toString();
        String password = ((EditText)findViewById(R.id.editText_password)).getText().toString();
        if(!(username.equals("") || password.equals(""))){
            if(checkBox.isChecked()){
                attemptRegister(username, password);
            }else{
                attemptLogin(username, password);
            }
        }else{
            Toast.makeText(getApplicationContext(), "Please ensure both fields are filled out.", Toast.LENGTH_SHORT).show();
        }
    }

    //region REGISTER
    private void attemptRegister(String username, String password) {
        if(isUsernameValid(username) && isPasswordValid(password)){
            checkForUsername(username, password);
        }
    }

    private boolean isUsernameValid(String username) {
        if(username.length()<4){
            Toast.makeText(getApplicationContext(), "Username is too short, it must be at least 4 characters in length.", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private boolean isPasswordValid(String password) {
        if(password.length() < 6){
            Toast.makeText(getApplicationContext(), "Password is too short, it must be at least 6 characters in length.", Toast.LENGTH_SHORT).show();
            return false;
        }else if(!password.matches(".*\\d+.*")){
            Toast.makeText(getApplicationContext(), "Password must contain a number.", Toast.LENGTH_SHORT).show();
            return false;
        }else if(!password.matches(".*[a-z]+.*")){
            Toast.makeText(getApplicationContext(), "Password must contain a lowercase letter.", Toast.LENGTH_SHORT).show();
            return false;
        }else if(!password.matches(".*[A-Z]+.*")){
            Toast.makeText(getApplicationContext(), "Password must contain an uppercase letter.", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private void checkForUsername(final String username, final String password){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("username", username);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/checkUsername.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int count = response.getInt("count");
                                if(count < 1){
                                    addPlayerToRemoteDatabase(username, password);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Someone else has that username already. Please try something else.", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            Toast.makeText(getApplicationContext(), "Error checking database.", Toast.LENGTH_SHORT).show();
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addPlayerToRemoteDatabase(final String username, final String password){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/addPlayer.php?username=" + username + "&password=" + password;
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int playerId = response.getInt("playerId");
                                if(playerId > 0){
                                    EditText username = (EditText) findViewById(R.id.editText_username);
                                    username.setText("");
                                    EditText password = (EditText) findViewById(R.id.editText_password);
                                    password.setText("");
                                    CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox_registering);
                                    checkBox.toggle();
                                    Button loginButton = (Button)findViewById(R.id.button_login);
                                    loginButton.setText("Login");
                                    Toast.makeText(getApplicationContext(), "Player Created!", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            Toast.makeText(getApplicationContext(), "Error checking database.", Toast.LENGTH_SHORT).show();
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region LOGIN
    private void attemptLogin(String username, final String password) {
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("username", username);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getPlayer.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int checkId = response.getInt("id");
                                String checkName = response.getString("name");
                                String checkPassword = response.getString("password");
                                if(checkPassword.equals(password)){
                                    Player player = new Player(checkId, checkName);
                                    checkPlayerCount(player);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Password doesn't seem to be correct. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "No player found. Check your information.", Toast.LENGTH_SHORT).show();
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPlayerCount(final Player player){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/checkPlayerCount.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int count = response.getInt("count");
                                if(count < MAX_PLAYERS){
                                    player.turnNumber = count;
                                    if(count == 0){
                                        createNewGame(player);
                                    }else{
                                        getMyGame(player);
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(), "There are already the maximum number of people playing.", Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewGame(final Player player){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/addGame.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int gameId = response.getInt("Id");
                                if(gameId >= 0){
                                    addPlayerToGame(player, new Game(gameId, 0, 0));
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "Failed to create game.", Toast.LENGTH_SHORT).show();
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMyGame(final Player player){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/getGame.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int gameId = response.getInt("id");
                                if(gameId >= 0){
                                    addPlayerToGame(player, new Game(gameId, 0, 0));
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "Failed to create game.", Toast.LENGTH_SHORT).show();
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPlayerToGame(final Player player, final Game game){
        try {
            if(myRequestQueue == null){
                myRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("id", player.id);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/addPlayerToGame.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int affected = response.getInt("affected");
                                if(affected > 0 ){
                                    sendToGame(player, game);
                                }
                            } catch(Exception ex) {
                                Toast.makeText(getApplicationContext(), "Couldn't add player to game :(", Toast.LENGTH_SHORT).show();
                                System.out.println(ex.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("LoginActivity", error.toString());
                            System.out.println("Error");
                        }
                    });
            myRequestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("LoginActivity", e.toString());
            Toast.makeText(getApplicationContext(), "Error creating new user account.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToGame(Player player, Game game){
        Intent gameIntent = new Intent(this, MainActivity.class);
        gameIntent.putExtra("player", player);
        gameIntent.putExtra("game", game);
        startActivity(gameIntent);
        finish();
    }
    //endregion

    //region LocalStuff
    private long addUserInformationToLocal(String username, String password){
        MyDBContract.MyDbHelper myDbHelper = new MyDBContract.MyDbHelper(getApplicationContext());
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //create my values
        values.put(MyDBContract.DBEntry.COLUMN_NAME_USERNAME, username);
        values.put(MyDBContract.DBEntry.COLUMN_NAME_PASSWORD, password);
        //insert the values into the table
        return db.insert(MyDBContract.DBEntry.TABLE_NAME, null, values);
    }

    private String getUsernameFromLocalDatabase(String username){
        MyDBContract.MyDbHelper myDbHelper = new MyDBContract.MyDbHelper(getApplicationContext());
        SQLiteDatabase rdb = myDbHelper.getReadableDatabase();
        String selection = MyDBContract.DBEntry.COLUMN_NAME_USERNAME + " LIKE ?";
        String[] selectionArgs = {username};
        String[] projection = {MyDBContract.DBEntry.COLUMN_NAME_USERNAME};
        Cursor cursor = rdb.query(MyDBContract.DBEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        String existingUser = "";
        if(cursor.moveToFirst()){
            existingUser = cursor.getString(cursor.getColumnIndexOrThrow(MyDBContract.DBEntry.COLUMN_NAME_USERNAME));
        }
        return existingUser;
    }

    private boolean checkUserInformationInLocalDatabase(String username, String password){
        MyDBContract.MyDbHelper myDbHelper = new MyDBContract.MyDbHelper(getApplicationContext());
        SQLiteDatabase rdb = myDbHelper.getReadableDatabase();
        String selection = MyDBContract.DBEntry.COLUMN_NAME_USERNAME + " LIKE ?";
        String[] selectionArgs = {username};
        String[] projection = {MyDBContract.DBEntry.COLUMN_NAME_PASSWORD};
        Cursor cursor = rdb.query(MyDBContract.DBEntry.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    null,
                                    null,
                                    null);
        if(cursor.moveToFirst()){
            String tempPassword = cursor.getString(cursor.getColumnIndexOrThrow(MyDBContract.DBEntry.COLUMN_NAME_PASSWORD));
            if(!tempPassword.equals(password)){
                Toast.makeText(getApplicationContext(), "Password is incorrect.", Toast.LENGTH_SHORT).show();
                return false;
            }else{
                return true;
            }
        }else{
            Toast.makeText(getApplicationContext(), "No user found by that name.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    //endregion
}