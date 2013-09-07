/**
 * Quote.java
 */

package uk.co.ryanharrison.stocks;

import java.io.Serializable;

/**
 * A wrapper class holding information about a quote for a particular stock
 * 
 * @author Ryan Harrison
 */
class Quote implements Serializable {

  /** Serial version id required when implementing Serializable */
  private static final long serialVersionUID = 1328189913783422102L;

  /** The average number of shares that are traded daily */
  public long               averageDailyVolume;

  /** The change in price of the stock in the current day of trading */
  public double             change;

  /** The percentage change in price of the stock in the current day of trading */
  public double             percentChange;

  /** The lowest price of the stock in the current day of trading */
  public double             daysLow;

  /** The highest price of the stock in the current day of trading */
  public double             daysHigh;

  /** The lowest price of the stock in the current year of trading */
  public double             yearLow;

  /** The highest price of the stock in the current year of trading */
  public double             yearHigh;

  /** The market capitalisation value of the stock */
  public String             marketCapitalization;

  /** The current price of the stock */
  public double             lastTradePrice;

  /** The full name of the stock */
  public String             name;

  /** The ticker symbol of the stock */
  public String             ticker;

  /** The amount of shares that have been traded in the current day of trading */
  public long               volume;

  /** The stock exchange that the stock belongs to */
  public String             stockExchange;
}
