package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.krohn.lab1completed.Player;
import com.example.krohn.lab1completed.R;

import java.util.ArrayList;

public class BiddingFragment extends Fragment {
    private Player currPlayer;
    private int handSize;

    public BiddingFragment() {
        // Required empty public constructor
    }

    public static BiddingFragment newInstance(Player player, int handSize) {
        BiddingFragment fragment = new BiddingFragment();
        Bundle args = new Bundle();
        args.putSerializable("player", player);
        args.putInt("handSize", handSize);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currPlayer = (Player) getArguments().getSerializable("player");
        handSize = getArguments().getInt("handSize");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bidding, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        addValues(handSize);
    }

    public void addValues(int numCards){
        while(getView().findViewById(R.id.spinner_bid) == null){
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //there are 10 cards now but there might not be in the future
        //this code cannot be in the onCreate method because the activity is not yet created, therefore it will crash
        Spinner spin = (Spinner)(getView().findViewById(R.id.spinner_bid));
        ArrayList<Integer> arr = new ArrayList<Integer>();
        //add 0 to numCards as possible bids
        for(int i=0; i<=numCards; i++){
            arr.add(i);
        }
        //code to actually add values to the spinner
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getContext(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
    }
}
