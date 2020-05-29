package com.programmergabut.solatkuy.ui.fragmentcompass.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.programmergabut.solatkuy.data.local.localentity.MsApi1
import com.programmergabut.solatkuy.data.Repository
import com.programmergabut.solatkuy.data.remote.remoteentity.compassJson.CompassApi
import com.programmergabut.solatkuy.util.Resource

class FragmentCompassViewModel(a: Application, private val repository: Repository): AndroidViewModel(a) {

    private var coordinateID = MutableLiveData<MsApi1>()

    var compassApi : MutableLiveData<Resource<CompassApi>> = Transformations.switchMap(coordinateID){
        repository.getCompass(it)
    } as MutableLiveData<Resource<CompassApi>>

    val msApi1Local = repository.mMsApi1()

    fun fetchCompassApi(msApi1: MsApi1){
        this.coordinateID.value = msApi1
    }

}