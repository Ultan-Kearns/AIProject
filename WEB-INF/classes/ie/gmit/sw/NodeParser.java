package ie.gmit.sw;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/*
 * This class will traverse the graph of nodes and get the most relevant terms that 
 * are found in each webpage.  These terms will be added to the wordcloud if they are 
 * frequent which will be determined by the Heuristics
 */
public class NodeParser {
	// change these only template best first search
	// Code was adapted from Assignment workshop by Dr. John Healy GMIT
	private static final int MAX = 20;
	private static final int TITLE_WEIGHT = 100;
	private static final int HEADING_WEIGHT = 20;
	private static final int PARAGRAPH_WEIGHT = 1;
	private String title;
	private String term;
	// for
	private Set<String> closed = new ConcurrentSkipListSet();
	// get comparator
	private Queue<DocumentNode> q = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));
	static Map<String, Integer> map = new ConcurrentHashMap<>();
	/**
	 * 
	 * @param url
	 * @param searchTerm
	 * @throws IOException
	 */
	public NodeParser(String url, String searchTerm) throws IOException {
		super();
		this.term = searchTerm;
		// https://duckduckgo.com/html/?q= works
		System.out.println("URL : " + url + "  CHILD  " + searchTerm);
		Document doc = Jsoup.connect(url + searchTerm).get();
		int score = getHeuristicScore(doc);
		// for tracking
		closed.add(url);
		// put new document on queue
		q.offer(new DocumentNode(doc, score));
		process();

		// TODO Auto-generated constructor stub
	}
	/**
	 * processes the list
	 */
	public void process() {
		while (!q.isEmpty() && closed.size() <= MAX) {
			DocumentNode node = q.poll();
			Document doc = node.getDocument();
			Elements edges = doc.select("a[href]"); // a with href
			for (Element e : edges) {
				// absolute URL
				String link = e.absUrl("href");

				if (!closed.contains(link) && closed.size() <= MAX && link != null) {
					Document child;
					try {
						closed.add(link);
						child = Jsoup.connect(link).get();
						int score = getHeuristicScore(child);
						System.out.println("CHILD TITLE: " + child.title());
						q.offer(new DocumentNode(child, score));
					} catch (IOException e1) {
					}
				}
			}
		}

	}
	/**
	 * 
	 * @param text
	 * @return
	 * this function will return a map of the text
	 */
	private static Map index(String... text) {
		for (String s : text) {
			int i = 0;
			// extact each word from string and add to map after filtering with ignore
			// words(TreeSet)
			map.put(s, i);
			i++;
		}
		return map;
	}

	private class DocumentNode {
		private Document document;
		private int score;

		public DocumentNode(Document d, int score) {
			super();
			this.document = d;
			this.score = score;
		}

		public Document getDocument() {
			return document;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

	}
	/**
	 * 
	 * @param s
	 * @return
	 * Uses regular expressions to detect for patterns in text
	 */
	private int getFrequency(String s) {
		s = s.toLowerCase();
		int i = 0;
		// Use regex to detect term in string s - ref : https://stackoverflow.com/questions/22566503/count-the-number-of-occurrences-of-a-word-in-a-string
		Pattern searchPattern = Pattern.compile(term);
		Matcher findMatch = searchPattern.matcher(s);
		while (findMatch.find()) {
		    i++;
		}
		return i;
	}
	/**
	 * 
	 * @param d
	 * @return
	 * @throws IOException
	 * Scores the heuristic to see the occurence of the term in title, body and text, if it passes a certain threshold it will be 
	 * scored by fuzzy heuristic function
	 */
	private int getHeuristicScore(Document d) throws IOException {
		int titleScore = 0,headingScore = 0,bodyScore = 0;
		String title = d.title();
		System.out.println("TITLE" + d.title());
		titleScore = getFrequency(title) * TITLE_WEIGHT;
		Elements headings = d.select("h1");
		for(Element heading : headings) {
			String h1 = heading.text();
			headingScore += getFrequency(h1) * HEADING_WEIGHT;
			System.out.println("HEADING --> " + h1);
		}
		
		System.out.println(closed.size() + " ---> " + title);
	 
 
		Elements paragraphs = d.select("p");
		try {
			StringBuffer bodyText = new StringBuffer();
		for(Element paragraph : paragraphs) {
			bodyText.append(paragraph.text());
					//need to iterate over body and search for the most common strings in body
		}
		bodyScore = getFrequency(bodyText.toString()) * PARAGRAPH_WEIGHT;

		}
		catch(Exception e) {
			
		}
		if(titleScore + headingScore + bodyScore > 5) {
			getFuzzyHeuristic(titleScore, headingScore, bodyScore);
		}
		else {
			System.out.println("irrelevant");
		}
		return titleScore + headingScore + bodyScore;
		
	}

	// INCLUDE JFUZZY LOGIC CODE HERE
	// http://jfuzzylogic.sourceforge.net/html/index.html
	/**
	 * 
	 * @param title
	 * @param heading
	 * @param body
	 * @return
	 * This function gets the fuzzy heuristic score of the search, it loads the FCL file and scores the input 
	 * according to rules defined in the FCL file.
	 */
	private int getFuzzyHeuristic(int title, int heading, int body) {
		// load fuzzy inference systems in here
		FIS fis = FIS.load("./res/Frequency.fcl", true);
		fis.setVariable("title", title);
		fis.setVariable("heading", heading);
		fis.setVariable("body", body);
		fis.evaluate();
		System.out.println("HEding = " + heading + " TITLE = " + title + "body = " + body);
		FunctionBlock fb = fis.getFunctionBlock("frequency");
		// rule example if title is significant and headings is relevant and body is
		// frequent then score is high
		Variable frequency = fb.getVariable("relevance");
		// if(fuzzy score is high then call index on the title, headings and body)
		System.out.println("SCORE : " + frequency);
		// need to index index(title,heading,body);
		// then return result
		return 1;
	}

	public static void main(String[] args) throws IOException {
		NodeParser p = new NodeParser("https://duckduckgo.com/html/?q=", "test");
		p.process();
	}
}
