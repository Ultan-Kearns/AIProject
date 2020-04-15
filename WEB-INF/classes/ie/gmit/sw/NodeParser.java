package ie.gmit.sw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
/*
 * NEED TO DO: 
 * 1. Get most occurring words in html body, title, text(Done)
 * 2. Get distance between strings in words
 * 3. Use BFS, DFS, or Breadth First Search when navigating tree or graph(Done - depth first search_
 * 4. Get output to image
 */
public class NodeParser {
	// change these only template best first search
	// Code was adapted from Assignment workshop by Dr. John Healy GMIT
	protected static final int MAX = 50;
	private static final int TITLE_WEIGHT = 100;
	private static final int HEADING_WEIGHT = 20;
	private static final int PARAGRAPH_WEIGHT = 1;
 	private String term;
 	// for
	private Set<String> closed = new ConcurrentSkipListSet<String>();
	// get comparator
	//Should be thread safe since each thread maintains its own list
	private List<DocumentNode> q = Collections.synchronizedList(new LinkedList<DocumentNode>());
	Map<String, Integer> map = new ConcurrentHashMap<>();
	//read in ignore words
	StringBuffer ignoreWords = ignore();
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
		// put new document at end of list 
		q.add(new DocumentNode(doc, score));
 

		// TODO Auto-generated constructor stub
	}
	/**
	 * processes the list
	 */
	public void process() {
		while (!q.isEmpty() && closed.size() <= MAX) {
			DocumentNode node = q.get(q.size() - 1);
			Document doc = node.getDocument();
			Elements edges = doc.select("a[href]"); // a with href
			for (Element e : edges) {
				// absolute URL
				String link = e.absUrl("href");

				if (!closed.contains(link) && closed.size() <= MAX && link != null) {
					Document child;
					try {
						closed.add(link);
						//fails here if test is query
						child = Jsoup.connect(link).get();
						int score = getHeuristicScore(child);
						System.out.println("CHILD TITLE: " + child.title());
						if(score > 5) {
							q.add(new DocumentNode(child, score));
						}
					} catch (IOException e1) {
						System.out.println("CAUGHT");
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
	private void index(String text,int frequency) {
		//put word and number of occurrences into map only need 20 words may change for options
		if(map.size() < 20 && 	ignoreWords.toString().contains(text) == false) {
		map.put(text,frequency); 
		} 
	}

	private class DocumentNode {
		private Document document;
		//says it's unused but is used and the warnings are annoying.
		@SuppressWarnings("unused")
		private int score;

		public DocumentNode(Document d, int score) {
			super();
			this.document = d;
			this.score = score;
		}

		public Document getDocument() {
			return document;
		}

	}
	/**
	 * 
	 * @param s
	 * @return
	 * Uses regular expressions to detect for patterns in text
	 */
	private int getFrequency(String word,String textBody) {
		// get both strings to lower
		word = word.toLowerCase();
		textBody = textBody.toLowerCase();
		int occurrence = 0;
		// Use regex to detect s - ref : https://stackoverflow.com/questions/22566503/count-the-number-of-occurrences-of-a-word-in-a-string
		Pattern searchPattern = Pattern.compile(word);
		Matcher findMatch = searchPattern.matcher(textBody);
		while (findMatch.find()) {
			occurrence++;
		}
		if(occurrence > 5 ) {
		index(word,occurrence);
		}
		return occurrence;
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
		titleScore = getFrequency(title,term) * TITLE_WEIGHT;
		
		Elements headings = d.select("h1");
		StringBuffer headingText = new StringBuffer();
		for(Element heading : headings) {
			String h1 = heading.text();
			headingText.append(h1);
			System.out.println("HEADING --> " + h1);
		}
	      Pattern pattern = Pattern.compile("\\w+");
 	      Matcher matcher = pattern.matcher(headingText);
	      System.out.println("IN");
	      //need to filter out ignore words here
	      //match each word with heading text return highest word count
	      while(matcher.find()) {
	  		  headingScore = getFrequency(matcher.group(),headingText.toString()) * HEADING_WEIGHT;
	       }

		System.out.println(closed.size() + " ---> " + title);
	 
 
		Elements paragraphs = d.select("p");
		try {
			StringBuffer bodyText = new StringBuffer();
		for(Element paragraph : paragraphs) {
			bodyText.append(paragraph.text());
 		}
		//Break body into words
	        pattern = Pattern.compile("\\w+");
	      //Creating a Matcher object
	        matcher = pattern.matcher(bodyText);
 	      //need to filter out ignore words here
	      //match each word with body text return highest word count
	      while(matcher.find()) {
	  		  bodyScore = getFrequency(matcher.group(),bodyText.toString()) * PARAGRAPH_WEIGHT;
	       }

		}
		catch(Exception e) {
			
		}
		//check if the fuzzy heuristic is good
		if(getFuzzyHeuristic(titleScore, headingScore, bodyScore) > 8) {
		return titleScore + headingScore + bodyScore;
		}
		else {
			return 0;
		}
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
	private double getFuzzyHeuristic(int title, int heading, int body) {
		// load fuzzy inference systems in here also get string distance in here
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
		System.out.println("SCORE : " + frequency.defuzzify());
		// then return result
		return frequency.defuzzify();
	}
	public static StringBuffer ignore() throws IOException {
		//create list of ignore words 
		StringBuffer ignore = new StringBuffer();
		//read file
		FileReader in = new FileReader("./res/ignorewords.txt");
		BufferedReader br = new BufferedReader(in);
		//append buffer
		String line = "";
		while((line = br.readLine()) != null) {
			ignore.append(line + "\n");
		}
		//close file
		br.close();
		System.out.println("IGNORE " + ignore);
		return ignore;
	}
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//create + start worker threads
		//testing multithreading works
 
		Worker w1 = new Worker("https://duckduckgo.com/html/?q=", "byzantium");
		Worker w2 = new Worker("https://duckduckgo.com/html/?q=", "test");
		w1.start();
		w2.start();
		w1.join();
		w2.join();
		Map<String, Integer> test = new ConcurrentHashMap<String, Integer>(w1.wordMap);
		System.out.println("most occuring words" + test.toString());
		
	} 
 
	 
}
