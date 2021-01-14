package com.example.openlooper.model

import com.example.openlooper.model.RoundRoute.Options
import com.google.gson.annotations.SerializedName


data class GETRoundRoute (

    @SerializedName("coordinates") var coordinates : List<List<Double>>,
    @SerializedName("options") var options : Options,
    @SerializedName("instructions") var instructions : Boolean = false

)

