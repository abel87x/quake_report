package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class QuakeAdapter extends ArrayAdapter<Quake> {

    private static final String LOCATION_SEPARATOR = "of";
    private Context mContext;


    public QuakeAdapter(Context context, ArrayList<Quake> dataArrayList) {
        super(context, 0, dataArrayList);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Quake currentItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.quake_item, parent, false);
        }

        // Displaying the magnitude on the screen as a string, formatting it to only show one decimal place
        // Also changing the background color of magnitude circle depending on magnitude value
        double originalNumberMagnitude = currentItem.getMagnitude();
        String stringMagnitude = formatMagnitude(originalNumberMagnitude);

        TextView magTextView = (TextView) convertView.findViewById(R.id.mag_txt); // Referring the the magnitude TextView in the Layout
        GradientDrawable magnitudeCircle = (GradientDrawable) magTextView.getBackground(); // Referring to the drawable circle used in the TextView

        magnitudeCircle.setColor(getMagCircleColor(originalNumberMagnitude)); // Changing the color of the circle
        magTextView.setText(stringMagnitude); // Setting the text in TextView

        // Splitting the location/place string into two parts/strings to be displayed in two TextViews
        // We could have used String.split(String string) method

       /* if (originalLocation.contains(LOCATION_SEPARATOR)) {
            String[] parts = originalLocation.split(LOCATION_SEPARATOR);
            locationOffset = parts[0] + LOCATION_SEPARATOR;
            primaryLocation = parts[1];
        } else {
            locationOffset = getContext().getString(R.string.near_the);
            primaryLocation = originalLocation;
        }*/

        // Instead, we used the following
        String originalLocation = currentItem.getLocation();
        TextView cityTextView = (TextView) convertView.findViewById(R.id.city_txt);
        TextView offsetTextView = (TextView) convertView.findViewById(R.id.orient_txt);
        if (originalLocation.contains(LOCATION_SEPARATOR)) {
            String locationOffset = originalLocation.substring(0, originalLocation.indexOf(LOCATION_SEPARATOR) + 2);
            String primaryLocation = originalLocation.substring(originalLocation.indexOf(LOCATION_SEPARATOR) + 2);

            cityTextView.setText(primaryLocation);
            offsetTextView.setText(locationOffset);
        } else {
            offsetTextView.setText(R.string.no_offset_found);
            cityTextView.setText(originalLocation);
        }


        // Converting the date and time formats and populating the respective TextViews
        long currentDateTime = currentItem.getDateTime();
        Date dateTimeObject = new Date(currentDateTime);

        TextView dateTextView = (TextView) convertView.findViewById(R.id.date_txt);
        dateTextView.setText(formatDate(dateTimeObject));

        TextView timeTextView = (TextView) convertView.findViewById(R.id.time_txt);
        timeTextView.setText(formatTime(dateTimeObject));

        // Returning the populated view to the ListView which is the parent view.
        return convertView;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM DD, YYYY");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }

    private String formatMagnitude(double decimal) {
        DecimalFormat formatter = new DecimalFormat("#.#");
        return formatter.format(decimal);
    }

    private int getMagCircleColor(double magnitude) {
        int magColorResourceId;
        int intMagnitude = (int) magnitude;
        switch (intMagnitude) {
            case 0:
            case 1:
                magColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magColorResourceId = R.color.magnitude9;
                break;
            default:
                magColorResourceId = R.color.magnitude10plus;
                break;
        }
        return ContextCompat.getColor(getContext(), magColorResourceId);
    }
}
