package ie.gmit.sw.ai.cloud;

public class WordFrequency { 
	private String word;
	private int frequency;
	private int fontSize = 0;

	public WordFrequency(String word, int frequency) {
		this.word = word;
		this.frequency = frequency;
	}

	public String getWord() {
		return this.word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getFontSize() {
		return this.fontSize;
	}

	public void setFontSize(int size) {
		this.fontSize = size;
	}

	public String toString() {
		return "Word: " + getWord() + "\tFreq: " + getFrequency() + "\tFont Size: " + getFontSize();
	}
}