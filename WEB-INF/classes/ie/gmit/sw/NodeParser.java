package ie.gmit.sw;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NodeParser {
	// change these only template best first search
	// Code was adapted from Assignment workshop by Dr. John Healy GMIT
	private static final int MAX = 100;
	private static final int TITLE_WEIGHT = 100;
	private static final int HEADING_WEIGHT = 20;
	private static final int PARAGRAPH_WEIGHT = 1;
	private String title;
	private String term;
	// for
	private Set<String> closed = new ConcurrentSkipListSet<>();
	// get comparator
	private Queue<DocumentNode> q = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));

	public NodeParser(String url, String searchTerm) throws IOException {
		super();
		this.term = searchTerm;
		Document doc = Jsoup.connect(url).get();
		int score = getHeuristicScore(doc);
		// for tracking
		closed.add(url);
		// put new document on queue
		q.offer(new DocumentNode(doc, score));
		process();
		// TODO Auto-generated constructor stub
	}

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
						q.offer(new DocumentNode(child, score));
					} catch (IOException e1) {
					}
				}
			}
		}
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
	private int getFrequency(String s) {
		//check for searchterm in s
		return 0;
	}
	private int getHeuristicScore(Document d) {
		String title = d.title();
		System.out.println(closed.size() + " ---> " + title);
		try{
		String body = d.body().text();
		System.out.println(body);
		}
		catch(Exception e) {
			
		}
		Elements headings = d.select("h1");
		for(Element heading : headings) {
			String h1 = heading.text();
			System.out.println("HEADING --> " + h1);
		}
		
		return 0;
	}

	public static void main(String[] args) throws IOException {
		new NodeParser("https://jsoup.org/cookbook/input/parse-document-from-string", "Java");
	}
}
