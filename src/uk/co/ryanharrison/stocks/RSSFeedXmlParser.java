/**
 * RSSFeedXmlParser.java
 */

package uk.co.ryanharrison.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * Class to parse an RSS feed xml file into a Map of link names to urls
 * 
 * @author Ryan Harrison
 */
public class RSSFeedXmlParser {

  /**
   * Wrapper class for an RSS item holding the title of the item and its url
   * 
   * @author Ryan Harrison (rh00148)
   */
  private static class RSSItem {

    /** The title of the rss item */
    public final String title;

    /** The url of the rss item */
    public final String link;

    /**
     * Construct a new RSSItem with the specified title and url
     * 
     * @param title
     *          The title of the item
     * @param link
     *          THe url of the item
     */
    public RSSItem(String title, String link) {
      this.title = title;
      this.link = link;
    }
  }

  /**
   * Parse an RSS feed xml file into a Map of link names to their corresponding urls
   * 
   * @param in
   *          The inputstream for the rss xml file
   * @return A Map of RSS link names to their urls
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  public Map<String, String> parse(InputStream in) throws XmlPullParserException, IOException {
    try {
      // Set up the xml parser and make it point to the rss file input stream
      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(in, null);
      parser.nextTag();

      // Read and parse the xml file
      return this.readFeed(parser);
    }
    finally {
      // Make sure to close the input stream in all cases
      if (in != null) {
        in.close();
      }
    }
  }

  /**
   * Read the RSS feed xml file and parse each element we are interested in
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @return A Map of RSS link names to their urls
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private Map<String, String> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    // LinkedHashMap used to maintain the order of the RSS entries
    Map<String, String> entries = new LinkedHashMap<String, String>();

    parser.nextTag();
    // Continue parsing until we have reached the end of the file
    while (parser.next() != XmlPullParser.END_TAG) {
      // Skip over anything that is not a start element tag
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }

      // Get the name of the current start tag
      String name = parser.getName();
      // We only want to parse the data inside the item tag
      if (name.equals("item")) {
        // Parse the item and add it to the result map
        RSSItem e = this.readRssItem(parser);
        entries.put(e.title, e.link);
      }
      else {
        // Otherwise we can skip the tag as we are not interested in it
        this.skip(parser);
      }
    }
    return entries;
  }

  /**
   * Read and process the link tag and get the link url out of the file as a String
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @return The link url of the rss item
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
    // Assert that the current tag is a starting link tag
    parser.require(XmlPullParser.START_TAG, null, "link");
    // Now we can safely read the text inside the link tag
    String link = this.readText(parser);
    // Assert that the current tag is an ending link tag
    parser.require(XmlPullParser.END_TAG, null, "link");
    return link;
  }

  /**
   * Parses the contents of an item entry. If we encounters a title or link tag, hand them off to other read methods for parsing.
   * Otherwise we can skip the content
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @return An RSSItem object corresponding to the data held in the item tag
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private RSSItem readRssItem(XmlPullParser parser) throws XmlPullParserException, IOException {
    // Assert that the current tag is a starting item tag
    parser.require(XmlPullParser.START_TAG, null, "item");
    String title = null;
    String link = null;

    // While there are more elements in the tags
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }

      // Get the name of the current start tag
      String name = parser.getName();

      // If it is a title tag, read it and set the variable
      if (name.equals("title")) {
        title = this.readTitle(parser);
      }
      // If it is a link tag, read it and set the variable
      else if (name.equals("link")) {
        link = this.readLink(parser);
      }
      else {
        // Otherwise we can skip the tag as we are not interested in it
        this.skip(parser);
      }
    }

    // Return the RSSItem object with the title and url we read
    return new RSSItem(title, link);
  }

  /**
   * Extract the text inside the current XML tag of the parser
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @return The text inside the current xml tag as a String
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    String result = "";
    // If the next element is valid text
    if (parser.next() == XmlPullParser.TEXT) {
      // Get the text from the tag
      result = parser.getText();
      parser.nextTag();
    }
    return result;
  }

  /**
   * Read and process the title tag and get the title out of the file as a String
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @return The title of the rss item
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
    // Assert that the current tag is a starting title tag
    parser.require(XmlPullParser.START_TAG, null, "title");
    // Now we can safely read the text inside the title tag
    String title = this.readText(parser);
    // Assert that the current tag is an ending title tag
    parser.require(XmlPullParser.END_TAG, null, "title");
    return title;
  }

  /**
   * Skip over the current xml tag in the parser
   * 
   * @param parser
   *          The parser object set up with the xml file
   * @throws XmlPullParserException
   *           If their was an error parsing the rss xml file
   * @throws IOException
   *           If there was an error downloading the rss xml file
   */
  private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    // We cannot skip over anything if the current tag is not a starting tag
    if (parser.getEventType() != XmlPullParser.START_TAG) {
      throw new IllegalStateException();
    }

    // Initially we have a depth of one as we want to skip over just the current tag
    int depth = 1;

    // Keep moving onto the next tag until we have skipped over the current one
    while (depth != 0) {
      switch (parser.next()) {
      // Decrease the depth if we have reached an end tag
        case XmlPullParser.END_TAG:
          depth--;
          break;
        // Increase the depth if we have reached a start tag
        case XmlPullParser.START_TAG:
          depth++;
          break;
      }
    }
  }
}