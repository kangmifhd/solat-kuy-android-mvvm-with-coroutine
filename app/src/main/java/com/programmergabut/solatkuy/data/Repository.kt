package com.programmergabut.solatkuy.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.programmergabut.solatkuy.data.local.LocalDataSource
import com.programmergabut.solatkuy.data.local.dao.*
import com.programmergabut.solatkuy.data.local.localentity.*
import com.programmergabut.solatkuy.data.remote.RemoteDataSourceAladhan
import com.programmergabut.solatkuy.data.remote.RemoteDataSourceApiAlquran
import com.programmergabut.solatkuy.data.remote.remoteentity.compassJson.CompassResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.prayerJson.PrayerResponse
import com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonAr.ReadSurahArResponse
import com.programmergabut.solatkuy.util.Resource
import com.programmergabut.solatkuy.util.enumclass.EnumConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/*
 * Created by Katili Jiwo Adi Wiyono on 26/03/20.
 */

class Repository @Inject constructor(
    val remoteDataSourceAladhan: RemoteDataSourceAladhan,
    val remoteDataSourceApiAlquran: RemoteDataSourceApiAlquran,
    val notifiedPrayerDao: NotifiedPrayerDao,
    val msApi1Dao: MsApi1Dao,
    val msSettingDao: MsSettingDao,
    val msFavAyahDao: MsFavAyahDao,
    val msFavSurahDao: MsFavSurahDao
) {

    /* companion object{
        @Volatile
        private var instance: Repository? = null

        fun getInstance(remoteDataSourceAladhan: RemoteDataSourceAladhan,
                        remoteDataSourceApiAlquran: RemoteDataSourceApiAlquran,
                        localDataSource: LocalDataSource) =
            instance ?: synchronized(this){
                instance
                    ?: Repository(remoteDataSourceAladhan, remoteDataSourceApiAlquran, localDataSource)
            }
    } */

    /* Room */
    /* NotifiedPrayer */
    suspend fun updatePrayerIsNotified(prayerName: String, isNotified: Boolean) = notifiedPrayerDao.updatePrayerIsNotified(prayerName, isNotified)

    /* MsApi1 */
    fun getMsApi1(): LiveData<Resource<MsApi1>> {
        val data = MediatorLiveData<Resource<MsApi1>>()
        val msApi1 = msApi1Dao.getMsApi1()

        data.value = Resource.loading(null)

        data.addSource(msApi1) {
            data.value = Resource.success(it)
        }

        return data
    }
    suspend fun updateMsApi1(msApi1: MsApi1) = msApi1Dao.updateMsApi1(
        msApi1.api1ID, msApi1.latitude, msApi1.longitude, msApi1.method, msApi1.month, msApi1.year
    )

    /* MsFavAyah */
    fun getMsFavAyah() : LiveData<Resource<List<MsFavAyah>>> {
        val data = MediatorLiveData<Resource<List<MsFavAyah>>>()
        val listfavAyah = msFavAyahDao.getMsFavAyah()

        data.value = Resource.loading(null)

        data.addSource(listfavAyah) {
            data.value = Resource.success(it)
        }

        return data
    }
    fun getMsFavAyahBySurahID(surahID: Int): LiveData<Resource<List<MsFavAyah>>> {
        val data = MediatorLiveData<Resource<List<MsFavAyah>>>()
        val listfavAyah = msFavAyahDao.getMsFavAyahBySurahID(surahID)

        data.value = Resource.loading(null)

        data.addSource(listfavAyah) {
            data.value = Resource.success(it)
        }

        return data
    }
    suspend fun insertFavAyah(msFavAyah: MsFavAyah) = msFavAyahDao.insertMsAyah(msFavAyah)
    suspend fun deleteFavAyah(msFavAyah: MsFavAyah) = msFavAyahDao.deleteMsFavAyah(msFavAyah)

    /* MsFavSurah */
    fun getMsFavSurah(): LiveData<Resource<List<MsFavSurah>>> {
        val data = MediatorLiveData<Resource<List<MsFavSurah>>>()
        val listfavSurah = msFavSurahDao.getMsFavSurah()

        data.value = Resource.loading(null)

        data.addSource(listfavSurah) {
            data.value = Resource.success(it)
        }

        return data
    }
    fun getMsFavSurahByID(ayahID: Int): LiveData<Resource<MsFavSurah>> {
        val data = MediatorLiveData<Resource<MsFavSurah>>()
        val listfavSurah = msFavSurahDao.getMsFavSurahBySurahID(ayahID)

        data.value = Resource.loading(null)

        data.addSource(listfavSurah) {
            data.value = Resource.success(it)
        }

        return data
    }
    suspend fun insertFavSurah(msFavSurah: MsFavSurah) = msFavSurahDao.insertMsSurah(msFavSurah)
    suspend fun deleteFavSurah(msFavSurah: MsFavSurah) = msFavSurahDao.deleteMsFavSurah(msFavSurah)

    /* MsSetting */
    fun getMsSetting(): LiveData<Resource<MsSetting>> {
        val data = MediatorLiveData<Resource<MsSetting>>()
        val msSetting = msSettingDao.getMsSetting()

        data.value = Resource.loading(null)

        data.addSource(msSetting) {
            data.value = Resource.success(it)
        }

        return data
    }
    suspend fun updateIsUsingDBQuotes(isUsingDBQuotes: Boolean) = msSettingDao.updateIsUsingDBQuotes(isUsingDBQuotes)

    /*
     * Retrofit
     */
    fun fetchCompass(msApi1: MsApi1) = remoteDataSourceAladhan.fetchCompassApi(msApi1)

    fun fetchPrayerApi(msApi1: MsApi1) = remoteDataSourceAladhan.fetchPrayerApi(msApi1)

    fun fetchReadSurahEn(surahID: Int) = remoteDataSourceApiAlquran.fetchReadSurahEn(surahID)

    fun fetchAllSurah() = remoteDataSourceApiAlquran.fetchAllSurah()

    fun fetchReadSurahAr(surahID: Int): MutableLiveData<Resource<ReadSurahArResponse>> = remoteDataSourceApiAlquran.fetchReadSurahAr(surahID)

    fun syncNotifiedPrayer(msApi1: MsApi1): LiveData<Resource<List<NotifiedPrayer>>> {

        return object : NetworkBoundResource<List<NotifiedPrayer>, PrayerResponse>(){
            override fun loadFromDB(): LiveData<List<NotifiedPrayer>> = notifiedPrayerDao.getNotifiedPrayer()

            override fun shouldFetch(data: List<NotifiedPrayer>?): Boolean = true

            override fun createCall(): LiveData<Resource<PrayerResponse>> = remoteDataSourceAladhan.fetchPrayerApi(msApi1)

            override fun saveCallResult(data: PrayerResponse){
                val sdf = SimpleDateFormat("dd", Locale.getDefault())
                val currentDate = sdf.format(Date())

                val timings = data.data.find { obj -> obj.date.gregorian?.day == currentDate.toString() }?.timings

                val map = mutableMapOf<String, String>()

                map[EnumConfig.fajr] = timings?.fajr.toString()
                map[EnumConfig.dhuhr] = timings?.dhuhr.toString()
                map[EnumConfig.asr] = timings?.asr.toString()
                map[EnumConfig.maghrib] = timings?.maghrib.toString()
                map[EnumConfig.isha] = timings?.isha.toString()
                map[EnumConfig.sunrise] = timings?.sunrise.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    map.forEach { p ->
                        notifiedPrayerDao.updatePrayerTime(p.key, p.value)
                    }
                }
            }
        }.asLiveData()
    }

}