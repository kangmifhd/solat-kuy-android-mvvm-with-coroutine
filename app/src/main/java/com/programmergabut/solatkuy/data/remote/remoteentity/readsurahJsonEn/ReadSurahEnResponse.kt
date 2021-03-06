package com.programmergabut.solatkuy.data.remote.remoteentity.readsurahJsonEn


import com.google.gson.annotations.SerializedName
import com.programmergabut.solatkuy.base.BaseResponse

class ReadSurahEnResponse: BaseResponse() {
    @SerializedName("code")
    var code: Int = 0
    @SerializedName("data")
    lateinit var `data`: Data
    @SerializedName("status")
    lateinit var status: String
}