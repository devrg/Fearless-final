package android.srrr.com.fearless;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;
import static android.content.Context.LOCATION_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.ALL_PERMISSION;

public class HomeFragment extends Fragment implements OnMapReadyCallback{

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton pol_fab, hos_fab;
    private GoogleMap gMap;
    private LocationManager locationManager;
    private Double lat, lng;
    private FirebaseAuth mAuth;
    private CoordinatorLayout main_coord;
    private SharedPreferences sharedPref;

    private LocationFetch loc_fetch;

    String[] Permissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CALL_PHONE, READ_CONTACTS, SEND_SMS};

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_tab_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.get_curr_loc_menu){
            pointCurrentLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionMenu = getView().findViewById(R.id.fab_menu);
        floatingActionMenu.setIconAnimated(false);

        pol_fab = getView().findViewById(R.id.police_fab);
        hos_fab = getView().findViewById(R.id.hospital_fab);
        main_coord = getActivity().findViewById(R.id.main_coordinator);

        loc_fetch = new LocationFetch(getActivity().getApplicationContext());
        //start Async Task
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            Toast.makeText(getActivity().getApplicationContext(), "The user is: " + user.getEmail(), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "You are not logged in", Toast.LENGTH_LONG).show();
        }

        if(!hasPermission(getActivity().getApplicationContext(), Permissions)){
            requestPermissions(Permissions, ALL_PERMISSION);
            new LocationUpdateTask().execute();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);

        //use the location service
        pol_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapWithLocation("Police Station");
            }
        });

        hos_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapWithLocation("Hospital");
            }
        });
    }

    public void pointCurrentLocation(){
        loc_fetch.fetchCurrentLocation();
        lat = loc_fetch.fetchLatitude();
        lng = loc_fetch.fetchLongitude();

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        } else {
            if (lat != 0.0 && lng != 0.0) {
                centreMapOnLocation(lat, lng, "Your Current Location");
            }
        }
    }

    private void showGPSDisabledAlertToUser() {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(callGPSSettingIntent);
    }

    private void openMapWithLocation(String search_string) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        } else {
            loc_fetch.fetchCurrentLocation();
            lat = loc_fetch.fetchLatitude();
            lng = loc_fetch.fetchLongitude();

            //open the google map using the search string and latitude and longitude
            Uri googleMapUri = Uri.parse("geo: " + lat + ", " + lng + "?q=" + search_string + "");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    private static boolean hasPermission(Context context, String... Permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && Permissions != null){
            for(String permission : Permissions){
                if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ALL_PERMISSION){
            for(int i = 0; i<grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    hasPermission(getActivity().getApplicationContext(), Permissions);
                }
            }
        }
    }

    public void centreMapOnLocation(Double lat, Double lng, String title) {
        LatLng userLocation = new LatLng(lat, lng);
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18));
        gMap.getUiSettings().setMapToolbarEnabled(false);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        loc_fetch.fetchCurrentLocation();
        pointCurrentLocation();
        new LocationUpdateTask().execute();
    }

    private class LocationUpdateTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            while(loc_fetch.getUpdated() == false){
                ; //when the location is not updated, wait for sometimes
            }
            lat = loc_fetch.fetchLatitude();
            lng = loc_fetch.fetchLongitude();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pointCurrentLocation();
        }
    }
}
