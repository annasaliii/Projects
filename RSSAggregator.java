import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Put your name here
 *
 */
public final class RSSAggregator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        int title = getChildElement(channel, "title");
        int link = getChildElement(channel, "link");
        int descrip = getChildElement(channel, "description");

        String titleElement = "No Title Available";

        String descripElement = "No Description Available";

        String linkElement = channel.child(link).child(0).label();

        if (title > -1 && channel.child(title).numberOfChildren() > 0) {
            titleElement = channel.child(title).child(0).label();
        }

        if (descrip > -1 && channel.child(descrip).numberOfChildren() > 0) {
            descripElement = channel.child(descrip).child(0).label();
        }
        out.println(
                "<html> <head> <title>" + titleElement + "</title> </head> ");

        out.println("<body> <h1> <a href=\" " + linkElement + " \"> "
                + titleElement + "</a> </h1>");

        out.println("<p>" + descripElement + "</p> <table border=\"1\" ");
        out.println("<tr> <th>Date</th> ");
        out.println(" <th>Source</th> ");
        out.println(" <th>News</th> </tr>");

    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("</table> </body> </html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        /*
         * TODO: fill in body
         */
        int index = -1;
        for (int i = 0; i < xml.numberOfChildren(); i++) {
            if (xml.child(i).isTag() && xml.child(i).label().equals(tag)) {
                index = i;
            }
        }
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        int date = getChildElement(item, "pubDate");

        int source = getChildElement(item, "source");

        int title = getChildElement(item, "title");

        int descrip = getChildElement(item, "description");

        int link = getChildElement(item, "link");

        if (date > -1) {
            out.println(
                    "<tr> <td> " + item.child(date).child(0).label() + "</td>");
        } else {
            out.println("<tr> <td>No Date Available</td> ");
        }

        if (source > -1) {
            out.println("<td> <a href=\" "
                    + item.child(source).attributeValue("url") + "\">"
                    + item.child(source).child(0).label() + "</a></td>");
        } else {
            out.println("<td>No Source Available</td>");
        }
        String titleElement = "No Title Available";
        if (title > -1 && item.child(title).numberOfChildren() > 0) {
            titleElement = item.child(title).child(0).label();

        } else if (descrip > -1 && item.child(descrip).numberOfChildren() > 0) {
            titleElement = item.child(descrip).child(0).label();
        }

        if (link > -1) {
            out.println("<td> <a href=\" " + item.child(link).child(0).label()
                    + "\">" + titleElement + "</a> </td>");
        } else if (link == -1) {
            out.println("<td>" + titleElement + "</td>");
        }

    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file, SimpleWriter out) {
        XMLTree xml = new XMLTree1(url);
        XMLTree channel = xml.child(0);
        out.println("Processing " + file);
        if (xml.label().equals("rss") && xml.hasAttribute("version")
                && xml.attributeValue("version").equals("2.0")) {

            SimpleWriter outputFile = new SimpleWriter1L(file);

            outputHeader(channel, outputFile);

            for (int i = 0; i < channel.numberOfChildren(); i++) {

                if (channel.child(i).isTag()
                        && channel.child(i).label().equals("item")) {

                    processItem(channel.child(i), outputFile);
                }
            }

            outputFooter(outputFile);
        }

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        out.print("Enter the name of a RSS 2.0 feed file: ");
        String file = in.nextLine();

        XMLTree xml = new XMLTree1(file);
        SimpleWriter outputFile = new SimpleWriter1L(
                xml.attributeValue("title") + ".html");
        outputFile.println("<html> <head> <title>" + xml.attributeValue("title")
                + "</title> </head> ");
        outputFile
                .println("<body> <h2>" + xml.attributeValue("title") + "</h2>");
        outputFile.println("<ul>");

        for (int i = 0; i < xml.numberOfChildren(); i++) {
            String url2 = xml.child(i).attributeValue("url");
            String file1 = xml.child(i).attributeValue("file");
            String name = xml.child(i).attributeValue("name");

            outputFile.println(
                    "<li> <a href=\"" + file1 + "\">" + name + "</a> </li>");

            processFeed(url2, file1, out);

        }
        outputFile.println("</ul> </body> </html>");
        in.close();
        out.close();
        outputFile.close();
    }

}