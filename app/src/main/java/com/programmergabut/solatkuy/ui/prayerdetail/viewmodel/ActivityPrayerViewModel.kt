package com.programmergabut.solatkuy.ui.prayerdetail.viewmodel

import androidx.lifecycle.ViewModel

class ActivityPrayerViewModel() : ViewModel() {

//    private val prayers = MutableLiveData<Resource<PrayerApi>>()
//    private val compositeDisposable = CompositeDisposable()
//
//    fun fetchPrayer(){
//        prayers.postValue(Resource.loading(null))
//        compositeDisposable.add(
//            mainRepository.getPrayer()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                prayers.postValue(Resource.success(it))
//            },{
//                prayers.postValue(Resource.error(it.message.toString(),null))
//                Log.d("Main_view_model",it.message.toString())
//            })
//        )
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        compositeDisposable.dispose()
//    }
//
//    fun getPrayer():LiveData<Resource<PrayerApi>>{
//        return prayers
//    }
}