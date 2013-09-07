package uk.co.ryanharrison.stocks;

import uk.co.ryanharrison.stocks.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * Activity to display an about application message to the user
 * 
 * @author Ryan Harrison
 */
public class AboutActivity extends Activity {

  /** Tag for this activity */
  public static final String TAG = "About";

  /**
   * Called when the activity is created. Simply set the content view to the layout resource to display the about dialog
   * 
   * @param savedInstanceState
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set the layout of this activity
    this.setContentView(R.layout.activity_about);
  }
}
