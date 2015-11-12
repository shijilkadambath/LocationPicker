package com.shijil.library;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


import java.util.Arrays;
import java.util.List;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.shijil.library.adapter.PlaceAutocompleteAdapter;
import com.shijil.library.utils.AppConstants;

/**
 * Created by shijil on 11/10/2015.
 */
public abstract class LocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,AdapterView.OnItemClickListener{


    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    /**
     * GoogleApiClient wraps our service connection to Google Play Services and provides access
     * to the user's sign in state as well as the Google's APIs.
     */
    private GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;
    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered. The user requests an
     * address by pressing the Fetch Address button. This may happen before GoogleApiClient
     * connects. This activity uses this boolean to keep track of the user's intent. If the value is
     * true, the activity tries to fetch the address as soon as GoogleApiClient connects.
     */
    private boolean mAddressRequested;
    private boolean mButtonClicked;
    /**
     * The formatted location address.
     */
    private String mAddressOutput;
    private String mCityOutput;
    private String mLatitudeOutput;
    private String mLongitudeOutput;

    private List<String> addressList;
    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    private PlaceAutocompleteAdapter mAdapter;



    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));


    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    protected abstract void updateUIWidgets(boolean loading);

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    protected abstract void onReceiveLocation(String mCityOutput,String mAddressOutput,String mLatitudeOutput,
            String mLongitudeOutput);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();



        mResultReceiver = new AddressResultReceiver(new Handler());

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);


        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;
        mButtonClicked=false;
        mAddressOutput = "";
        mLatitudeOutput="";
        mLongitudeOutput="";
        mCityOutput="";
        updateValuesFromBundle(savedInstanceState);

        updateUIWidgets(mAddressRequested);
        // buildGoogleApiClient();

    }
    public void pickDeviceCurrentLocation() {
        mButtonClicked=true;
        fetchAddressButtonHandler();
    }
    public PlaceAutocompleteAdapter getPlaceAutocompleteAdapter() {
        return mAdapter;
    }

    /**
     * Runs when user clicks the Fetch Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    private void fetchAddressButtonHandler() {
        // We only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
            // showToast("connetcted");
            mAddressRequested = true;
            updateUIWidgets(mAddressRequested);
        }else {
            showToast("Please turn on the location service");
        }
        // If GoogleApiClient isn't connected, we process the user's request by setting
        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.

    }

    /**
     * Updates the address.
     */
    protected void displayAddressOutput() {
        if (mButtonClicked){
            // Toast.makeText(this,mAddressOutput,Toast.LENGTH_LONG).show();
            onReceiveLocation(mCityOutput, mAddressOutput, mLatitudeOutput,mLongitudeOutput);
        }
    }
    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }
    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(AppConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(AppConstants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }
    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
        final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        //Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        addressList = Arrays.asList(String.valueOf(item.description).split(","));

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, placeId);
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

           /* Toast.makeText(getApplicationContext(), "Clicked: " + item.description,
                    Toast.LENGTH_SHORT).show();*/
        // Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
    }
    /*private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };*/

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("place", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            mLatitudeOutput=String.valueOf(place.getLatLng().latitude);
            mLongitudeOutput=String.valueOf(place.getLatLng().longitude);
            mAddressOutput=String.valueOf(place.getAddress());
            if (addressList==null){

                mCityOutput=String.valueOf(place.getName());
            }else {
                if(addressList.size()>3){
                    mCityOutput=addressList.get(addressList.size()-3);
                }else {
                    mCityOutput=String.valueOf(place.getName());
                }

            }



            // Toast.makeText(LocationActivity.this,"|"+mAddressOutput,Toast.LENGTH_LONG).show();
            //place.getLatLng().latitude
            places.release();
            onReceiveLocation(mCityOutput,mAddressOutput, mLatitudeOutput,mLongitudeOutput);
        }
    };
    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("place", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this, getString(R.string.nonetwork) + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.nonetwork, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                startIntentService();
            }
            // fetchAddressButtonHandler();
        }
    }
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        // Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);

        // Save the address string.
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            //showToast("At Reciver");
            // Show a toast message if an address was found.
            if (resultCode == AppConstants.SUCCESS_RESULT) {

                // Display the address string or an error message sent from the intent service.
                mCityOutput = resultData.getString(AppConstants.RESULT_DATA_KEY);
                mAddressOutput = resultData.getString(AppConstants.RESULT_DATA_KEY2);
                mLatitudeOutput= resultData.getString(AppConstants.RESULT_LAT_KEY);
                mLongitudeOutput= resultData.getString(AppConstants.RESULT_LONG_KEY );
                displayAddressOutput();
                //showToast(getString(com.newagesmb.socialbee.R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
            updateUIWidgets(mAddressRequested);
        }
    }
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

