package kiraNeccesaryLibs;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The main task of this class is to change a string in every possible and useful way.<br>
* You might also find some useful stuff in the {@link Stuff} class, as some String related stuff like paths correction moved there
 * @author Kira
 *
 */
public class StringManipulator {

	/**
	 * Use this String part to change the System dependent line seperator to a specific String. This allows you to e.g. transfer on large String,
	 * which actually consists of many lines. It is manly used, so that you can use println with a printer class.
	 */
	public static final String LINE_SEPERATOR_CODE = "~#!line-Seperator!~##";
	/**
	 * If you want to convert a List to a String, use this String code as a seperator between entries.
	 */
	public static final String ARRAY_LIST_CODE = "~#!arrayListCode-Seperator!~##";
	
	
	//-----Split String---------------------------------------------------------------------------------------------------------------- 
	//splits String between Spaces
	// 		ArrayList<String> sst = new ArrayList<String>(Arrays.asList(splitString(s)));
	/**
	 * Bei leerzeichen splitten
	 * @param s
	 * @return
	 */
	public static ArrayList<String> splitString (String s)
	{
		//bei splitChar splitten
		return splitString(trimSpaces(s), " ");
	}
	
	/**
	 * splittet den String an jedem festgelegten Char
	 * @param s der String der gesplittet werden soll
	 * @param splitChar der char, an dem gesplittet werden soll
	 * @return 
	 */
	public static ArrayList<String> splitString (String s, String splitChar)
	{
		//TODO make this deprecated and make a new version of it which allowes splitting on strings. There are many characters which are unable to be splitted and needs escape. Maybe use Pattern.quote() somewhere.
		
		
		//Punkte (und bestimmt noch weitere Zeichen) koennen nicht einfach als "." gelassen werden, das wird nicht erkannst, deshalb hier die sonderfaelle
		boolean exception = false;
		if(splitChar.equals("."))
		{
			splitChar="\\.";
			exception= true;
		}
//		System.out.println(s+" : "+ splitChar );
		//Zeilenumbruch zu Wort machen
		if(s.contains("\n"))
		{
//			System.out.println("CONTAINS NEW LINE");
			
			s=s.replace("\n", " \n");
			s=s.replace("\n ", " \n");
//			System.out.println(s);
		}
		
		if (s.contains(splitChar) || exception){
//			System.out.println(Arrays.asList("adsads"+Arrays.asList(s.split(splitChar))));
			 		ArrayList<String> ret = new ArrayList<String>(Arrays.asList(s.split(splitChar)));
//			System.out.println("LISET:"+(ret));
			return ret;
		}
		else{
	 		ArrayList<String> ret = new ArrayList<String>();
	 		ret.add(s);
			return ret;			//nur ein wort
		}
	}

	//-----trimSpaces----------------------------------------------------------------------------------------------------------------
	/**
	 * replaces all space regions with a single space ("hello     you  noob"->"hello you noob")
	 * @param s
	 * @return
	 */
	public static String trimSpaces(String s)
	{
		s=s.replace("  ", " ");
		s=s.replace("  ", " ");
    	return s;
	}
	//-----WortAnz----------------------------------------------------------------------------------------------------------------
	/**
	 * @param s the sentence or at least a String with different words which are seperated by a spacebar
	 * @return Number of words in String
	 */
	public static int wortAnz (String s)
	{
		ArrayList<String>  st=splitString(s);
		if(st != null)
		{
			int i= st.size();
			return i;
		}
		return 1;
	}
	
	//-----isUppercase----------------------------------------------------------------------------------------------------------------
	/**
	 * checks if letter is Uppercase
	 * @param letter
	 * @return
	 */
	public static boolean isUppercase (String letter)
	{
	if(letter.matches("A")||letter.matches("B")||letter.matches("C")||letter.matches("D")||letter.matches("E")||letter.matches("F")||letter.matches("G")||letter.matches("H")||letter.matches("I")||letter.matches("J")||letter.matches("K")||letter.matches("L")||letter.matches("M")||letter.matches("N")||letter.matches("O")||letter.matches("P")||letter.matches("Q")||letter.matches("R")||letter.matches("S")||letter.matches("T")||letter.matches("U")||letter.matches("V")||letter.matches("W")||letter.matches("X")||letter.matches("Y")||letter.matches("Z"))	
	{
	return true;	
	}
	
	return false;
	}
	
