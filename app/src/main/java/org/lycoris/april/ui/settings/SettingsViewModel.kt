package org.lycoris.april.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel



class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Member with permission:\n" + "\n"+
                "- Tuan's OnePlus 8 - permissions: admin, RWX\n" +
                "\n" +
                "- Khanh's Galaxy Note 10 - permissions: RWX\n" +
                "\n" +
                "- Thang's Xiaomi - permission: X\n" +
                "\n" +
                "- Dat's Redmi - permission: X\n" +
                "\n" +
                "- Huy's LG G7 - permission: X"
    }

    val text: LiveData<String> = _text
}
