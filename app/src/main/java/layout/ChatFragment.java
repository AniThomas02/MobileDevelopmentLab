package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.krohn.lab1completed.ChatLogAdapter;
import com.example.krohn.lab1completed.Player;
import com.example.krohn.lab1completed.R;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.krohn.lab1completed.R.id.editText_chat;

public class ChatFragment extends Fragment {
    private Player player;
    private ArrayList<String> chatLog;
    private ChatLogAdapter chatLogAdapter;
    private RequestQueue requestQueue;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(Player player, ArrayList<String> chatLog) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable("player", player);
        args.putStringArrayList("chatLog", chatLog);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = (Player) savedInstanceState.getSerializable("player");
        chatLog = savedInstanceState.getStringArrayList("chatLog");
    }

    @Override
    public void onStart(){
        super.onStart();

        Button addChatButton = (Button) getView().findViewById(R.id.button_sendText);
        addChatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //grab the text that the user inputted
                EditText text = (EditText) getView().findViewById(editText_chat);
                String addText = text.getText().toString();
                addChatToDatabase(addText);
            }
        });

        ListView chatListView = (ListView)getView().findViewById(R.id.listview_chat);
        chatLogAdapter = new ChatLogAdapter(getContext(), R.layout.list_chat, chatLog);
        chatListView.setAdapter(chatLogAdapter);
    }

    public void addChatToDatabase(final String addText){
        try {
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            }
            JSONObject params = new JSONObject();
            params.put("playerName", player.name);
            params.put("chat", addText);
            String url = "http://webdev.cs.uwosh.edu/students/thomaa04/CardGameLiveServer/addChat.php";
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if(response.getInt("affected") > 0){
                                    chatLog.add(player.name + ": :" + addText);
                                    chatLogAdapter.notifyDataSetChanged();
                                    EditText text = (EditText) getView().findViewById(editText_chat);
                                    text.setText("");
                                }else{
                                    Toast.makeText(getContext().getApplicationContext(), "Message not sent.", Toast.LENGTH_SHORT).show();
                                }
                            }catch (Exception e){
                                Toast.makeText(getContext().getApplicationContext(), "Error sdding text to database.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("ChatFragment", error.getStackTrace().toString());
                        }
                    });
            requestQueue.add(jsObjRequest);
        }catch (Exception e){
            Log.i("ChatFragment", e.getStackTrace().toString());
            Toast.makeText(getContext().getApplicationContext(), "Error with database.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}
