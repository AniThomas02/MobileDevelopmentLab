package layout;

import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.krohn.lab1completed.Card;
import com.example.krohn.lab1completed.R;

import java.util.ArrayList;


public class TableFragment extends Fragment {
    private ArrayList<Card> cardsList;
    private View rootView;

    public TableFragment() {
        // Required empty public constructor
    }

    public static TableFragment newInstance(ArrayList<Card> cardsList) {
        TableFragment tableFragment = new TableFragment();
        Bundle args = new Bundle();
        args.putSerializable("cardsList", cardsList);
        tableFragment.setArguments(args);
        return tableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cardsList = (ArrayList<Card>) getArguments().getSerializable("cardsList");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_table, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateTable(cardsList);
    }

    public void updateTable(ArrayList<Card> updatedCards){
        while(rootView.findViewById(R.id.image_table_card1) == null){
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TypedArray tableResources =  getResources().obtainTypedArray(R.array.my_table_cards);
        TypedArray cardResources = getResources().obtainTypedArray(R.array.my_cards);
        cardsList = updatedCards;
        ImageView cardView;
        for(int i = 0; i < cardsList.size(); i++){
            cardView = (ImageView) getActivity().findViewById(tableResources.getResourceId(i, 0));
            cardView.setImageResource(cardResources.getResourceId(cardsList.get(i).id, 0));
        }
        tableResources.recycle();
        cardResources.recycle();
    }
}