	//-----adjustString----------------------------------------------------------------------------------------------------------------
	/**
	 * Gibt den Satz wieder, aber setzt die Satzumbrueche automatisch, dass eine Zeile nicht mehr Buchstaben (mit Leerzeichen) als maxCharsInRow enthaelt
	 * @param s the String to adjust
	 * @param maxCharsInRow defines how many chars are shown in one line. Meaning every letter, number etc as well as a space bar are counted as one. \n and other commands are ignored for the row length
	 * @return
	 */
	
	public static String adjustString(String s, int maxCharsInRow)
	{
		ArrayList<String> sst = (splitString(s));
		ArrayList<String> ret = new ArrayList<String>();
		
		int lineChars = sst.get(0).length();
		String currentLine=sst.get(0);
		
		String retString="";
		
		for(int i=1;i<sst.size();i++)
		{
			if(lineChars + sst.get(i).length() <= maxCharsInRow || (sst.get(i).contains("\n")))
			{
				//Manueller Zeilenumbruch am Anfang
				if(sst.get(i).contains("\n"))
				{
					if(sst.get(i).indexOf('\n')==0)
					{
						ret.add(currentLine);	

						currentLine=sst.get(i).subSequence(1, sst.get(i).length()).toString();	//Das \n wegmachen
						lineChars=sst.get(i).length()+1;
					}else if(sst.get(i).indexOf('\n')==sst.get(i).length()-1)
					{
						//Manueller Zeilenumbruch am Ende
						currentLine = currentLine +" "+ sst.get(i).replace('\n', ' ');
						ret.add(currentLine.substring(0, currentLine.length()-2));	//Das \n wegmachen
						currentLine="";
						lineChars=0;
					}
				}else
				{	
					//Normalfall
					lineChars=lineChars+sst.get(i).length()+1;
					currentLine = currentLine +" "+ sst.get(i);
				}
			}else
			{
				ret.add(currentLine);
				currentLine=sst.get(i);
				lineChars=sst.get(i).length();
			}
		}
		
		ret.add(currentLine);

		for(int i=0;i<ret.size();i++)
		{
			retString=retString+ret.get(i)+"\n";
		}
		return retString;
	}
	
	//-----getWordByNumber----------------------------------------------------------------------------------------------------------------
	/**
	 * gets a specific word in the String at the given position
	 * @param s the sentence you want to get a word fromClient
	 * @param position the position of the word. Remember that the first word is at place 0
	 * @return
	 */
	
	public static String getWordByNumber(String s, int position)
	{
		return splitString(s).get(position);
	}	
	
	//-----stringListTOString----------------------------------------------------------------------------------------------------------------

	/**
	 * Builds a sentence fromClient the given words (addSpaces=true) or the given lines (addReturn = true). If both are false, the words or lines
	 * will just be appended after one another. Both true is possible, but I cant think of any situation where you want to use it.
	 * @param sst the list containing all the words
	 * @param addSpaces if true, the resulting String will have a SPACE between every list entry (should be words)
	 * @param addReturn if true, the resulting String will have a RETURN between every list entry (should be lines)
	 * @return
	 */
	public static String stringListTOString(ArrayList<String> sst, boolean addSpaces, boolean addReturn)
	{
		String s = "";
		if(addSpaces){s = stringListTOString(sst, " ");}
		if(addReturn){s = stringListTOString(sst, System.lineSeparator());}
		if(!addSpaces && !addReturn){return stringListTOString(sst, "");}
		return s;
	}
	
	public static String stringListTOString(ArrayList<String> strings, String seperator) {

		String ret="";
		for(int i=0;i<strings.size();i++){
			ret+=strings.get(i)+seperator;}
		
		try{
			ret.substring(0, ret.length()-seperator.length());//delete last seperator
		}catch(StringIndexOutOfBoundsException e){System.err.println("String to short, maybe there are no Strings to combine? "+strings);}
		return ret;
	}
	
	//-----Number conversion----------------------------------------------------------------------------------------------------------------
	
	public static boolean isNumber(String no)
	{
		try{
			Double.parseDouble(no);
			return true;
		}catch(NumberFormatException e)
		{
			return false;
		}
	
	}
	/**Konvertiert alle Zahlen vom Wort in die Zahl (Null-->0)
	 * Die Nummern muessen alle als lowercase vorliegen
	 * @param s Satz
	 * @return ArrayList<String> mit den konvertierten Zahlen 
	 */
	public static ArrayList<String> numberConvert(String s)
	{
		return numberConvert(splitString(s));
	}
	
