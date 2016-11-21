package com.example.krohn.lab1completed;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if(checkBox.isChecked()){
            attemptRegister(username, password);
        }else{
            attemptLogin(username, password);
        }
    }

    private void attemptRegister(String username, String password) {
        if(isUsernameValid(username) && isPasswordValid(password)){
            if(addUserInformation(username, password) > -1){
                sendToGame(username);
            }
        }
    }

    private void attemptLogin(String username, String password) {
        if(checkUserInformation(username, password)){
            sendToGame(username);
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

    private void sendToGame(String username){
        Intent gameIntent = new Intent(this, MainActivity.class);
        gameIntent.putExtra("user", username);
        startActivity(gameIntent);
        finish();
    }

    private long addUserInformation(String username, String password){
        MyDBContract.MyDbHelper myDbHelper = new MyDBContract.MyDbHelper(getApplicationContext());
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //create my values
        values.put(MyDBContract.DBEntry.COLUMN_NAME_USERNAME, username);
        values.put(MyDBContract.DBEntry.COLUMN_NAME_PASSWORD, password);
        //insert the values into the table
        return db.insert(MyDBContract.DBEntry.TABLE_NAME, null, values);
    }

    private boolean checkUserInformation(String username, String password){
        MyDBContract.MyDbHelper myDbHelper = new MyDBContract.MyDbHelper(getApplicationContext());
        SQLiteDatabase rdb = myDbHelper.getReadableDatabase();
        String selection = MyDBContract.DBEntry.COLUMN_NAME_USERNAME + " LIKE ?";
        String[] selectionArgs = {username};
        String[] projection = {MyDBContract.DBEntry.COLUMN_NAME_USERNAME,
                                MyDBContract.DBEntry.COLUMN_NAME_PASSWORD};
        Cursor cursor = rdb.query(MyDBContract.DBEntry.TABLE_NAME,
                                    projection,
                                    selection,
                                    selectionArgs,
                                    null,
                                    null,
                                    null);
        if(!cursor.moveToFirst()){
            String tempUser = cursor.getString(cursor.getColumnIndexOrThrow(
                    MyDBContract.DBEntry.COLUMN_NAME_USERNAME));
            String tempPassword = cursor.getString(cursor.getColumnIndexOrThrow(MyDBContract.DBEntry.COLUMN_NAME_PASSWORD));
            if(tempUser.equals(username) && tempPassword.equals(password)){
                return true;
            }else{
                Toast.makeText(getApplicationContext(), "Username or password do not match. Please try again.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(getApplicationContext(), "Cannot find user by that name.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}