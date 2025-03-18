package com.rlc.onms.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.rlc.onms.R;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        RadioGroup fragmentOptions = view.findViewById(R.id.fragmentOptions);

        // Kullanıcının önceki tercihini göster
        String defaultFragment = getDefaultFragment();
        if ("Ticket Asistan".equals(defaultFragment)) {
            ((RadioButton) view.findViewById(R.id.radioTicketFragment)).setChecked(true);
        } else {
            ((RadioButton) view.findViewById(R.id.radioSaraFragment)).setChecked(true);
        }

        // Kullanıcının yeni tercihlerini kaydet
        fragmentOptions.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedFragment = (checkedId == R.id.radioTicketFragment) ? "Ticket Asistan" : "Şehirler Arası";
            saveDefaultFragment(selectedFragment);
            Toast.makeText(getContext(), "Varsayılan uygulama : " + selectedFragment, Toast.LENGTH_SHORT).show();
        });

        return view;
    }


    private void saveDefaultFragment(String fragmentName) {
        SharedPreferences preferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("default_fragment", fragmentName);
        editor.apply();
    }

    private String getDefaultFragment() {
        SharedPreferences preferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        return preferences.getString("default_fragment", "Ticket Asistan"); // Varsayılan fragment
    }
}



