# LocationPicker
LocationPicker library using google place api
Simple Android libarary for picking a location using google location api.Pick location from device current location or from the google place suggestions list.


# Usage :
  1. Extended your Activity with LocationActivity
  2. Manage progress view in overriding methode protected void      	updateUIWidgets(boolean loading).

  3. To get current location call methode pickDeviceCurrentLocation();
   4. To get google place suggestion list in your 	AutoCompleteTextView set adapter as

mAutocompleteView.setOnItemClickListener(this);
mAutocompleteView.setAdapter(getPlaceAutocompleteAdapter());


   5. get current location and selected loaction in suggestion list from overriding methode void onReceiveLocation(String mCityOutput,String mAddressOutput, String mLatitudeOutput, String mLongitudeOutput)



	
	Don't forget to add your google location api key in the string file
#License
 	Copyright 2015 Shijil kadambath

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
