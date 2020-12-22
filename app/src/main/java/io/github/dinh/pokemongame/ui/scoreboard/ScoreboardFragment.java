package io.github.dinh.pokemongame.ui.scoreboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.dinh.pokemongame.HashMapToString;
import io.github.dinh.pokemongame.PlayerDB;
import io.github.dinh.pokemongame.R;

public class ScoreboardFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private ScoreboardFragmentListener scoreboardFragmentListener;
    private ListView itemsListView;
    private EditText nameEditText;
    private Button insertButton;
    private PlayerDB db;
    public interface ScoreboardFragmentListener{
        void onNewPlayerAdded();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ScoreboardViewModel scoreboardViewModel = ViewModelProviders.of(this).get(ScoreboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_scoreboard, container, false);

        insertButton = root.findViewById(R.id.insertButton);
        itemsListView = root.findViewById(R.id.itemsListView);
        nameEditText = root.findViewById(R.id.nameEditText);

        db = new PlayerDB(this.getContext());
        updateListViewDisplay();

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sNewPlayer = nameEditText.getText().toString().trim();

                // Compile pattern
                Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");

                if (!sNewPlayer.equals("") && !sNewPlayer.isEmpty()) {
                    Matcher matcher = pattern.matcher(sNewPlayer);
                    if (matcher.matches()) {
                        try {
                            db.insertPlayer(sNewPlayer);
                            updateListViewDisplay();
                            //insertcodehere
                            scoreboardFragmentListener.onNewPlayerAdded();
                            nameEditText.setText("");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Error message
                    }
                }

            }
        });
        return root;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ScoreboardFragmentListener) {
            scoreboardFragmentListener = (ScoreboardFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ScoreboardFragmentListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        scoreboardFragmentListener = null;
    }
    public void updateListViewDisplay() {
        // create a List of Map<String, ?> objects
        ArrayList<HashMapToString> data = db.getPlayersOrderByPoints();

        // create the resource, from, and to variables
        int resource = R.layout.listview_item;
        String[] from = {"name", "points"};
        int[] to = {R.id.nameTextView, R.id.pointsTextView};

        // create and set the adapter
        SimpleAdapter adapter =
                new SimpleAdapter(this.getContext(), data, resource, from, to);
        itemsListView.setAdapter(adapter);
    }
}