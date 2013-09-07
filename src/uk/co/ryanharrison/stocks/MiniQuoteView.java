/**
 * MiniQuoteView.java
 */

package uk.co.ryanharrison.stocks;

import uk.co.ryanharrison.stocks.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Custom view used to display a single quote in a gridview. Only the main pieces of data are displayed and the backgrond changes
 * colour depending on the price change
 * 
 * @author Ryan Harrison
 */
public class MiniQuoteView extends LinearLayout {

  /** The quote object that this view will display */
  private Quote    quote;

  /** Company textview */
  private TextView companyText;

  /** Ticker textview */
  private TextView tickerText;

  /** Price textview */
  private TextView priceText;

  /** Exchange textview */
  private TextView exchangeText;

  /** Market capitalisation textview */
  private TextView marketCapText;

  /** Change textview */
  private TextView changeText;

  /** Percent change textview */
  private TextView percentChangeText;

  /** The paint used to draw a border around the view */
  private Paint    borderPaint;

  /**
   * Create a new MiniQuoteView with specified context and set of attributes
   * 
   * @param context
   *          The context that this view will display in
   * @param attrs
   *          The attributes that this view will have. Used by xml layouts
   */
  public MiniQuoteView(Context context, AttributeSet attrs) {
    super(context, attrs);

    LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.miniquoteview, this);

    // Get views from layout
    this.getViewsFromLayout();
    this.fillData();
  }

  /**
   * Construct a new MiniQuoteView with specified context and quote object to display
   * 
   * @param context
   *          The context that this view will display in
   * @param quote
   *          The quote object that this view will display
   */
  public MiniQuoteView(Context context, Quote quote) {
    super(context);
    this.quote = quote;

    // Set the layout for this view from the xml file
    LayoutInflater inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.miniquoteview, this);

    // Get views from layout
    this.getViewsFromLayout();

    // Fill the UI views with the data from the quote object
    this.fillData();

    // Set up the paint used to draw the border
    this.borderPaint = new Paint();
    this.borderPaint.setColor(Color.BLACK);
    this.borderPaint.setStrokeWidth(4.0f);
    this.borderPaint.setStyle(Style.STROKE);
  }

  /** Fill in the UI views with data from the quote object */
  private void fillData() {
    // If the quote has no data, then display a not available message only
    if (this.quote == null) {
      this.companyText.setText(this.getResources().getString(R.string.not_available));
      this.tickerText.setText(this.getResources().getString(R.string.not_available));
      return;
    }

    this.tickerText.setText(this.quote.ticker == null ? "" : this.quote.ticker);

    // If the name field is not null then we have valid data to display
    if (this.quote.name != null) {
      this.companyText.setText(this.quote.name);
      this.priceText.setText(this.quote.lastTradePrice + "");
      this.exchangeText.setText("  (" + this.quote.stockExchange + ")");
      this.marketCapText.setText(this.quote.marketCapitalization.equals("null") ? this.getResources().getString(
          R.string.not_available) : this.quote.marketCapitalization);

      double change = this.quote.change;

      // If the dayslow field is not zero then the market has opened for trading so we can display the price change data
      if (this.quote.daysLow != 0) {
        // Set the background and text depending on the sign of the price change field
        if (change < 0) {
          // Red for a negative change and a down arrow for the text
          this.setBackgroundColor(this.getResources().getColor(R.color.lightred));
          this.changeText.setText(this.getResources().getString(R.string.down_arrow) + Utils.roundTwoPlaces(this.quote.change));
        }
        else {
          // Green for a positive change and an up arrow for the text
          this.setBackgroundColor(this.getResources().getColor(R.color.lightgreen));
          this.changeText.setText(this.getResources().getString(R.string.up_arrow) + Utils.roundTwoPlaces(this.quote.change));
        }

        this.percentChangeText.setText("  (" + Utils.roundTwoPlaces(this.quote.percentChange) + "%)");
      }
      // Otherwise inform the user that the market is closed for trading
      else {
        this.changeText.setText(this.getResources().getString(R.string.market_closed));
      }
    }
  }

  /**
   * Get views from layout and initialise view fields
   */
  private void getViewsFromLayout() {
    this.companyText = (TextView) this.findViewById(R.id.miniCompanyTextView);
    this.tickerText = (TextView) this.findViewById(R.id.miniTickerTextView);
    this.priceText = (TextView) this.findViewById(R.id.miniPriceTextView);
    this.exchangeText = (TextView) this.findViewById(R.id.miniExchangeTextView);
    this.marketCapText = (TextView) this.findViewById(R.id.miniMarketCapTextView);
    this.changeText = (TextView) this.findViewById(R.id.miniChangeTextView);
    this.percentChangeText = (TextView) this.findViewById(R.id.miniPercentChangeTextView);
  }

  /**
   * Override the draw method to add a black border around the view
   * 
   * @param canvas
   *          The canvas to draw on
   * 
   * @see android.widget.LinearLayout#onDraw(android.graphics.Canvas)
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), this.borderPaint);
  }
}
