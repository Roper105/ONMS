package com.rlc.onms.Utils;



import android.content.Context;
import android.net.Uri;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsUtil {

    // KML dosyasını haritaya yükler
    public static void loadKmlOnMap(GoogleMap map, Context context, Uri kmzUri) throws IOException, XmlPullParserException {
        if (map != null && kmzUri != null) {
            InputStream inputStream = context.getContentResolver().openInputStream(kmzUri);
            KmlLayer layer = new KmlLayer(map, inputStream, context);
            layer.addLayerToMap();

            // Harita kamera zoom
            List<LatLng> path = new ArrayList<>();
            for (KmlContainer container : layer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    if (placemark.getGeometry() instanceof KmlLineString) {
                        KmlLineString lineString = (KmlLineString) placemark.getGeometry();
                        path.addAll(lineString.getGeometryObject());
                    }
                }
            }
            // 1. metre
            LatLng targetPoint = getPointAtDistance(path, 0);
            if (targetPoint != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPoint, 13));
            }
        }
    }

    // KML verisinden koordinatları çözümleme
    public static List<LatLng> parseKmlForCoordinates(String kmlData) {
        List<LatLng> points = new ArrayList<>();
        String regex = "<coordinates>([\\s\\S]*?)</coordinates>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(kmlData);

        if (matcher.find()) {
            String[] coords = Objects.requireNonNull(matcher.group(1)).trim().split("\\s+");
            for (String coord : coords) {
                String[] latLng = coord.split(",");
                double lon = Double.parseDouble(latLng[0]);
                double lat = Double.parseDouble(latLng[1]);
                points.add(new LatLng(lat, lon));
            }
        }
        return points;
    }

    // Haritada belirtilen mesafedeki bir noktayı bulma
    public static LatLng getPointAtDistance(List<LatLng> path, double targetDistance) {
        float totalDistance = 0;
        for (int i = 1; i < path.size(); i++) {
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    path.get(i - 1).latitude, path.get(i - 1).longitude,
                    path.get(i).latitude, path.get(i).longitude,
                    results
            );
            totalDistance += results[0];

            if (totalDistance >= targetDistance) {
                return path.get(i);
            }
        }
        return null;
    }

    public static LatLng getInterpolatedPointAtDistance(List<LatLng> pathPoints, double targetDistance) {
        double accumulatedDistance = 0.0;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            LatLng pointA = pathPoints.get(i);
            LatLng pointB = pathPoints.get(i + 1);

            double segmentDistance = SphericalUtil.computeDistanceBetween(pointA, pointB);
            System.out.println("Segment: " + pointA + " -> " + pointB + " | Mesafe: " + segmentDistance);

            if (accumulatedDistance + segmentDistance >= targetDistance) {
                double remainingDistance = targetDistance - accumulatedDistance;
                double fraction = remainingDistance / segmentDistance;

                double lat = pointA.latitude + fraction * (pointB.latitude - pointA.latitude);
                double lng = pointA.longitude + fraction * (pointB.longitude - pointA.longitude);

                System.out.println("Interpolated Point: " + lat + ", " + lng);
                return new LatLng(lat, lng);
            }

            accumulatedDistance += segmentDistance;
        }

        System.out.println("Hedef mesafe aşılamadı, NULL dönüyor.");
        return null;
    }

    public static void findLocation(Context context, GoogleMap map, List<LatLng> pathPoints, EditText etDistance) {
        if (pathPoints == null || pathPoints.isEmpty()) {
            Toast.makeText(context, "Önce KMZ dosyası seçin!", Toast.LENGTH_SHORT).show();
            return;
        }
        String input = etDistance.getText().toString();

        if (input.isEmpty()) {
            Toast.makeText(context, "Lütfen metre girin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double targetDistance = Double.parseDouble(input);

        double marginDistance = targetDistance/1.1; // %10 hata payı ile gösteriliyor.

        //LatLng targetPoint = getInterpolatedPointAtDistance(pathPoints, targetDistance);
        LatLng targetPoint = getInterpolatedPointAtDistance(pathPoints, marginDistance);

        if (targetPoint != null && map != null) {
            map.addMarker(new MarkerOptions().position(targetPoint).title(targetDistance + " metre")); // marker hata paylı metrajı göstermiyor yazılan metrajı gösteriyr
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetPoint, 15));
        }
    }


}
