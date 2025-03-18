package com.rlc.onms.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.rlc.onms.Adapters.KmzAdapter;
import com.rlc.onms.R;
import com.rlc.onms.Utils.DialogUtil;
import com.rlc.onms.Utils.FileUtil;
import com.rlc.onms.Utils.MapsUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;






public class SaraFragment extends Fragment implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView recyclerView;
    private SearchView searchView;
    private EditText etDistance;
    private MapView mapView;
    private Uri kmzUri;
    private List<LatLng> pathPoints = new ArrayList<>();
    private GoogleMap mMap;
    private KmzAdapter adapter;


    private final ActivityResultLauncher<Intent> fileChooserLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    kmzUri = result.getData().getData();

                    if (kmzUri == null) {
                        Toast.makeText(requireContext(), "Dosya seçimi başarısız!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        String kmlData = FileUtil.extractKmlFromKmz(requireContext(), kmzUri);
                        pathPoints = MapsUtil.parseKmlForCoordinates(kmlData);
                        MapsUtil.loadKmlOnMap(mMap, requireContext(), kmzUri);
                        Toast.makeText(requireContext(), "Dosya Seçildi", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Dosya yükleme hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();

                    } catch (XmlPullParserException e) {
                        Toast.makeText(requireContext(), "XML parse hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();

                    } catch (NullPointerException e) {
                        Toast.makeText(requireContext(), "Seçilen dosya KMZ değil, lütfen doğru dosya türü seçin.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sara, container, false);

        // Bileşenler
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = view.findViewById(R.id.searchView);
        etDistance = view.findViewById(R.id.etDistance);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Adapter başlat

//        startAdapter();

        List<String> kmzList = FileUtil.getInternalKmzFiles(requireContext());

        adapter = new KmzAdapter(kmzList, fileName -> {
            searchView.setQuery(fileName, false);
            recyclerView.setVisibility(View.GONE);

            File file = new File(requireContext().getFilesDir(), "kmz_files/" + fileName);
            if (!file.exists()) {
                Toast.makeText(requireContext(), "Dosya bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri fileUri = Uri.fromFile(file);

            try {
                String kmlData = FileUtil.extractKmlFromKmz(requireContext(), fileUri);
                pathPoints = MapsUtil.parseKmlForCoordinates(kmlData);
                MapsUtil.loadKmlOnMap(mMap, requireContext(), fileUri);  // >>> fileUri kmzUri karıştırma !!!!

                Toast.makeText(requireContext(), fileName + " yüklendi!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Dosya okunamadı!", Toast.LENGTH_SHORT).show();
            } catch (XmlPullParserException e) {
                Toast.makeText(requireContext(), "XML Parsing Hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // SearchView dinleyicisi
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d("SEARCHVIEW", "Arama yapıldı: " + query);
                try {
                    MapsUtil.loadKmlOnMap(mMap, requireContext(), kmzUri);

                } catch (IOException | XmlPullParserException e) {
                    Toast.makeText(requireContext(), "Harita yüklenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                Log.d("SEARCHVIEW", "Arama satırı: " + newText);
                if (newText.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.filter(newText);

                    if (adapter.getItemCount() == 0) {
                        Toast.makeText(requireContext(), "Sonuç bulunamadı!", Toast.LENGTH_SHORT).show();  //Debug filter test
                    }
                }
                return false;
            }
        });

        // Buton dinleyicileri
        view.findViewById(R.id.btnSelectKmz).setOnClickListener(v -> openFileChooser());

        view.findViewById(R.id.btnFindLocation).setOnClickListener(v -> MapsUtil.findLocation(requireContext(), mMap, pathPoints, etDistance));

        view.findViewById(R.id.btnUpdate).setOnClickListener(v -> updateFilesFromDrive());

        view.findViewById(R.id.btnCleanMap).setOnClickListener(v -> Toast.makeText(requireContext(),"Temizlemek için basılı tutun", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnCleanMap).setOnLongClickListener(v ->{
            mMap.clear();
            etDistance.setText("");
            searchView.setQuery("",false);
            kmzUri=null;
            pathPoints=null;

            Toast.makeText(requireContext(),"Harita temizlendi!", Toast.LENGTH_SHORT).show();
            return true;

        });

        return view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void updateFilesFromDrive() {

        AlertDialog loadingDialog = DialogUtil.showLoadingDialog(requireContext());

        new Thread(() -> {
            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                                getResources().openRawResource(R.raw.service_account_key))
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));

                Drive googleDriveService = new Drive.Builder(
                        new NetHttpTransport(),
                        new JacksonFactory(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("ONMS")
                        .build();

                FileUtil.updateFilesFromDrive(requireContext(), googleDriveService, "1FBSfxdP8l5-RJknh4aTLmZ5FeqvS11QJ");
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Dosyalar güncellendi!", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Dosyalar güncellenemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }

    private void openFileChooser() {
        Intent intent = FileUtil.getFileChooserIntent();
        intent.setType("application/vnd.google-earth.kmz"); // Sadece KMZ dosyalarını filtreler
        fileChooserLauncher.launch(intent);
    }

    @Override
    public void onMapReady (@NonNull GoogleMap googleMap){
        mMap = googleMap;
        LatLng bandirma = new LatLng(40.346587, 27.978899);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bandirma, 10));

    }

    @Override
    public void onResume () {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause () {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }





}