	/**Konvertiert alle Zahlen vom Wort in die Zahl (Null-->0)
	 * Die Nummern muessen alle als lowercase vorliegen
	 * @param sst ArrayListe mit den einzelnen Woertern
	 * @return ArrayList<String> mit den konvertierten Zahlen 
	 */
	public static ArrayList<String> numberConvert(ArrayList<String> sst)
	{

		for(int i=0;i<sst.size();i++)
		{
			switch(sst.get(i)){

			case "null":
				sst.set(i, "0");
				break;

			case "eins":
				sst.set(i, "1");
				break;

			case "zwei":
				sst.set(i, "2");
				break;

			case "drei":
				sst.set(i, "3");
				break;

			case "vier":
				sst.set(i, "4");
				break;

			case "fuenf":
				sst.set(i, "5");
				break;

			case "sechs":
				sst.set(i, "6");
				break;

			case "sieben":
				sst.set(i, "7");
				break;

			case "acht":
				sst.set(i, "8");
				break;

			case "neun":
				sst.set(i, "9");
				break;

			}
		}
		return sst;
	}
	
	
	//-----Vokal Check----------------------------------------------------------------------------------------------------------------

	/**
	 * checks if this char is a Vokal (a,e,u,i,o)
	 * @param c the char to check
	 * @return true if it is a vokal
	 */
	public static boolean isVokal (char c)
	{
		return(containsVokal(""+c));
	}
	
	/**
	 * checks if this char is a Vokal (a,e,u,i,o)
	 * @param s the char to check
	 * @return true if it is a vokal
	 */
	public static boolean isVokal (String s)
	{
		return(containsVokal(s));
	}

	/**
	 * checks if the String contains a vokal (a,e,i,o,u)
	 * @param s the String to check 
	 * @return true if there is a vocal in the String
	 */
	public static boolean containsVokal(String s)
	{
		s=s.toLowerCase();
		if(s.contains("a") || s.contains("e") || s.contains("i") || s.contains("o") || s.contains("u") || s.contains("oe") || s.contains("ae") || s.contains("ue"))
				{
					return true;
				}else
				{
					return false;
				}
	}
	

	/**
	 * checks if the String contains a konsonant
	 * @param s the String to check 
	 * @return true if there is a konsonant in the String
	 */
	public static boolean containsKonsonant(String s)
	{
		s=s.toLowerCase();
		if(s.contains("b") || s.contains("c") || s.contains("d") || s.contains("f") || s.contains("g") || s.contains("h") || s.contains("j") || s.contains("k") || s.contains("l") || s.contains("m") || s.contains("n") || s.contains("p") || s.contains("q") || s.contains("r") || s.contains("s") || s.contains("t") || s.contains("v") || s.contains("w") || s.contains("x") || s.contains("y") || s.contains("z"))
				{
					return true;
				}else
				{
					return false;
				}
	}
	
	
	//-----Zeichen Kombination----------------------------------------------------------------------------------------------------------------

	/**
	 * combines every same char after one another to one, so there are only different chars after another (oeffnen ->oefnen)
	 * @param s the String to trim the duplicate chars
	 * @return the string without douplicate chars
	 */
	public static String charKombination(String s)
	{
		char c;
		//kombiniert alle gleiche zeichen hintereinander zu einem --> oeffnen ->oefnen
		for(int t=0;t<s.length()-1;t++)
		{
			if(s.charAt(s.length()-t-2)==s.charAt(s.length()-t-1))
			{
				c=s.charAt(s.length()-t-2);
				s=s.replaceAll(""+c+""+c, ""+c);
			}
		}
		return s;
	}

	/**
	 * checks if the given String contains a letter
	 * @param s
	 * @return true if it contains a letter (a,s,h,e,b...); false if there are only numbers and Symbols (�,5,3,/,$ ...)
	 */
	public static boolean containsLetter(String s) {
		if(containsKonsonant(s) || containsVokal(s))
		{
			return true;
		}

		return false;

	}
	
