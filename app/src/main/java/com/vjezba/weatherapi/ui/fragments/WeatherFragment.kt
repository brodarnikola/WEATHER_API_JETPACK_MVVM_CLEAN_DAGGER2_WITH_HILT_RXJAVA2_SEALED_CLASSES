package com.vjezba.weatherapi.ui.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vjezba.domain.model.Weather
import com.vjezba.weatherapi.R
import com.vjezba.weatherapi.databinding.FragmentWeatherBinding
import com.vjezba.weatherapi.viewmodels.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WeatherFragment : Fragment() {

    private var fusedLocationClient: FusedLocationProviderClient? = null

    val weatherViewModel: WeatherViewModel by viewModels()

    var cityName = ""
    var searchNewCityData = false

    lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        context ?: return binding.root

        activity?.tvToolbarTitle?.text = "WEATHER"

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        weatherViewModel.getLastLocationListener(requireContext(), fusedLocationClient)

        setupLiveDataAndObservePattern()

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnStartForecast.setOnClickListener {
            val direction =
                WeatherFragmentDirections.weatherFragmentToForecastFragment(
                    cityName
                )
            findNavController().navigate(direction)
        }

        binding.btnSearchWeatherByCityName.setOnClickListener {
            it.isEnabled = false
            it.alpha = 0.4f
            searchNewCityData = true
            weatherViewModel.getWeatherDataByCityName(etCityName.text.toString())
        }
    }

    private fun setupLiveDataAndObservePattern() {

        weatherViewModel.lastLocation.observe(this, Observer { address ->

            clDisplayDataByCurrentLocation.visibility = View.GONE
            rlSearchDataByCurrentLocation.visibility = View.VISIBLE

            tvCurrentAddress.text = "Current address: " + address.getAddressLine(0)
            if (address.hasLatitude() && address.hasLongitude())
                tvLatLongitude.text =
                    "Latitude: " + address.latitude + " Longitude: " + address.longitude
        })

        weatherViewModel.forecastList.observe(this@WeatherFragment, Observer { item ->

            Log.d(ContentValues.TAG, "Weather data: ${item.weather.joinToString { "-" }}")

            if( searchNewCityData ) {
                binding.btnSearchWeatherByCityName.isEnabled = true
                binding.btnSearchWeatherByCityName.alpha = 1.0f
                if( item.main.temp == "" && item.name == "" ) {
                    notFoundAnyCityWithInsertedText()
                }
                else {
                    setCurrentCityLocation(item)
                }
            }
            else {
                clDisplayDataByCurrentLocation.visibility = View.VISIBLE
                rlSearchDataByCurrentLocation.visibility = View.GONE
            }

            if( item.main.temp == "" && item.name == "" ) {
                notFoundAnyCityWithInsertedText()
            }
            else {
                setCurrentCityLocation(item)
            }
        })
    }

    private fun setCurrentCityLocation(item: Weather) {
        tvCurrentAddress.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.sunflower_black
            )
        )
        if( searchNewCityData ) {
            tvCurrentAddress.text = "Searched address in edittext: " + etCityName.text.toString()
        }

        if (item.weather.isNotEmpty()) {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = "Description: " + item.weather[0].description
        } else
            tvDescription.visibility = View.GONE

        tvTemp.text = "Temp: " + item.main.temp
        tvMax.text = "Temp max: " + item.main.tempMax
        tvFeelsLike.text = "Feels like: " + item.main.feelsLike
        tvWind.text = "Wind Speed: " + item.wind.speed
        cityName = item.name
    }

    private fun notFoundAnyCityWithInsertedText() {
        tvCurrentAddress.setTextColor(ContextCompat.getColor( requireContext(), android.R.color.holo_red_dark ))
        tvCurrentAddress.text = "No match, no city found with this inserted text"

        tvDescription.text = "Description: -"
        tvTemp.text = "Temp: -"
        tvMax.text = "Temp max: -"
        tvFeelsLike.text = "Feels like: -"
        tvWind.text = "Wind Speed: -"
    }


}