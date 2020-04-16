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
 		 
		NodeParser p = null;
		System.out.println("IN");

		try {
			//fails when called from service handler
			p = new NodeParser(url, term);
			p.process();
			wordMap = p.getMap();
 
 		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			wordMap.put("error",100);
		}
		
	}
 	public Map<String,Integer> getMap() {
 		return wordMap;
 	}
 
}
