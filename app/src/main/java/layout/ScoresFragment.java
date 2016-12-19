package layout;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.krohn.lab1completed.Player;
import com.example.krohn.lab1completed.R;

import java.util.ArrayList;

import static com.example.krohn.lab1completed.MainActivity.currGame;

/**
 * Created by Ani Thomas on 12/14/2016.
 */

public class ScoresFragment extends Fragment {
    private ArrayList<Player> playersList;
    private View rootView;

    public ScoresFragment(){
        // Required empty public constructor
    }

    public static ScoresFragment newInstance(ArrayList<Player> playerList){
        ScoresFragment scoresFragment = new ScoresFragment();
        Bundle args = new Bundle();
        args.putSerializable("playerList", playerList);
        scoresFragment.setArguments(args);
        return scoresFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playersList = (ArrayList<Player>) getArguments().getSerializable("playerList");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scores, container, false);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateScores(playersList);
    }

    public void updateScores(ArrayList<Player> updatedPlayers){
        while(rootView.findViewById(R.id.text_score1) == null){
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TypedArray scoreResources =  getResources().obtainTypedArray(R.array.scoring_table);
        playersList = updatedPlayers;
        TextView scoreView;
        for (Player player: playersList) {
            scoreView = (TextView) getActivity().findViewById(scoreResources.getResourceId(player.turnNumber, 0));
            scoreView.setText("");
            for(String score: player.scores){
                scoreView.append(score + "\n");
            }
            if(player.turnNumber == currGame.playerTurn){
                scoreView.setBackgroundColor(Color.parseColor("#2DB82D"));
            }else{
                scoreView.setBackgroundColor(Color.parseColor("#DDDDDD"));
            }
        }
        scoreResources.recycle();
    }
}