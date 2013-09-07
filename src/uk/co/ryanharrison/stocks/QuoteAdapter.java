/**
 * ImageAdapter.java
 */

package uk.co.ryanharrison.stocks;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

/**
 * A custom adapter class that creates MiniQuoteViews for each Quote that is being held by this adapter The adapter can then be used
 * in other views to display the quotes in the UI
 * 
 * @author Ryan Harrison
 */
public class QuoteAdapter extends BaseAdapter {

  /** The context that this adapter will work in */
  private Context     context;

  /** The list of quotes that this adapter will create views for */
  private List<Quote> quotes;

  /**
   * Create a new QuoteAdapter with the specified context and list of quotes to work with
   * 
   * @param c
   *          The context that this adapter will work in
   * @param quotes
   *          The list of quotes that this adapter will create views for
   */
  public QuoteAdapter(Context c, List<Quote> quotes) {
    this.context = c;
    this.quotes = quotes;
  }

  /**
   * Add a new Quote to the set of items that this adapter creates views for
   * 
   * @param q
   *          The quote to add to the set of items that this adapter creates views for
   */
  public void addQuote(Quote q) {
    this.quotes.add(q);
  }

  /**
   * Get the number of items in the data set that this adapter creates views for
   * 
   * @return The size of the data set that this adapter creates views for
   * 
   * @see android.widget.Adapter#getCount()
   */
  @Override
  public int getCount() {
    return this.quotes.size();
  }

  /**
   * Get the item in the underlying data set at the specified position
   * 
   * @param position
   *          The position of the item to get
   * @return The item in the data set at the specified position
   * 
   * @see android.widget.Adapter#getItem(int)
   */
  @Override
  public Object getItem(int position) {
    return this.quotes.get(position);
  }

  /**
   * Get the row id associated with the specified position in the list.
   * 
   * @param position
   *          The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   * 
   * @see android.widget.Adapter#getItemId(int)
   */
  @Override
  public long getItemId(int position) {
    // We can just return a default value for this adapter
    return 0;
  }

  /**
   * Get a custom MiniQuoteView that displays the data at the specified position in the data set
   * 
   * @param position
   *          The position of the item within the adapter's data set of the item whose view we want.
   * @param convertView
   *          The old view to reuse, if possible
   * @param parent
   *          The parent that this view will eventually be attached to
   * @return A View corresponding to the data at the specified position.
   * 
   * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    MiniQuoteView miniQuoteView = new MiniQuoteView(this.context, this.quotes.get(position));
    miniQuoteView.setLayoutParams(new GridView.LayoutParams(220, 220));
    miniQuoteView.setPadding(4, 4, 4, 4);

    return miniQuoteView;
  }

  /**
   * Remove the item in the underlying data set at the specified position
   * 
   * @param position
   *          The position of the item to remove
   * */
  public void removeItem(int position) {
    this.quotes.remove(position);
  }
}
