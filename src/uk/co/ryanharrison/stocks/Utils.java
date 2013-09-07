/**
 * Utils.java
 */

package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Various utility methods for the app
 * 
 * @author Ryan Harrison
 */
public class Utils {

  /** Read timeout for http requests in milliseconds */
  private static final int READTIMEOUT    = 25000;

  /** Connection timeout for http requests in milliseconds */
  private static final int CONNECTTIMEOUT = 20000;

  /**
   * Given a string representation of a URL, sets up a connection and gets and input stream from it
   * 
   * @param url
   *          The url to get a data stream from
   * @return An InputStream for the url which can be used to download data from the url
   * @throws IOException
   *           If there was an error connecting to the url
   */
  public static InputStream downloadUrl(String url) throws IOException {
    // Set up the http connection and set timeout values
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setReadTimeout(READTIMEOUT);
    conn.setConnectTimeout(CONNECTTIMEOUT);
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    // Starts the query
    conn.connect();
    return conn.getInputStream();
  }

  /**
   * Determines whether or not the context is in landscape or not
   * 
   * @param context
   *          The context to use when getting orientation information
   * @return True if the context is in landscape, otherwise false
   */
  public static boolean isInLandScape(Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  /**
   * Determine whether or not there is an active data network currently available
   * 
   * @param context
   *          Context to use when getting network information
   * @return True if there is an active network available, otherwise false
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  /**
   * Round a double value to two decimal places and return as a String
   * 
   * @param value
   *          The double value to round
   * @return A String of the value rounded to two decimal places
   */
  public static String roundTwoPlaces(double value) {
    return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
  }
}
