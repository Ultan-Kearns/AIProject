<center><h1>AI Word Cloud by Ultan Kearns - G00343745 GMIT</h1></center>

# Important notes
+ Multi-threaded
+ No output for image but can run the main method in service handler and retrieve the results
+ All results filtered with ignore file
+ uses depth first search using doubly linked list
+ Heuristics are applied and results are output based on fuzzy heuristic

# About Project
+ This project was made under the supervision of Dr. John Healy for the fourth year module Artificial Intelligence.
+ This project was made by myself(Ultan Kearns) using resources from Dr. John Healy.
+ The overall goal of this project is to learn how to apply methodology and principles of AI to produce a solution to a real world problem.
+ This project will be a word cloud that traverses a graph G(Graph of HTML links) with node size N(HTML links) to produce a result of the most common words associated with the graph G by analyzing N.
+ Graph Traversal uses a depth first search accomplished using a doubly LinkedList
+ This application is multithreaded(See worker class)

## Exploration Of Heuristic
The heuristics are performed using the JFuzzy logic API.  The Heuristics take 3 inputs title, body and heading.  Each heuristic has three types, infrequent - it doesn't appear very often, nominal -  this would be the average amount you'd expect a word to appear in a given body of text, and frequent - this signifies it is very relevant.

The output of these inputs is relevance, this output will determine the overall relevance of a particular word.  The final output follows a set of rules which I will list below:
+ RULE 1 :IF title IS frequent THEN relevance IS high;
+ RULE 2 :IF title IS nominal OR heading IS frequent Or body IS frequent THEN relevance IS high;
+ RULE 3 :IF title IS infrequent AND heading IS nominal OR body IS nominal THEN relevance IS normal;
+ RULE 4 :IF title IS infrequent AND heading IS infrequent AND body IS frequent THEN relevance IS high; 		
+ RULE 5 :IF title IS infrequent AND heading IS infrequent AND body IS infrequent THEN relevance IS low; 		
+ RULE 6 :IF title IS nominal AND heading IS nominal AND body IS nominal THEN relevance IS normal; 		
+ RULE 7: IF title IS frequent OR body IS frequent OR heading IS frequent THEN relevance IS high;
+ RULE 8: IF title IS frequent AND body IS frequent AND heading IS frequent THEN relevance IS high;

The way I have these rules setup conveys to the user that the title is worth more than the heading and body, the heading is worth less than title but is worth more than the body, and the body is worth less than both the heading and title.  So therefore the body would require a higher frequency of the words occurrence to be considered relevant than the heading or the title.  This is due to the fact that if a word appears in a heading or title it must be more relevant than a word that appears in the body.

Below you can see how I fuzzified the rules and how title and heading require less occurrences than that of body to be considered relevant to the user.
	
	FUNCTION_BLOCK frequency
		// Here we define the inputs which we consider necessary to compute the output
		// Title, heading and body will all be analyzed
		VAR_INPUT
			title : REAL; 
			heading : REAL; 
			body: REAL;
		END_VAR
		//define output named relevance, how relevant will the word be?
		VAR_OUTPUT
			relevance : REAL; 
		END_VAR
		// title is worth more than heading and body
		// words here can be sparse but still considered relevant
		FUZZIFY title
			TERM infrequent := (100,1)(200,1)(300,0);
			TERM nominal :=  (200,0)(300,1)(400,0);
			TERM frequent :=  (300,0)(400,1)(500,1); 
		END_FUZZIFY 
		// heading is worth less than title but more than body
		// words here most appear more frequently than title to be considered relevant
		// but words can appear less times than in body and be considered relevant
		FUZZIFY heading 
			TERM infrequent := (0,1)(20,0);
			TERM nominal :=  (0,0)(10,0)(20,1)(30,0);
			TERM frequent := (0,0)(20,0)(40,1); 
		END_FUZZIFY
		//body is worth less than both heading and title 
		//and words most appear more frequently here to be considered relevant
		FUZZIFY body 
			TERM infrequent := (0,1)(30,1)(50,0);
			TERM nominal := (40,0)(55,1)(80,0);
			TERM frequent :=  (70,0)(100,1); 
		END_FUZZIFY
		//relevance is determined by the rules
		DEFUZZIFY relevance
			TERM low := (0,1)(30,1) (50,0);
			TERM normal :=(40,0)(70,1)(80,0);
			TERM high := (70,0)(90,1);
			METHOD : COG;
			DEFAULT := 0; 
		END_DEFUZZIFY
