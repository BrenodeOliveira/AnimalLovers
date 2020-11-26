package br.com.breno.animallovers.utils

import android.content.Context


class ProjectPreferences (context : Context) {
    val PREFERENCE_NAME = "SharedPreferenceExample"
    val PREFERENCE_LOGIN_COUNT = "LoginCount"
    var PREFERENCE_PET_LOGGED = "PetLogged"

    val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getLoginCount() : Int {
        return preference.getInt(PREFERENCE_LOGIN_COUNT, 0)
    }

    fun setLoginCount(count : Int) {
        val editor = preference.edit()
        editor.putInt(PREFERENCE_LOGIN_COUNT, count)
        editor.apply()
    }

    fun getPetLogged() : String? {
        return preference.getString(PREFERENCE_PET_LOGGED, "")
    }

    fun setPetLogged(idPet : String) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_PET_LOGGED, idPet)
        editor.apply()
    }
}