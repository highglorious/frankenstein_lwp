/*
 * Copyright 2014, FANGYTASK
 * http://fangytask.com
 * FRANKENSTEIN
 */

package com.fangytask.livewallpaper.frank;

import com.fangytask.livewallpaper.frank.R;

import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class franksettings extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(
                franklwp.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.franksettings);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                this);
    }

    @Override
    protected void onResume() {
        super.onResume();
       
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
    }
}
