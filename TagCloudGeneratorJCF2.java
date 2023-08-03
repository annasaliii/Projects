import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Put a short phrase describing the program here.
 *
 * @author Put your name here
 *
 */
public final class TagCloudGeneratorJCF2 {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private static class StringLT
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int result = 0;
            if (o1.getKey().compareTo(o2.getKey()) == 0) {
                result = o2.getValue().compareTo(o1.getValue());
            } else {
                result = o1.getKey().compareTo(o2.getKey());
            }

            return result;
        }
    }

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private static class IntegerLT
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int result = 0;
            if (o1.getValue().compareTo(o2.getValue()) == 0) {
                result = o2.getKey().compareTo(o1.getKey());
            } else {
                result = o2.getValue().compareTo(o1.getValue());
            }

            return result;
        }
    }

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGeneratorJCF2() {
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        for (int i = 0; i < str.length(); i++) {

            if (!charSet.contains(str.charAt(i))) {
                charSet.add(str.charAt(i));
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int i = position;
        boolean isSet = separators.contains(text.charAt(i));
        while (i < text.length()
                && isSet == separators.contains(text.charAt(i))) {

            i++;

        }
        return text.substring(position, i);
    }

    /**
     * Returns a map with the right font size.
     *
     * @param q
     *            the queue with the ordered elements
     * @return a map with the word as the key and font size as the value
     *
     */

    private static Map<Map.Entry<String, Integer>, Integer> fontMap(
            PriorityQueue<Map.Entry<String, Integer>> q) {

        List<Map.Entry<String, Integer>> nq = new LinkedList<>();
        int max = 0;
        int min = 0;
        final int minFont = 11;
        final int n = 37;
        int temp = 0;
        Map<Map.Entry<String, Integer>, Integer> m = new HashMap<>();

        while (q.size() > 0) {
            Map.Entry<String, Integer> p = q.poll();
            if (p.getValue() > max) {
                max = p.getValue();
            } else if (p.getValue() < min) {
                min = p.getValue();
            }
            nq.add(p);
        }

        while (nq.size() > 0) {
            Map.Entry<String, Integer> p = nq.remove(0);
            if (min == max) {
                temp = minFont;
            } else {
                temp = n * (p.getValue() - min) / (max - min) + minFont;

            }
            q.offer(p);
            m.put(p, temp);
        }

        return m;
    }

    /**
     * Sorts the words from the map into a queue.
     *
     * @param wordCountMap
     *            map with the words and number of
     * @param n
     *            amount of elements in the map
     *
     * @return returns a queue with words sorted alphabetically
     */
    private static PriorityQueue<Map.Entry<String, Integer>> sortedWords(
            Map<String, Integer> wordCountMap, int n) {

        Comparator<Map.Entry<String, Integer>> ci = new IntegerLT();

        PriorityQueue<Map.Entry<String, Integer>> si = new PriorityQueue<>(ci);

        for (Map.Entry<String, Integer> p : wordCountMap.entrySet()) {
            si.add(p);
        }

        Comparator<Map.Entry<String, Integer>> ci2 = new StringLT();

        PriorityQueue<Map.Entry<String, Integer>> si2 = new PriorityQueue<>(
                ci2);

        for (int i = 0; i < n; i++) {
            Map.Entry<String, Integer> good = si.poll();
            si2.offer(good);
        }

        return si2;
    }

    /**
     * Gets all the terms from a queue into a map and then counts how many
     * appear in the text.
     *
     * @param wordCountMap
     *            the map that will hold the information
     * @param words
     *            the queue that holds the words from the text
     *
     */
    private static void wordCountMap(Map<String, Integer> wordCountMap,
            List<String> words) {

        while (words.size() > 0) {
            String key = words.remove(0);

            if (wordCountMap.containsKey(key)) {

                int val = wordCountMap.get(key);
                val++;
                wordCountMap.remove(key);
                wordCountMap.put(key, val);
            } else {
                wordCountMap.put(key, 1);
            }
        }

    }

    /**
     * Makes a Queue with all the terms from the file.
     *
     *
     * @param lines
     *            A queue that holds lines from the text file
     *
     * @return queue stores all individual words from each line
     */
    private static List<String> wordList(List<String> lines) {

        final String separatorStr = " \t\n\r,-.!?[]';:/()`*@#$%^&\"";
        Set<Character> separatorSet = new HashSet<>();
        generateElements(separatorStr, separatorSet);

        List<String> words = new LinkedList<>();
        while (lines.size() > 0) {
            String oneLine = lines.remove(0);
            int position = 0;
            while (position < oneLine.length()) {
                String token = nextWordOrSeparator(oneLine, position,
                        separatorSet);
                if (token.length() > 0
                        && !separatorSet.contains(token.charAt(0))) {

                    words.add(token);
                }
                position += token.length();
            }
        }
        return words;
    }

    /**
     * Prints the index page.
     *
     * @param fileOut
     *            output file name
     * @param inputFileName
     *            name of the original file
     * @param n
     *            amount of elements in the map
     * @param q
     *            the queue with the order elements
     */
    public static void printHeader(PrintWriter fileOut, String inputFileName,
            int n, PriorityQueue<Map.Entry<String, Integer>> q) {

        fileOut.println("<html>");
        fileOut.println("<head>");

        fileOut.println(
                "<title> Top " + n + " words in " + inputFileName + "</title>");

        fileOut.println(
                "<link href=\"http://www.cse.ohio-state.edu/software/2231"
                        + "/web-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        fileOut.println(
                "<link href = \"tagcloud.css\" rel = \"stylesheet\" type=\"text/css\">");
        fileOut.println("</head>");

        Map<Map.Entry<String, Integer>, Integer> m = fontMap(q);

        printBody(fileOut, q, n, inputFileName, m);

        fileOut.println("</html>");

    }

    /**
     * Prints the table with words and numbers.
     *
     * @param fileOut
     *            output file
     * @param q
     *            the queue with the order elements
     * @param inputFileName
     *            name of the original file
     * @param n
     *            amount of elements in the map
     * @param m
     *            map of words with its counts
     */

    public static void printBody(PrintWriter fileOut,
            PriorityQueue<Map.Entry<String, Integer>> q, int n,
            String inputFileName, Map<Map.Entry<String, Integer>, Integer> m) {

        fileOut.println("<body>");
        fileOut.println(
                "<h2> Top " + n + " words in " + inputFileName + "</h2>");
        fileOut.println("<hr>");

        fileOut.println("<div class= \"cdiv\">");

        fileOut.println("<p class=\"cbox\">");

        while (q.size() > 0) {
            Map.Entry<String, Integer> p = q.poll();
            fileOut.println("<span style=\"cursor:default\" class=\"f"
                    + m.get(p) + "\" title=\"count: " + p.getValue() + "\">"
                    + p.getKey() + "</span>");
        }
        fileOut.println("</p>");
        fileOut.println("</div>");
        fileOut.println("</body>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        /*
         * Put your main program code here
         */
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.print("Enter the input file name: ");
        String inputFileName = in.readLine();
        BufferedReader input;

        try {
            input = new BufferedReader(new FileReader(inputFileName));

        } catch (IOException e) {
            System.err.println("Error opening file");
            return;
        }

        List<String> lines = new LinkedList<>();

        try {
            String oneLine = input.readLine();
            while (oneLine != null) {
                String str = oneLine.toLowerCase();
                lines.add(str);
                oneLine = input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading from file");

        }

        List<String> words = wordList(lines);

        Map<String, Integer> wordCountMap = new HashMap<>();
        wordCountMap(wordCountMap, words);

        int n = 0;
        while (n > wordCountMap.size() || n <= 0) {
            System.out.print("Enter a positive integer N: ");
            String num = in.readLine();

            try {
                n = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                System.err.println("Error: Positive number");
                return;
            }

        }

        System.out.print("Enter the output file name: ");
        String folderName = in.readLine();
        PrintWriter outputFile;
        try {
            outputFile = new PrintWriter(new FileWriter(folderName));
        } catch (IOException e) {
            System.err.print("Error opening file");
            return;
        }

        PriorityQueue<Map.Entry<String, Integer>> sortedWords = sortedWords(
                wordCountMap, n);

        printHeader(outputFile, inputFileName, n, sortedWords);

        try {
            in.close();
            outputFile.close();
            input.close();
        } catch (IOException e) {
            System.err.println("Error closing file");
            return;
        }

    }

}
