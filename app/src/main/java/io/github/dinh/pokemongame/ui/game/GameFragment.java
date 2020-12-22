package io.github.dinh.pokemongame.ui.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import io.github.dinh.pokemongame.DatabaseAccess;
import io.github.dinh.pokemongame.HashMapToString;
import io.github.dinh.pokemongame.PlayerDB;
import io.github.dinh.pokemongame.R;

import static android.content.Context.MODE_PRIVATE;

public class GameFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Fragment retainInstanceFragment;

    public GameFragment() {
        // Required empty public constructor
    }

    public interface OnCorrectGuessListener {
        void onCorrectGuess();
    }

    private OnCorrectGuessListener onCorrectGuessListener;
    private GameViewModel gameViewModel;
    private int chosenPokemonId;
    private String chosenPokemon;
    private String chosenSprite;
    private List<String> pokemonList;
    private EditText editText;
    private Button buttonSubmit;
    private ImageView imageView;
    private TextView textView;

    // define the SharedPreferences object
    private SharedPreferences savedValues;

    private Integer currentPlayerId;
    private String currentPlayer;
    private Integer points;
    Spinner playersSpinner;
    private PlayerDB playerDB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCorrectGuessListener) {
            onCorrectGuessListener = (OnCorrectGuessListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCorrectGuessListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCorrectGuessListener = null;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onResume() {
        super.onResume();
        // get the instance variables
        currentPlayer = savedValues.getString("currentPlayer", "");
        checkForPlayer();
        chosenSprite = savedValues.getString("chosenSprite", "");
        chosenPokemon = savedValues.getString("chosenPokemon", "");
        chosenPokemonId = savedValues.getInt("chosenPokemonId", 1);
        currentPlayerId = savedValues.getInt("currentPlayerId", 0);
        CharSequence unfinishedAnswer = savedValues.getString("editText", "");
        if (chosenSprite != null && !chosenSprite.isEmpty()) {
            imageView.setImageDrawable(
                    getResources().getDrawable(
                            getResourceId(chosenSprite, "drawable", Objects.requireNonNull(getContext()))));
        }
        if (currentPlayerId != 0) {
            playersSpinner.setSelection(currentPlayerId);
        }
        editText.setText(unfinishedAnswer);
    }

    public void cleanupRetainInstanceFragment() {
        FragmentManager fm = getFragmentManager();
        try {
            fm.beginTransaction().remove(this.retainInstanceFragment).commit();
        } catch (NullPointerException ex) {

        }
    }

    @Override
    public void onPause() {
        // save the instance variables
        SharedPreferences.Editor editor = savedValues.edit();
        try {
            editor.putString("currentPlayer", currentPlayer);
            editor.putString("chosenPokemon", chosenPokemon);
            editor.putString("chosenSprite", chosenSprite);
            editor.putInt("chosenPokemonId", chosenPokemonId);
            editor.putInt("currentPlayerId", currentPlayerId);
            editor.putString("editText", editText.getText().toString());
        } catch (NullPointerException ex) {
        }
        editor.commit();
        super.onPause();
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        gameViewModel = ViewModelProviders.of(this).get(GameViewModel.class);

        View root = inflater.inflate(R.layout.fragment_game, container, false);

        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this.getContext());
        databaseAccess.open();
        pokemonList = databaseAccess.getPokemonList();
        databaseAccess.close();

        // SPINNER
        this.playersSpinner = root.findViewById(R.id.playersSpinner);
        playersSpinner.setOnItemSelectedListener(this);

        textView = root.findViewById(R.id.textView);
        editText = root.findViewById(R.id.editTextPokemon);
        imageView = root.findViewById(R.id.imageView);
        buttonSubmit = root.findViewById(R.id.button_submit);

        editText.setVisibility(View.INVISIBLE);
        editText.setText("");
        textView.setText(R.string.pick_a_player);
        imageView.setImageDrawable(
                getResources().getDrawable(
                        getResourceId("ic_psyduck", "drawable", Objects.requireNonNull(getContext()))));

        // get SharedPreferences object
        savedValues = this.getActivity().getSharedPreferences("SavedValues", MODE_PRIVATE);
        if (savedInstanceState != null) {
            currentPlayer = savedInstanceState.getString("currentPlayer");
            currentPlayerId = savedInstanceState.getInt("currentPlayerId");
            playersSpinner.setSelection(currentPlayerId);
            chosenPokemon = savedInstanceState.getString("chosenPokemon");
            chosenPokemonId = savedInstanceState.getInt("chosenPokemonId");
            chosenSprite = savedInstanceState.getString("chosenSprite");
            editText.setText(savedInstanceState.getString("editText"));
        }

        // PLAYER DATABASE
        playerDB = new PlayerDB(this.getContext());
        updateSpinnerDisplay();

        buttonSubmit.setText("New Game");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForPlayer();
            }
        });

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void checkForPlayer() {
        if (currentPlayerId != null && buttonSubmit.getText() == "New Game") {
            buttonSubmit.setText(R.string.submit_answer);
            editText.setVisibility(View.VISIBLE);
            textView.setText(R.string.who_is_this_pokemon);

            generateNewPokemon();
        } else if (currentPlayerId != null && buttonSubmit.getText() != "New Game") {
            String usersGuess = editText.getText().toString().trim();
            if (!usersGuess.equals("") && !usersGuess.isEmpty()) {
                usersGuess = usersGuess.replaceAll("[^a-zA-Z]", "");
                if (usersGuess.equalsIgnoreCase(chosenPokemon.replaceAll("[^a-zA-Z]", ""))) {
                    // update point
                    getPoints(currentPlayerId);
                    textView.setText("Correct! It was " + chosenPokemon.substring(0, 1).toUpperCase()
                            + chosenPokemon.substring(1).toLowerCase() + ".\r\n" + "Who's this Pokémon?");
                    playerDB.updatePoints(currentPlayerId, points + 1);
                    onCorrectGuessListener.onCorrectGuess();
                } else {
                    textView.setText("Incorrect! It was " + chosenPokemon.substring(0, 1).toUpperCase()
                            + chosenPokemon.substring(1).toLowerCase() + ".\r\n" + "Who's this Pokémon?");
                }
                generateNewPokemon();
            } else {
                textView.setText("Please type in your answer before hitting submit");
            }
        } else if (currentPlayerId == null && buttonSubmit.getText() != "New Game") {
            textView.setText("Please choose a player from the list before hitting submit");
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void generateNewPokemon() {
        chosenPokemonId = (new Random()).nextInt(pokemonList.size()) + 1;
        chosenSprite = "pokemon_" + chosenPokemonId;
        chosenPokemon = pokemonList.get(--chosenPokemonId);
        imageView.setImageDrawable(
                getResources().getDrawable(
                        getResourceId(chosenSprite, "drawable", Objects.requireNonNull(getContext()))));
        editText.setText("");
    }

    public void updateSpinnerDisplay() {
        // create a List of Map<String, ?> objects
        ArrayList<HashMapToString> data = new ArrayList<HashMapToString>();
        HashMapToString hintMap = new HashMapToString("hint");
        hintMap.put("hint", "Select a Player");
        data.add(hintMap);
        data.addAll(playerDB.getPlayers());
        ArrayAdapter<HashMapToString> adapter =
                new ArrayAdapter<HashMapToString>(Objects.requireNonNull(this.getContext()), android.R.layout.simple_spinner_dropdown_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playersSpinner.setAdapter(adapter);
    }

    protected final static int getResourceId
            (final String resName, final String resType, final Context context) {
        final int ResourceID =
                context.getResources().getIdentifier(resName, resType,
                        context.getApplicationInfo().packageName);
        if (ResourceID == 0) {
            throw new IllegalArgumentException
                    (
                            "No resource string found with name " + resName
                    );
        } else {
            return ResourceID;
        }
    }

    //*****************************************************
    // Event handler for the Spinner
    //*****************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        ArrayList<HashMapToString> data = playerDB.getPlayers();
        if (parent.getId() == this.playersSpinner.getId()) {
            if (position == 0) {
                this.currentPlayerId = null;
            } else {
                this.currentPlayerId = Integer.parseInt(Objects.requireNonNull(data.get(position - 1).get("id")));
                this.points = Integer.parseInt(Objects.requireNonNull(data.get(position - 1).get("points")));
            }
        }
    }

    void getPoints(int id) {
        this.points = Integer.parseInt(Objects.requireNonNull(playerDB.getPlayers().get(id - 1).get("points")));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}