	/**
	 * inserts a given String into another one
	 * @param origin the String you want to add something to
	 * @param insert the String you want to insert into the origin String
	 * @param index the Index where you want to insert the insert String into the origin String
	 * @return the origin String with the inserted one
	 */
	public static String insertString(String origin, String insert, int index) {
		String x = origin.substring(0, index);
		String y = origin.substring(index);
		return x+insert+y;
	}
	
	
	/**
	 * unused (not yet neccesary)
	 * @param s
	 * @return
	 */
	public static String changeFirstVokal (String s)	//input: Word s; 	return: first a/u/o replaced with ae/ue/oe
	{
		if(s.contains("a") || s.contains("o") || s.contains("u"))	//s wurde schon bei tt=1 in pluralform gebracht
		{
			if(s.contains("a"))
			{s= s.replaceFirst("a", "ae");}
			if(s.contains("o"))
			{s= s.replaceFirst("o", "oe");}
			if(s.contains("u"))
			{s= s.replaceFirst("u", "ue");}
			System.out.println("umlautcheck "+s);
		}
		return s;
	}
	
	

	/**
	 * Aendert den ersten Umlaut zu seinem korrespondierenden Vokal (ae-->a, ue-->u; oe-->o)
	 * @param word
	 * @return das geaenderte Wort
	 */
	public static String changeFirstUmlaut (String word)	//input: Word s; 	return: first ae/ue/oe replaced with a/u/o 
	{
		if(word.contains("ae") || word.contains("oe") || word.contains("ue"))	//s wurde schon bei tt=1 in pluralform gebracht
		{
			if(word.contains("ae"))
			{word= word.replaceFirst("ae", "a");}
			if(word.contains("oe"))
			{word= word.replaceFirst("oe", "o");}
			if(word.contains("ue"))
			{word= word.replaceFirst("ue", "u");}
			System.out.println("umlautcheck "+word);
		}
		return word;
	}

	/**
	 * removes all letters from the String, only returning numbers and symbols
	 * @param changedPart1
	 * @return the String without any letters
	 */
	public static String removeObjects(String s, boolean removeLetters, boolean removeNumbers, boolean removeSymbols) {
		String ret = "";
		String cs = "";
		
		boolean remove = false;
		for(int t=0; t<s.length();t++)
		{
			remove = false;
			cs = Character.toString(s.charAt(t));
			if(!remove && removeLetters && containsLetter(cs))
			{remove = true;}
			
			if(!remove && removeNumbers)
			{
				int doubleFound =0;
				try{
					Integer.parseInt(cs);
					remove = true;
					if(Character.toString(s.charAt(t+1)).equals("."))	//checks for double/float number (dezimal)
					{
						Integer.parseInt(Character.toString(s.charAt(t+2)));	//if no exception frame, means there is a double
						
						for(int tt = 2;tt<s.length()-t;t++)
						{
							Double.parseDouble(s.substring(t, tt));
							doubleFound++;
						}
					}
					
				}catch(Exception e){
					if(doubleFound != 0)
					{
						t+=doubleFound+1;	//anzahl der nachkommastellen + Punktchar
					}
				}
			}
			
			if(!remove && removeSymbols && containsSymbol(cs)) //hinter die num abfrage, damit punkte, welche evtl zu doubles gehoeren nicht kaputt gemacht werden
			{remove = true;}
			
			if(!remove)
			{
			ret+=Character.toString(s.charAt(t));
			}

			
			
		}
		return ret;
	}

	/**
	 * checks the String for symbols like %�/"(=�%) etc.
	 * @param c
	 * @return true if this Strign contains a symbol
	 */
	public static boolean containsSymbol(String c) {
		if(
				c.contains("!") || c.contains("\"") || c.contains("�") || c.contains("$") || c.contains("&") || c.contains("_") ||
				c.contains(":") ||c.contains(";") ||c.contains("|") ||
				c.contains("(") || c.contains(")") || c.contains("{") || c.contains("}") || c.contains("[") || c.contains("]") ||
				c.contains("\\") || c.contains("�") || c.contains("@") || c.contains("'") || 
				c.contains("=") || c.contains("^")  || c.contains("~") || c.contains("#") || c.contains("%") || 
				c.contains("<") || c.contains(">") || c.contains("+") || c.contains("-") || c.contains("*") || c.contains("/") || c.contains(",") || c.contains(".")
			)
		{return true;}
		
		return false;
	}
	
	
	/**
	 * Converts the String to a String, where parts are replaced to allow a easy transfer with the printer class.
	 * Manly it is used, where all line Sperators are replaced with a special kind of String Code.<br>
	 * To decode the String again, use {@link #decodeTransferString(String)}
	 * @param s The String to encode
	 * @return the encoded version of your string
	 */
	public static String encondeTransferString(String s)
	{
		s=s.replaceAll("(\r\n|\n)", LINE_SEPERATOR_CODE);	//Fuer universalcodierung der Zeilenumbrueche
		s=s.replaceAll(System.lineSeparator(), LINE_SEPERATOR_CODE);
		return s;
	}
	
