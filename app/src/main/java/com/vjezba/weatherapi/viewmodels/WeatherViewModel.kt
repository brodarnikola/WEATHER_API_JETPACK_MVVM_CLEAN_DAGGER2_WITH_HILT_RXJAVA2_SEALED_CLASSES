/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vjezba.weatherapi.viewmodels

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.vjezba.domain.model.Weather
import com.vjezba.domain.repository.WeatherRepository
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import java.util.*
import java.util.concurrent.TimeUnit


class WeatherViewModel @ViewModelInject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _weatherMutableLiveData = MutableLiveData<Weather>().apply {
        value = Weather()
    }

    val weatherList: LiveData<Weather> = _weatherMutableLiveData

    private fun getWeatherFromNetwork(latitude: Double, longitude: Double) {
        weatherRepository.getWeatherData(latitude, longitude)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
            .subscribe(object : Observer<Weather> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(response: Weather) {

                    _weatherMutableLiveData.value?.let {
                        _weatherMutableLiveData.value = response
                    }
                }

                override fun onError(e: Throwable) {
                    Log.d(
                        ContentValues.TAG,
                        "onError received: " + e.message
                    )
                }

                override fun onComplete() {

                }
            })
    }

    fun getWeatherDataByCityName(cityName: String) {
        weatherRepository.getWeatherDataByCityName(cityName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
            .onErrorResumeNext { throwable: Throwable ->
                return@onErrorResumeNext ObservableSource {
                    _weatherMutableLiveData.value?.let {
                        _weatherMutableLiveData.value = Weather()
                    }
                    Log.d("onErrorResumeNext", "on exception resume next, update ui" + throwable.localizedMessage)
                }
            }
//            .onExceptionResumeNext {
//                _weatherMutableLiveData.value?.let {
//                    _weatherMutableLiveData.value = it
//                }
//                Log.d("onExceptionResumeNext","on exception resume next, update ui")
//            }
            .subscribe(object : Observer<Weather> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onNext(response: Weather) {

                    _weatherMutableLiveData.value?.let {
                        _weatherMutableLiveData.value = response
                    }
                }

                override fun onError(e: Throwable) {
                    Log.d(
                        ContentValues.TAG,
                        "onError received: " + e.message
                    )
                }

                override fun onComplete() {

                }
            })
    }

    private fun getWeatherFromLocalStorage() {
//        Observable.fromCallable {
//            val listDBMovies = getMoviesFromDb()
//            Forecast("", listDBMovies, CityData())
//        }
//            .subscribeOn(Schedulers.io())
//            //.flatMap { source: List<Articles> -> Observable.fromIterable(source) } // this flatMap is good if you want to iterate, go through list of objects.
//            //.flatMap { source: News? -> Observable.fromArray(source) or  } // .. iterate through each item
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnNext { data: Forecast? ->
//
//                Log.i("Size of database", "")
//                _weatherMutableLiveData.value?.let { _ ->
//                    _weatherMutableLiveData.value = data
//                }
//            }
//            .subscribe()
    }

    private val _lastLocationMutableLiveData = MutableLiveData<Address>().apply {
        value = Address(Locale("EN"))
    }

    val lastLocation: LiveData<Address> = _lastLocationMutableLiveData

    @SuppressLint("MissingPermission")
    fun getLastLocationListener(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient?
    ) {

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                addAddressValueToTextView( context, currentLatLng)
            }
        }

//        Observable.fromCallable {
//
//            var address = Address(Locale("en"))
//            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
//                if (location != null) {
//                    val currentLatLng = LatLng(location.latitude, location.longitude)
//                    address = addAddressValueToTextView(context, currentLatLng)
//                }
//            }
//            address
//
//            //val address = weatherRepository.getLastLocationListener(context, fusedLocationProviderClient, callback = Handler.Callback,)
//            //address
//            //val listDBMovies = getMoviesFromDb()
//            //Forecast("", listDBMovies, CityData())
//        }
//            .subscribeOn(Schedulers.io())
//            //.flatMap { source: List<Articles> -> Observable.fromIterable(source) } // this flatMap is good if you want to iterate, go through list of objects.
//            //.flatMap { source: News? -> Observable.fromArray(source) or  } // .. iterate through each item
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnNext { data: Address? ->
//
//                Log.i("Size of database", "data is: ${data?.getAddressLine(0)}")
//                _lastLocationMutableLiveData.value?.let { _ ->
//                    _lastLocationMutableLiveData.value = data
//                }
//            }
//            .subscribe()

        //weatherRepository.getLastLocationListener(context, currentLatLng)

//        viewModelScope.launch(Dispatchers.IO) {
//            val locale = Locale( getSystemLanguage(context) )
//            val gcd = Geocoder(context, locale)
//
//            var addresses: MutableList<Address> = mutableListOf()
//            try {
//                addresses =
//                    gcd.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)
//            } catch (e: Exception) {
//                Log.i( "ErrorTag","Exception is: ${e}")
//            }
//            withContext(Dispatchers.Main) {
//                if (addresses != null && addresses.size > 0 && addresses[0] != null
//                    && addresses[0].getAddressLine(0) != null )
//                    _lastLocationMutableLiveData.value = addresses[0]
//            }
//        }
    }

    private fun addAddressValueToTextView(context: Context, currentLatLng: LatLng) {
        val locale = Locale(getSystemLanguage(context))
        val gcd = Geocoder(context, locale)

        var address: MutableList<Address> = mutableListOf()
        try {
            address =
                gcd.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1)
        } catch (e: Exception) {
            Log.i("ErrorTag", "Exception is: ${e}")
        }
        if (address != null && address.size > 0 && address[0] != null
            && address[0].getAddressLine(0) != null
        ) {
            _lastLocationMutableLiveData.value = address[0]
            getWeatherFromNetwork(address[0].latitude, address[0].longitude)
        }
    }

    private fun getSystemLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0).language.toString()
        } else {
            context.resources.configuration.locale.language.toString()
        }
    }


    override fun onCleared() {
        super.onCleared()
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }

}

