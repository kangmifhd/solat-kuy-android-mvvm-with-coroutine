package com.programmergabut.solatkuy.ui.activitymain

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmergabut.solatkuy.data.PrayerRepositoryImpl
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.util.EnumStatus
import kotlinx.coroutines.launch

class MainActivityViewModel @ViewModelInject constructor(val prayerRepositoryImpl: PrayerRepositoryImpl) : ViewModel(){

    val msSetting = prayerRepositoryImpl.getMsSetting()

    var errStatus = MutableLiveData<EnumStatus>()
    private var errMessage = ""
    fun getErrMsg() = errMessage

    fun updateMsApi1(msApi1: MsApi1) = viewModelScope.launch {

        if(msApi1.latitude.isEmpty() || msApi1.longitude.isEmpty() || msApi1.latitude == "." || msApi1.longitude == "."){
            errMessage = "latitude and longitude cannot be empty"
            errStatus.postValue(EnumStatus.ERROR)
            return@launch
        }

        val arrLatitude = msApi1.latitude.toCharArray()
        val arrLongitude = msApi1.longitude.toCharArray()

        if(arrLatitude[arrLatitude.size - 1] == '.' || arrLongitude[arrLongitude.size - 1] == '.'){
            errMessage = "latitude and longitude cannot be ended with ."
            errStatus.postValue(EnumStatus.ERROR)
            return@launch
        }

        if(arrLatitude[0] == '.' || arrLongitude[0] == '.'){
            errMessage = "latitude and longitude cannot be started with ."
            errStatus.postValue(EnumStatus.ERROR)
            return@launch
        }

        prayerRepositoryImpl.updateMsApi1(msApi1)
        errMessage = "Success change the coordinate"
        errStatus.postValue(EnumStatus.SUCCESS)
    }

    fun updateIsHasOpenApp(isHasOpen: Boolean) = viewModelScope.launch{
        prayerRepositoryImpl.updateIsHasOpenApp(isHasOpen)
    }

}