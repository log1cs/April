package org.lycoris.april.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


var isButtonPressed = false

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "jargon"
    }
    val text: LiveData<String> = _text

}