<center><h1>AI Word Cloud by Ultan Kearns - G00343745 GMIT</h1></center>

# About Project
+ This project was made under the supervision of Dr. John Healy for the fourth year module Artificial Intelligence.
+ This project was made by myself(Ultan Kearns) using resources from Dr. John Healy.
+ The overall goal of this project is to learn how to apply methodology and principles of AI to produce a solution to a real world problem.
+ This project will be a word cloud that traverses a graph G(Graph of HTML links) with node size N(HTML links) to produce a result of the most common words associated with the graph G by analyzing N.

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

	FUZZIFY title
		TERM infrequent := (0,1)(2,0);
		TERM nominal :=  (1,0)(2,1)(6,0);
		TERM frequent :=  (4,0)(10,1); 
	END_FUZZIFY 
	FUZZIFY heading 
		TERM infrequent := (0,1)(2,0);
		TERM nominal :=  (1,0)(3,1)(4,0);
		TERM frequent := (3,0)(6,1); 
	END_FUZZIFY
	FUZZIFY body 
		TERM infrequent := (0,1)(5,0);
		TERM nominal := (3,0)(6,1)(12,0);
		TERM frequent :=  (10,0)(20,1); 
	END_FUZZIFY
	DEFUZZIFY relevance
		TERM low := (0,1) (5,0);
		TERM normal :=(5,0)(10,1)(15,0);
		TERM high := (15,0)(20,1);
		METHOD : COG;
		DEFAULT := 0; 
## Graph Traversal (AI traversal of nodes)
Here I will explain which graph traversal algorithm(s) I used and why.
## Overall Result

## Additional Features
+ Able to specify what number of words you want to appear on screen