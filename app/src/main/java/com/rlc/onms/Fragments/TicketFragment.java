package com.rlc.onms.Fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;


import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rlc.onms.Adapters.CoordinateAdapter;
import com.rlc.onms.Entities.Coordinate;
import com.rlc.onms.R;
import com.rlc.onms.Views.MaxHeightListView;

public class TicketFragment extends Fragment implements OnMapReadyCallback {

    private ListView listViewCoordinates;
    private ArrayList<Coordinate> coordinateList;
    private CoordinateAdapter adapter;
    private EditText topluMetin;

    private GoogleMap mMap;
    private MapView mapView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

        // UI units
        topluMetin = view.findViewById(R.id.editTextClipboard);
        Button pasteButton = view.findViewById(R.id.buttonPaste);
        Button buttonClear = view.findViewById(R.id.buttonClear);
        listViewCoordinates = view.findViewById(R.id.listViewCoordinates);
        MaxHeightListView listViewCoordinates = view.findViewById(R.id.listViewCoordinates);
        listViewCoordinates.setMaxHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                200, // >>>>>>>>>>>>>>>>>>> ListView yükseklik sınırı
                getResources().getDisplayMetrics()
        ));


        FrameLayout mapContainer = view.findViewById(R.id.mapContainer);

        // Harita başlangıçta gizli
        mapContainer.setVisibility(View.GONE);

        // Koordinat listesi ve adaptör
        coordinateList = new ArrayList<>();
        adapter = new CoordinateAdapter(requireContext(), coordinateList);
        listViewCoordinates.setAdapter(adapter);
        listViewCoordinates.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (coordinateList.isEmpty()) {
            listViewCoordinates.setVisibility(View.GONE);
        } else {
            listViewCoordinates.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        listViewCoordinates.setOnItemClickListener((parent, view1, position, id) -> {
            listViewCoordinates.setItemChecked(position, true);
            adapter.notifyDataSetChanged();

            // Diğer işlemler (örneğin harita güncellemesi)
            mapContainer.setVisibility(View.VISIBLE);

            Coordinate selectedCoordinate = coordinateList.get(position);
            try {
                double latitude = Double.parseDouble(selectedCoordinate.getLatitude());
                double longitude = Double.parseDouble(selectedCoordinate.getLongitude());
                LatLng selectedLatLng = new LatLng(latitude, longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Seçilen Konum"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 17));
            } catch (NumberFormatException e) {
                Log.e("CoordinateParseError", "Hatalı koordinat formatı", e);
            }
        });


        pasteButton.setOnClickListener(v -> clipboardMetniAl());

        buttonClear.setOnClickListener(v -> Toast.makeText(requireContext(), "Temizlemek için basılı tutun", Toast.LENGTH_SHORT).show());

        buttonClear.setOnLongClickListener(v -> {
            topluMetin.setText("");
            coordinateList.clear();
            mapContainer.setVisibility(View.GONE);
            listViewCoordinates.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return true;
        });

        mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        return view;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

    }

    public void metinDegisti() {
        String koordinatlar = topluMetin.getText().toString();

        if (!koordinatlar.isEmpty()) {
            String pattern = "(?<!\\d)(?!0\\d)\\d{2,3}\\.\\d+"; // Eski desen
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(koordinatlar);

            ArrayList<String> tempCoordinates = new ArrayList<>();
            while (matcher.find()) {
                tempCoordinates.add(matcher.group());
            }


            if (tempCoordinates.size() > 1) {
                // Listeyi temizle
                coordinateList.clear();

                // Koordinatları ekle
                for (int i = 0; i < tempCoordinates.size() - 1; i += 2) {
                    String latitude = tempCoordinates.get(i);
                    String longitude = tempCoordinates.get(i + 1);
                    Coordinate coordinate = new Coordinate(latitude, longitude);
                    coordinateList.add(coordinate);
                }

                listViewCoordinates.setVisibility(View.VISIBLE);

                adapter.notifyDataSetChanged();

            }
        }
    }

    // Clipboard verisini alacak metod
    public void clipboardMetniAl() {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);

        ClipData clip = clipboard.getPrimaryClip();

        if (clip != null && clip.getItemCount() > 0) {
            CharSequence text = clip.getItemAt(0).getText();
            topluMetin.setText(text);
            metinDegisti();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}
