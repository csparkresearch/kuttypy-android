package com.example.kuttypy.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<String>("helllo");
    public void setText(String name) {   mText.setValue(name); }
    public LiveData<String> getText() {
        return mText;
    }


}