	/**
	 * Decodes the String, where all line seperators are replaced by special String codes by the {@link #encondeTransferString(String)} method.
	 * @param s the String to decode
	 * @return the decoded String
	 */
	public static String decodeTransferString(String s)
	{
		s=s.replaceAll(LINE_SEPERATOR_CODE, System.lineSeparator());
		return s;
	}

	/**
	 * Encodes an ArrayList to a String, where every entry is seperated by a special code from one another and also the entry itself
	 * is encoded with the {@link #encondeTransferString(String)}.<br>
	 * To decode the list, use {@link #decodeTransferArrayList(String)}
	 * @param list The ArrayList to encode
	 * @return the encoded ArrayList
	 * 
	 */
	public static String encondeTransferArrayList (ArrayList<String> list)
	{
		String s ="";
		  for(String cc : list)
		  {
			  s+=encondeTransferString(cc)+ARRAY_LIST_CODE;
		  }
		return s;
	}
	
	/**
	 * Decodes the String which contains informations about an ArrayList, which were decoded by {@link #encondeTransferArrayList(ArrayList)}
	 * @param s the string to decode
	 * @return the decoded String
	 */
	public static ArrayList<String> decodeTransferArrayList(String s)
	{
  		ArrayList<String> ret = StringManipulator.splitString(s, ARRAY_LIST_CODE);
  		for(String x : ret){
  			x=decodeTransferString(s);
  		}
  		
  		return ret;
	}

	/**
	 * This method does the same as if you call yourString.indexOf(aString). But with this method you would have to know the exact String.
	 * Here you can just enter a sequences of chars which you know of the String you are searching for and leave the chars where you dont care what it is blank.<br><br>
	 * 
	 * Here is an example:<br>
	 * I used this method to check if a String, which was entered by a user contains a url. Therefore I wanted to check if it contains the
	 * sequence ".xx/" or ".xxx/" for stuff like "https://www.google.de/?gws_rd=ssl". So I called the method like this:<br><br>
	 * indexOfRelativeSequence("https://www.google.de/?gws_rd=ssl", ".", "", "", "/") for ".xx/" and <br>
	 * indexOfRelativeSequence("https://www.google.de/?gws_rd=ssl", ".", "", "", "", "/") for ".xxx/" <br><br>
	 * 
	 * The "x" is a character I dont care for what it is (.de/; .it/; .com/; .net/; ...), so I entered for this character nothing ("").<br>
	 * You can also use null for characters that you dont care what value they have. In my case the code then would look like:<br><br>
	 * 
	 * indexOfRelativeSequence("https://www.google.de/?gws_rd=ssl", ".", null, null, "/") for ".xx/" and <br>
	 * indexOfRelativeSequence("https://www.google.de/?gws_rd=ssl", ".", null, null, null, "/") for ".xxx/" <br><br>
	 * 
	 * @param searchString The String, in which you are searching the relative String for
	 * @param characters the chars, of what your relative String consists. If you dont care for a specific character, leave an empty String or use null for this char
	 * @return the starting position of your character sequence in you String or -1 if none was found
	 */
	public static int indexOfRelativeSequence(String searchString, String... characters) {
		String ccs = "";
		String cc = "";
		boolean breaking = false;
		for(int t=0;t<searchString.length()-characters.length;t++){
			for(int tt=0;tt<characters.length;tt++){
				breaking = false;
				ccs = searchString.substring(t+tt, t+tt+1);
				cc = characters[tt];
				if(cc != null && !cc.equals("") && !cc.equals(ccs)){breaking = true; break;}
			}
			if(!breaking){
				return t;
			}
		}

		return -1;
		
	}
	
	
}

