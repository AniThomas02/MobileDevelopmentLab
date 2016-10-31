package com.example.krohn.lab1completed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity{

    boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onCheckboxClicked(View view) {
        if(((CheckBox) view).isChecked()){
            ((Button) findViewById(R.id.button_login)).setText("Register");
            isRegistering = true;
        }else{
            ((Button) findViewById(R.id.button_login)).setText("Login");
            isRegistering = false;
        }
    }

    public void registerOrLogin(View view){
        String username = ((EditText)findViewById(R.id.editText_username)).getText().toString();
        String password = ((EditText)findViewById(R.id.editText_password)).getText().toString();
        if(isRegistering){
            attemptRegister(username, password);
        }else{
            attemptLogin(username, password);
        }
    }

    private void attemptRegister(String username, String password) {
        if(isUsernameValid(username) && isPasswordValid(password)){
            Toast.makeText(getApplicationContext(), "Registered!", Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptLogin(String username, String password) {
        if(username.equals("Erik") && password.equals("Krohn1")){
            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Incorrect username or password. Login unsuccessful.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUsernameValid(String username) {
        if(username.length() < 4) {
            Toast.makeText(getApplicationContext(), "Username must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private boolean isPasswordValid(String password) {
        if(password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,}$")){
            return true;
        }else{
            Toast.makeText(getApplicationContext(), "Error with password. Check password requirements and try again.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

