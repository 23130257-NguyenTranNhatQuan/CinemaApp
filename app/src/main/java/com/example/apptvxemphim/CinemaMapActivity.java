package com.example.apptvxemphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CinemaMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double lat, lng;
    private String cinemaName, cinemaAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_map);

        cinemaName    = getIntent().getStringExtra("CINEMA_NAME");
        cinemaAddress = getIntent().getStringExtra("CINEMA_ADDRESS");
        lat = getIntent().getDoubleExtra("CINEMA_LAT", 0);
        lng = getIntent().getDoubleExtra("CINEMA_LNG", 0);

        TextView tvName = findViewById(R.id.tvMapCinemaName);
        TextView tvAddress = findViewById(R.id.tvMapCinemaAddress);
        tvName.setText(cinemaName);
        tvAddress.setText(cinemaAddress);

        findViewById(R.id.btnBackMap).setOnClickListener(v -> finish());

        Button btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
                startActivity(new Intent(Intent.ACTION_VIEW, fallbackUri));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng position = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(position).title(cinemaName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
    }
}