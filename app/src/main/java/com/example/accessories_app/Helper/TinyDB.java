package com.example.accessories_app.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class TinyDB {

    private SharedPreferences preferences;

    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    // --- Get Methods ---
    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    // --- Generic Object Methods ---
    public <T> void putListObject(String key, ArrayList<T> list) {
        checkForNullKey(key);
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<>();
        for (T item : list) {
            objStrings.add(gson.toJson(item));
        }
        putListString(key, objStrings);
    }

    public <T> ArrayList<T> getListObject(String key, Class<T> classOfT) {
        Gson gson = new Gson();
        ArrayList<String> objStrings = getListString(key);
        ArrayList<T> objList = new ArrayList<>();
        for (String jsonString : objStrings) {
            T obj = gson.fromJson(jsonString, classOfT);
            objList.add(obj);
        }
        return objList;
    }

    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[0]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    // --- Utility ---
    private void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }
}
