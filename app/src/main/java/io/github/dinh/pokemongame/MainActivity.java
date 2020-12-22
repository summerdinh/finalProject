package io.github.dinh.pokemongame;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import io.github.dinh.pokemongame.ui.game.GameFragment;
import io.github.dinh.pokemongame.ui.scoreboard.ScoreboardFragment;

public class MainActivity extends AppCompatActivity implements GameFragment.OnCorrectGuessListener, ScoreboardFragment.ScoreboardFragmentListener {

    private GameFragment gameFragment;
    private ScoreboardFragment scoreboardFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameFragment = (GameFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_game);
        scoreboardFragment = (ScoreboardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_scoreboard);

        if(gameFragment==null||scoreboardFragment==null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_game, new GameFragment())
                    .replace(R.id.fragment_scoreboard, new ScoreboardFragment())
                    .addToBackStack(null)
                    .commit();
        }



        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_game, R.id.navigation_scoreboard)
                .build();
        try {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        } catch (IllegalArgumentException ex) {

        }

    }

    @Override
    public void onCorrectGuess() {
        scoreboardFragment.updateListViewDisplay();
    }

    @Override
    public void onNewPlayerAdded() {
        gameFragment.updateSpinnerDisplay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finishActivity(0);
    }
}