## Graph Traversal (AI traversal of nodes)
Here I will explain which graph traversal algorithm(s) I used and why.

I ignored pages that had a total score of less than 5 as I deemed these irrelevant, the pages deemed relevant were passed to the getfuzzyheuristic function where they were scored.

The graph traversal algorithm I used was depth first search, I accomplished this by creating a doubly linkedlist and added each document node to the end of the list.  Each edge or link node in the HTML page is then added to the back of the list.  In this way a depth first search was implmented.

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
## MultiThreaded
Here is the class that instantiates a new Thread it extends the class Thread and overrides the run method to start a new instance of the class NodeParser.

	package ie.gmit.sw;
	
	import java.io.IOException;
	import java.util.Map;
	import java.util.concurrent.ConcurrentHashMap;
	/*
	 * This is a worker class that will be used to create threads
	 * these threads will each call a nodeparser to parse the nodes 
	 * and navigate the tree to find the 20 most frequent words 
	 * associated with a certain topic
	 * This class extends thread
	 */
	public class Worker extends Thread{
		String url,term;
		Map <String,Integer> wordMap = new ConcurrentHashMap<String, Integer>();
		public Worker(String url, String term) {
			this.term = term;
			this.url = url;
			
		}
	 	public void run() {
			NodeParser p;
			System.out.println("IN");
			try {
				p = new NodeParser(url, term);
				p.process();
				wordMap = p.getMap();
	 		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		}
	 	public   Map<String, Integer> getMap() {
	 		return wordMap;
	 	}
	}


## Overall Result
+ Depth first search was implemented using a doubly linked list - it's a list thats linked....doubly
+ MultiThreading was implemented - used the worker class see multithreaded section
+ Ignore words are filtered from returned String, Int map
+ Uses defuzzified value to output the result
+ Program outputs expected results but cannot decode them to image 

## Tests
### Top 20 words for:
+ Caracella:  {roman=9, father=9, used=8, dio=22, end=14, severus=12, caracalla=61, 11=9, new=6, 1=75, 2=91, 3=18, 4=12, emperor=31, 8=12, name=7, geta=10, brother=12, 20=15, age=20}
+ Marcus Aurelius: {stoic=10, 26=14, roman=45, april=6, good=8, life=21, augustus=8, known=12, aurelius=23, emperors=20, latin=7, antoninus=55, philosopher=11, 17=24, 161=16, march=7, marcus=215, 1=345, emperor=50, li=261}
+ Fyodor Dostoevsky: {november=8, works=25, dor=9, literary=14, mikh=19, fy=9, ipa=9, fyodor=7, 11=16, russian=40, february=9, j=83, dostoevsky=180, 1=332, 2=99, 9=87, short=15, writer=13, tr=134, story=10}
+ Michael Collins: {dublin=8, death=6, irish=17, british=12, independence=6, ned=9, ric=9, kitty=7, collins=48, jordan=8, ira=12, end=13, res=18, break=6, film=32, michael=7, boland=22, 4=6, valera=27, ireland=9}
+ Plato: {bc=11, thought=10, considered=14, athenian=6, classical=11, platonist=9, world=23, school=6, academy=7, greek=16, figure=6, philosopher=28, ple=59, philosophy=15, ancient=14, 2=58, socrates=96, western=9, plato=275, pl=379}

## Notes
+ You can run service handlers main class for a demo in this program
+ No image output currently.
+ You can read a nicer version of this text file here

## Extras
+ Able to specify what number of words you want to appear on screen
+ Uses DFS(Depth first search) by adding searchterms to the end of a list
+ Can use custom ignore file by passing string arg to ignore function