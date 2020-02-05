package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		HashMap<String,Occurrence> Map=new HashMap<String,Occurrence>();
		if (docFile==null) {
			throw new FileNotFoundException();
		}
		Scanner scan = new Scanner(new File(docFile));
		while (scan.hasNext()) {
			String word=getKeyWord(scan.next());
			if (Map.containsKey(word)) {
				Map.get(word).frequency++;
			}
			else {
				Occurrence occurrence=new Occurrence(docFile,1);
				Map.put(word,occurrence);
			}
		}
		scan.close();
		return Map;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for (String word :kws.keySet()){
			if(keywordsIndex.containsKey(word)){
				keywordsIndex.get(word).add(kws.get(word));
				insertLastOccurrence(keywordsIndex.get(word));
			} 
			else {
				ArrayList<Occurrence> occurrence=new ArrayList<Occurrence>();
				occurrence.add(kws.get(word));
				keywordsIndex.put(word,occurrence);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word){ 
		word=word.toLowerCase();
		if (word==null) {
			return null;
		}
		int i=0;
		int j=0;
		int k=0;
		String pre="";
		String cur="";
		String post="";
		if (hasPunctuation(word)) {	
			for (i=0;i<word.length();i++){
				if (!Character.isLetter(word.charAt(i)))
					break;  
				else
					pre=pre+word.charAt(i); 
			}
			for (j=i;j<word.length();j++){
				if (Character.isLetter(word.charAt(j)))
					break; 
				else
					cur=cur+word.charAt(j);
			}	
			for (k=j;k<word.length();k++) {
				post=post+word.charAt(k);
			}
			if (post.isEmpty()){
				if (noiseWords.contains(pre))
					return null;
				else{
					if (!pre.trim().isEmpty())
						return pre;
					else
						return null; 
				}
			}
			else {
				return null; 
			}
		}
		else{
			if (noiseWords.contains(word)){
				return null;
			}
			else{
				if (!word.trim().isEmpty()){
					return word;
				}
				else{
					return null;  
				}
			}
		}
	}
	
	private static boolean hasPunctuation(String word) {
		for (int i=0;i<word.length();i++) {
			if (!Character.isLetter(word.charAt(i))) {
				return true; 
			}
		}
		return false; 
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> freq = new ArrayList<Integer>();
		for (int i = 0; i < occs.size()-1; i++){
			freq.add(occs.get(i).frequency);
		}
		int val = occs.get(occs.size()-1).frequency; 
		ArrayList<Integer> result = binarySearch(freq, 0, freq.size()-1,val);
		return result;
	}
	
	private ArrayList<Integer> binarySearch(ArrayList<Integer> frequency,int min,int max,int key) {
		ArrayList<Integer> midPoints=new ArrayList<Integer>(); 
		while (max>=min){
			int mid=(min+max)/2;
			midPoints.add(mid); 
			if (frequency.get(mid)<key){
				max=mid-1;
			}
			else if (frequency.get(mid)>key){
				min=mid+1;
			}
			else
				break; 
		}
		return midPoints; 
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		kw1=kw1.toLowerCase();
		kw2=kw2.toLowerCase();
		ArrayList<String> result=new ArrayList<String>();
		ArrayList<Occurrence> arraylist1=keywordsIndex.get(kw1);
		ArrayList<Occurrence> arraylist2=keywordsIndex.get(kw2);
		int i=0;
		int j=0; 
		int count=0;
		if (arraylist1==null && arraylist2==null){
			return null; 
		}
		else if (arraylist1==null){
			while (j<arraylist2.size() && count<5){
				result.add(arraylist2.get(j).document); 
				j++; 
				count++; 
			}
		}
		else if (arraylist2==null){
			while (i<arraylist1.size() && count<5){
				result.add(arraylist1.get(i).document); 
				i++; 
				count++; 
			}
		}
		else {	
			while ((i<arraylist1.size() || j<arraylist2.size()) && count<5) {
				if (arraylist1.get(i).frequency>arraylist2.get(j).frequency && (!result.contains(arraylist1.get(i).document))) {
					result.add(arraylist1.get(i).document); 
					i++;
					count++; 
				}
				else if (arraylist1.get(i).frequency<arraylist2.get(j).frequency && (!result.contains(arraylist2.get(j).document))){
					result.add(arraylist2.get(j).document); 
					j++;
					count++; 
				}
				else{
					if (!result.contains(arraylist1.get(i).document)){
						result.add(arraylist1.get(i).document);
						count++; 
						i++;
					}
					else{
						i++; 
					}
					if ((!result.contains(arraylist2.get(j).document))){
						if (count<5){
							result.add(arraylist2.get(j).document); 
							j++;
							count++; 
						}
					}
					else {
						j++; 
					}
				}
			}
		}
	return result;
	}
}
