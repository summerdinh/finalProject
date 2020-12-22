package io.github.dinh.pokemongame.ui.scoreboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScoreboardViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScoreboardViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}