package com.shijil.locationpicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.shijil.library.LocationActivity;

public class MainActivity extends LocationActivity {

    private AutoCompletetwithClear mAutocompleteView;

    private TextView txt_address;

    private ProgressDialog progressDialog;

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    @Override
    protected void updateUIWidgets(boolean loading) {
        if(progressDialog==null)return;
        if (loading) {
           // showProgress();
            progressDialog.show();
        } else {
            //hideProgress();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onReceiveLocation(String mCityOutput,String mAddressOutput, String mLatitudeOutput, String mLongitudeOutput) {

        String text="<p>"+mCityOutput+", Latitude:"+mLatitudeOutput+" Longitude:"+mLongitudeOutput+
                "</p>"+mAddressOutput+"</p>";
        txt_address.setText(Html.fromHtml(text));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog=new ProgressDialog(this);
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompletetwithClear)findViewById(R.id.txt_auto_place);
        txt_address= (TextView) findViewById(R.id.txt_address);

        findViewById(R.id.txt_setloc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickDeviceCurrentLocation();
            }
        });

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(this);
        mAutocompleteView.setAdapter(getPlaceAutocompleteAdapter());
    }


}
