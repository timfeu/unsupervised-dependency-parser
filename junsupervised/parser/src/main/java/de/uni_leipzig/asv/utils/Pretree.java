/*******************************************************************************
 * Copyright 2012
 * Chris Biemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.uni_leipzig.asv.utils;

/*
 * #%L
 * JUnsupervisedParser
 * %%
 * Copyright (C) 2016 Tim Feuerbach
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author Christian Biemann, Florian Holz
 * @version 26.02.2006
 * 
 *          Externe Funktionen:
 * 
 *          public void train(String word, String class) : Einfuegen von word
 *          mit Klasse class public String classify(String word): Liefert class
 *          zu word (bei genug thresh) public void ttyout(): Ausgabe des Baums
 *          auf Standardausgabe public void prune(); Pruning des Trees public
 *          String getAllEntriesString(); Ausgabe des Baums als String public
 *          Map toMap(); Map aller Paare (Eintrag, Klassen) des Baumes public
 *          Set keySet(); Set aller Keys (Eintraege) des Pretree
 * 
 *          void load(String filename): Laed Baum als Objetbaum void save(String
 *          filename): Speichert Baum als Objetbaum
 * 
 *          Parameter:
 * 
 *          boolean d: Debug-Meldungen boolean _ignorecase: wie der name sagt
 *          boolean _reverse: ob eintraege von hinten betrachtet werden double
 *          thresh: threshold fuer klassifizierung
 * 
 *          Interne Funktionen:
 * 
 *          alles, was private ist void insert(Knoten k): Fuegt Knoten in Baum
 *          ein Knoten einf(Knoten k1, Knoten k2) :Knoten 2 (Blatt) wird in
 *          Knoten 1 eingefuegt, Knoten suche(Knoten k, String w) :String w wird
 *          in Knoten k gesucht, returnt Knoten mit w void anzeig(Knoten
 *          aktknoten,int n) : Zeigt Knotenstruktur auf Textstandardausgabe.
 *          Vector vecAdd(Vector, Vector) addiert Klassenwerte von Vektoren
 */

public class Pretree implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2454434179583109180L;

	public static final int getExact = 0;

	public static final int getUpper = 1;

	public static final int getLower = 2;

	// Deklaration der Variablen/Instanzen
	static Hashtable hash; // fuer vecAdd

	static boolean d = false; // fuer Meldungen (debugging), false=AUS

	private boolean _reverse = false;

	private boolean _ignorecase = false;

	private double thresh = 0.0;

	/*
	 * wurzel und _stringtree sind fuer die gleiche Information zustaendig! das
	 * erfordert Aufmerksamkeit, um Inkonsistenzen zu vermeiden!
	 * 
	 * wurzel ist die Wurzel der Objektbaumrepraesentation (die ist im Zweifel
	 * massgebend)
	 * 
	 * _stringtree enthaelt die Stringrepraesentation, auf der _keine_ den
	 * Pretree veraendernden Operationen moeglich sind d.h. bei einer solchen
	 * wird die Stringrepraesentation ungueltig => _stringtee = null
	 * 
	 * solange der Informationsstand derselbe ist, koennen sowohl wurzel != null
	 * als auch _stringtree != null sein! bspw. bearbeiteter Pretree (nur
	 * Obj.repr.) wird gespeichert (Str.repr. erzeugt, aber Obj.Repr. bleibt)
	 * 
	 * siehe (in Methoden) if( wurzel==null){ wurzel =
	 * getObjectTree(_stringtree); } _stringtree = null; bei Bedarf, also wenn
	 * Obj.Repr. geaendert wird, und somit Str.Repr ungueltig wird
	 * 
	 * bzw. if( _stringtree==null ){ _stringtree = getStringTree(wurzel); } nie
	 * wurzel=null hier, da die Obj.repr. nie ungueltig wird (ausser beim laden
	 * eines Pretrees)
	 */
	private Knoten wurzel; // Wurzelknoten; zunaechst leerer Baum

	private char[] _stringtree;

	private int _offsetlaenge;

	private int _basis;

	private int _startchar;

	private int _endchar;

	private char _achtungZahl;

	private char _achtungKnoten;

	private char _endOfWordChar;

	private static String _tab = new String("\t");

	private static String _nl = new String("\n");

	// Defaultwerte
	private static int _defaz = 2;

	private static int _defak = 3;

	private static int _defsc = 33;

	private static int _defec = 248;

	private static char _defEoW = (char) 4;

	private static boolean _defic = false;

	private static boolean _defrv = false;

	// Konstruktior initialisiert Wurzel und Wurzelklassen
	// ----------------------------------------------------
	public Pretree() {

		this.wurzel = new Knoten();
		this._stringtree = null;

		this._startchar = _defsc;
		this._endchar = _defec;
		this._achtungZahl = (char) _defaz;
		this._achtungKnoten = (char) _defak;
		this._endOfWordChar = _defEoW;
		this._basis = this._endchar - this._startchar + 1;
		this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
				/ Math.log(this._basis));

		this._reverse = _defrv;
		this._ignorecase = _defic;
	}

	public Pretree(int sc, int ec, int az, int ak, int eow, boolean rv,
			boolean ic, char[] stringtree) {

		this.wurzel = null;
		this._stringtree = stringtree;

		this._startchar = sc;
		this._endchar = ec;
		this._achtungZahl = (char) az;
		this._achtungKnoten = (char) ak;
		this._endOfWordChar = (char) eow;
		this._basis = this._endchar - this._startchar + 1;
		this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
				/ Math.log(this._basis));

		this._reverse = rv;
		this._ignorecase = ic;
	}

	public void setStartChar(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			if (this.wurzel == null) {
				this.wurzel = getObjectTree(this._stringtree);
			}
			this._stringtree = null;

			this._startchar = c;
			this._basis = this._endchar - this._startchar + 1;
			this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
					/ Math.log(this._basis));
		}
	}

	public void setEndChar(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			if (this.wurzel == null) {
				this.wurzel = getObjectTree(this._stringtree);
			}
			this._stringtree = null;

			this._endchar = c;
			this._basis = this._endchar - this._startchar + 1;
			this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
					/ Math.log(this._basis));
		}
	}

	public void setAchtungZahl(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			if (this.wurzel == null) {
				this.wurzel = getObjectTree(this._stringtree);
			}
			this._stringtree = null;

			this._achtungZahl = (char) c;
		}
	}

	public void setAchtungKnoten(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			if (this.wurzel == null) {
				this.wurzel = getObjectTree(this._stringtree);
			}
			this._stringtree = null;

			this._achtungKnoten = (char) c;
		}
	}

	public void setEndOfWordChar(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			this._endOfWordChar = (char) c;
		}
	}

	/*
	 * private _setXXX fuer load(filename) ohne Konversion i Obj.Repr.
	 */
	private void _setStartChar(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			this._startchar = c;
			this._basis = this._endchar - this._startchar + 1;
			this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
					/ Math.log(this._basis));
		}
	}

	private void _setEndChar(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			this._endchar = c;
			this._basis = this._endchar - this._startchar + 1;
			this._offsetlaenge = (int) Math.ceil(Math.log(Integer.MAX_VALUE)
					/ Math.log(this._basis));
		}
	}

	private void _setAchtungZahl(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			this._achtungZahl = (char) c;
		}
	}

	private void _setAchtungKnoten(int c) {
		if (c < 0) {
			throw new IllegalArgumentException(
					"Character number must be greater than 0");
		} else {
			this._achtungKnoten = (char) c;
		}
	}

	public void setThresh(double d) {
		this.thresh = d;
	}

	public double getThresh() {
		return this.thresh;
	}

	public void setIgnoreCase(boolean b) {
		this._ignorecase = b;
	}

	public void setReverse(boolean b) {
		this._reverse = b;
	}

	public boolean getIgnoreCase() {
		return this._ignorecase;
	}

	public boolean getReverse() {
		return this._reverse;
	}

	private static String reverse(String s) {

		char[] ret = new char[s.length()];
		StringBuffer torev = new StringBuffer(s);
		for (int i = torev.length() - 1, j = 0; i >= 0; i--, j++) {
			ret[j] = torev.charAt(i);
		}
		return new String(ret);
	}

	// Voted ermittelt in Abh. von thresh die Entscheidung
	// ---------------------------------------------------
	private String voted2(Vector classes) {
		if (classes == null) {
			return null;
		}
		int sum = 0;
		int maxval = 0, actval;
		String maxclass = "undecided";
		String actclass;
		for (Enumeration e = classes.elements(); e.hasMoreElements();) {
			StringTokenizer st = new StringTokenizer((String) e.nextElement(),
					"=");
			actclass = st.nextToken();
			if (st.hasMoreTokens())
				actval = new Integer(st.nextToken()).intValue();
			else
				actval = 0;
			sum += actval;
			if (actval > maxval) {
				maxval = actval;
				maxclass = actclass;
			}
			if ((actval == maxval) && !actclass.equals(maxclass)) {
				maxclass += ";" + actclass;
			}

		}
		if (((double) maxval / (double) sum) >= this.thresh) {
			return maxclass;
		} else {
			return "undecided";
		}
	} // end voted

	// vecAdd addiert die Klassen von 2 Vectors
	// -------------------------------------------------------------
	private static Vector vecAdd(Vector een, Vector twee) {
		Vector terug = new Vector();
		hash = new Hashtable();
		String clas, snr;
		int nr, nr2;
		String van;
		String cont;
		for (Enumeration e = een.elements(); e.hasMoreElements();) {
			van = (String) e.nextElement();
			StringTokenizer st = new StringTokenizer(van, "=");
			clas = st.nextToken();
			snr = st.nextToken();
			// nr=new Integer(snr).intValue();
			hash.put(clas, snr);
		}
		for (Enumeration f = twee.elements(); f.hasMoreElements();) {
			van = (String) f.nextElement();
			StringTokenizer st = new StringTokenizer(van, "=");
			clas = st.nextToken();
			if (st.hasMoreTokens()) {
				snr = st.nextToken();
				nr = new Integer(snr).intValue();
			} else {
				nr = 0;
			}
			cont = (String) hash.get(clas);
			if (cont != null) { // schon vorhanden
				if (d) {
					System.out.println("Cont: " + cont);
				}
				nr2 = new Integer(cont).intValue();
				nr = nr + nr2;
			}
			snr = (new Integer(nr)).toString();
			hash.put(clas, snr);
		}

		for (Enumeration g = hash.keys(); g.hasMoreElements();) {
			String c = (String) g.nextElement();
			String instr = c + "=" + hash.get(c);
			terug.addElement(instr);
		}
		return terug;
	} // end vecAdd

	// ////////EINF//**************************************************************************************
	private Knoten einf(Knoten k1, Knoten k2) { // Knoten 2 (Blatt) wird in
												// Knoten 1 eingefuegt. 3
												// Faelle:
		// wk2= pr wk1= 1.(e) 2. p 3. ps

		String w0 = new String(); // Sem(w0)=p
		String w1 = new String(); // Sem(w1)=s
		String w2 = new String(); // Sem(w2)=r

		if (k1 == null) { // FALL 1: k1 ex. noch nicht. Dann k2 hier
							// einhaengen, ende.
			return k2;
		} // end if Fall 1

		int pos = 0; // Matching fuer Faelle 2 und 3
		int min;

		if (k1.inhalt.length() < k2.inhalt.length()) {
			min = k1.inhalt.length();
		} // Mimimumsbildung fuer for
		else {
			min = k2.inhalt.length();
		}
		for (pos = 0; pos < min; pos++) { // Finden von pos fuer berechn.
											// w0-w2
			if (k1.inhalt.charAt(pos) != k2.inhalt.charAt(pos)) {
				break;
			}
		} // rof pos
		w0 = k2.inhalt.substring(0, pos); // Berechnung w0,w1,w2
		w1 = k1.inhalt.substring(pos, k1.inhalt.length());
		w2 = k2.inhalt.substring(pos, k2.inhalt.length());
		if (w2.length() == 0) {
			k1.classes = vecAdd(k1.classes, k2.classes);
			return k1;
		} // fi w2

		if (w1.length() == 0) { // Fall 2: k1 Anfangsstueck von k2, |s|=0
			k2.inhalt = w2;
			Knoten goalpos = getChild(k1, w2);
			if (goalpos == null) {
				k1.kinder.addElement(k2);
			} else {
				k1.kinder.removeElement(goalpos);
				k1.kinder.addElement(einf(goalpos, k2));
			}
			k1.classes = vecAdd(k1.classes, k2.classes);
			return k1;
		} // end if Fall 2
		else { // Fall 3: k1 und k2 haben gleiches Praefix p, Suffixe s bzw. r
			Knoten h = new Knoten(w0);
			k2.inhalt = w2;
			h.kinder.addElement(k2);
			k1.inhalt = w1;
			h.kinder.addElement(k1);
			h.classes = vecAdd(k1.classes, k2.classes);
			return h;
		} // end else Fall 3
	} // end Knoten einf(Knoten,Knoten) // Returnt rekursiv veraenderten
		// Unterknoten.

	// getChild liefert kind von Knoten k zurueck, in dem w gefunden werden kann
	private Knoten getChild(Knoten k, String w) {
		Knoten kind;
		for (Enumeration e = k.kinder.elements(); e.hasMoreElements();) {
			kind = (Knoten) e.nextElement();
			if (d) {
				// System.out.println("Kind: "+kind.inhalt+" vgl mit "+w);

			}
			if (kind.inhalt.substring(0, 1).equals(w.substring(0, 1))) {
				return kind;
			}
		}
		return null;
	}

	// //////Suche*********************************************************************************************
	private Knoten suche(Knoten k, String w) { // String w wird in Knoten k
												// gesucht.

		Knoten rknoten;
		String w0 = new String(); // s. REM einf
		String w1 = new String(); //
		String w2 = new String(); //

		if (k == null) {
			return null; // FALL 1: p=0 (nicht gefunden)
		}

		int min; // s. REM einf
		int pos = 0;

		if (k.inhalt.length() < w.length()) {
			min = k.inhalt.length();
		} // Mimimumsbildung fuer for
		else {
			min = w.length();
		}
		for (pos = 0; pos < min; pos++) { // Finden von pos fuer berechn.
											// w0-w2
			if (k.inhalt.charAt(pos) != w.charAt(pos)) {
				break;
			}
		}

		w0 = w.substring(0, pos); // Berechnung w0,w1,w2
		w1 = k.inhalt.substring(pos, k.inhalt.length());
		w2 = w.substring(pos, w.length());

		if (w2.length() != 0) {
			if (d) {
				System.out.println("'" + w2 + "'");
			}
			rknoten = suche(getChild(k, w2), w2); // weitersuchen in unterkn.
			if (rknoten != null) {
				return rknoten;
			} else {
				return k;
			}
		}

		if (w1.length() != 0) {
			return k; // nicht gefunden: returne letzten Knten
		}

		return k; // Suchergebnis durchreichen
	} // end Knoten suche(knoten,wort) - Return des Unterknotens;

	// //////////////////////anzeige///////////////////////////////////// fuer
	// ttyout

	private void anzeig(Knoten aktknoten, int n) { // Zeigt Knotenstruktur auf
													// Textstandardausgabe.
		int tiefe = n + 1; // fuer "-" Anzeige
		for (Enumeration e = aktknoten.kinder.elements(); e.hasMoreElements();) {
			Knoten akk = (Knoten) e.nextElement();
			for (int j = 1; j <= tiefe; j++) {
				System.out.print("-");
			}
			Vector v = akk.classes;
			if (v != null) {
				System.out.println(akk.inhalt + " " + akk.classes.toString());
			} else {
				System.out.println(akk.inhalt + " nix");
			}
			anzeig(akk, tiefe);
		} // for-if
	} // void anzeig(Knote, tiefe)

	private String anzeigStr(Knoten aktknoten, int n) { // Zeigt Knotenstruktur
														// auf
														// Textstandardausgabe.
		String retStr = "";
		int tiefe = n + 1; // fuer "-" Anzeige

		// aktueller Knoten
		for (int j = 1; j <= n; j++) {
			retStr += "-";
		}
		if (this._reverse) {
			retStr += reverse(aktknoten.inhalt) + " "
					+ aktknoten.classes.toString() + "\n";
		} else {
			retStr += aktknoten.inhalt + " " + aktknoten.classes.toString()
					+ "\n";
		}

		// kinder
		for (Enumeration e = aktknoten.kinder.elements(); e.hasMoreElements();) {
			Knoten akk = (Knoten) e.nextElement();
			for (int j = 1; j <= tiefe; j++) {
				retStr += "-";
			}
			retStr += anzeigStr(akk, tiefe);
		} // for-if
		return retStr;
	} // String anzeigStr(Knote, tiefe)

	// *********************** Pruning *************************

	public void prune() {
		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		this._stringtree = null;

		this.wurzel = pruneKnoten(this.wurzel);
	} // end prune()

	private Knoten pruneKnoten(Knoten aktKnoten) {
		String vklass = ""; // voted Klasse
		String aklass = ""; // aktuelle Klasse
		StringTokenizer st; // fuer Vektoraunseinandernehmen
		Knoten akk;
		Vector temp;

		// System.out.println("Anzahl: "+aktKnoten.classes.size()+" von "+
		// aktKnoten.classes.toString()+" mit "+aktKnoten.kinder.size()+ "
		// Kindern, Inhalt "+aktKnoten.inhalt);
		// Blatt: Schneide Inhalt ab
		if (aktKnoten.kinder.size() == 0) {
			aktKnoten.inhalt = aktKnoten.inhalt.substring(0, 1);
		}
		// Eindeutige Klasse: Abschneiden des unterbaumes
		else if (aktKnoten.classes.size() == 1) {
			aktKnoten.inhalt = aktKnoten.inhalt.substring(0, 1);
			aktKnoten.kinder.removeAllElements();
		}
		// innerer Knoten: rekursiv absteigen und default loeschen
		else {
			vklass = voted2(aktKnoten.classes);
			temp = new Vector();
			for (Enumeration e = aktKnoten.kinder.elements(); e
					.hasMoreElements();) {
				akk = (Knoten) e.nextElement();
				if (akk.classes.size() == 1) { // falls Eindeutig
					aklass = (String) akk.classes.elementAt(0);
					st = new StringTokenizer(aklass, "=");
					aklass = st.nextToken();
					// System.out.println("Class "+aklass);
					if (aklass.equals(vklass)) {
					} else {
						akk = pruneKnoten(akk);
						temp.addElement(akk);
					}
				} else {
					akk = pruneKnoten(akk);
					temp.addElement(akk);
				}
			} // for-if
			aktKnoten.kinder = temp;
		} // else

		return aktKnoten;
	} // end pruneKnoten

	// Ansteuerung als Objekt Pretree
	// **************************************************************************
	// ***********************************************************************************Schnittstelle*******

	/**
	 * trains, that word is of class cla
	 */
	public void train(String word, String cla) {

		this.train(word, cla, 1);
	}

	/**
	 * trains, that word is of class cla with number nr of occurrences
	 */
	public void train(String word, String cla, int nr) {

		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		this._stringtree = null;

		if (this._ignorecase) {
			word = word.toLowerCase();
		}
		if (this._reverse) {
			word = reverse(word);
		}
		Knoten k = new Knoten(word + this._endOfWordChar);
		k.classes = new Vector();
		k.classes.addElement(cla + "=" + nr);
		insert(k);
	}

	/**
	 * zaehlt Anzahl verschiedener Klassen im Baum
	 */
	public int getNrOfClasses() {

		if (this.wurzel == null) {

			int i = 0;
			while (this._stringtree[i] != this._achtungKnoten) { // Wurzelinhalt,
																	// sollte
																	// nicht
																	// vorkommen
				i++;
			}

			int ret = 0;
			i++;
			i++;
			while (this._stringtree[i] != ']') { // classes-Angaben
													// mitmeisseln

				while ((this._stringtree[i] != ';')
						&& (this._stringtree[i] != ']')) {
					i++;
				}
				if (this._stringtree[i] != ']') {
					i++;
				}// ';' hintermirlassen
				ret++;
			}
			return ret;
		} else {
			Vector wurzelvec = new Vector();
			wurzelvec = this.wurzel.classes;
			return wurzelvec.size();
		}
	}

	/**
	 * zaehlt Anzahl Blaetter im Baum, bei einem geprunetem Baum Anzahl der
	 * repraesentierten, nicht die wirkliche Anzahl Blaetter des Restbaumes
	 */
	public int getNrOfNodes() {

		int retval = 0;
		Vector wurzelvec = new Vector();

		if (this.wurzel == null) {

			int i = 0;
			while (this._stringtree[i] != this._achtungKnoten) { // Wurzelinhalt,
																	// sollte
																	// nicht
																	// vorkommen
				i++;
			}

			Vector aktclasses = new Vector();

			i++;
			i++;
			while (this._stringtree[i] != ']') { // classes-Angaben
													// mitmeisseln
				StringBuffer aktclass = new StringBuffer();
				while ((this._stringtree[i] != ';')
						&& (this._stringtree[i] != ']')) {
					aktclass.append(this._stringtree[i]);
					i++;
				}
				if (this._stringtree[i] != ']') {
					i++;
				}// ';' hintermirlassen
				aktclasses.add(aktclass.toString());
			}
			wurzelvec = aktclasses;

		} else {
			wurzelvec = this.wurzel.classes;
		}

		String actStr = "";
		StringTokenizer st;
		String wclass = "", welement = "";
		int actInt = 0;
		for (Enumeration f = wurzelvec.elements(); f.hasMoreElements();) {
			actStr = (String) f.nextElement();
			st = new StringTokenizer(actStr, "=");
			if (st.hasMoreTokens())
				wclass = st.nextToken();
			else
				continue;
			if (st.hasMoreTokens())
				welement = st.nextToken();
			else
				continue;
			actInt = new Integer(welement).intValue();
			retval += actInt;
		}
		return retval;
	}

	public String classify(String word) {

		if (this.wurzel == null) {
			return classify_string(word);
		}
		return classify_object(word);
	}

	private String classify_string(String word) {

		if (this._ignorecase) {
			word = word.toLowerCase();
		}
		if (this._reverse) {
			word = reverse(word);
		}
		Knoten k = get_nearest(word + this._endOfWordChar);
		return voted2(k.classes);
	}

	private String classify_object(String word) {

		if (this._ignorecase) {
			word = word.toLowerCase();
		}
		if (this._reverse) {
			word = reverse(word);
		}
		Knoten k = find(word + this._endOfWordChar);
		return voted2(k.classes);
	}

	public double getProbabilityForClass(String word, String cla) {
		double ret = 0;
		if (this.wurzel == null) {
			return getProbabilityForClass_string(word, cla);
		}
		if (this._ignorecase) {
			word = word.toLowerCase();
		}
		if (this._reverse) {
			word = reverse(word);
		}
		Knoten k = find(word + "<");

		double valsum = 0;
		double goalval = 0;
		String actclass;
		int actval;

		for (Enumeration e = k.classes.elements(); e.hasMoreElements();) {
			StringTokenizer st = new StringTokenizer((String) e.nextElement(),
					"=");
			actclass = st.nextToken();
			actval = new Integer(st.nextToken()).intValue();
			valsum += actval;
			if (actclass.equals(cla)) {
				goalval = actval;
			}
		} // rof enum e

		if (valsum > 0) {
			ret = goalval / valsum;
		}

		return ret;
	}

	public double getProbabilityForClass_string(String word, String cla) {
		double ret = 0;

		if (this._ignorecase) {
			word = word.toLowerCase();
		}
		if (this._reverse) {
			word = reverse(word);
		}
		Knoten k = get_nearest(word + "<");

		double valsum = 0;
		double goalval = 0;
		String actclass;
		int actval;

		for (Enumeration e = k.classes.elements(); e.hasMoreElements();) {
			StringTokenizer st = new StringTokenizer((String) e.nextElement(),
					"=");
			actclass = st.nextToken();
			actval = new Integer(st.nextToken()).intValue();
			valsum += actval;
			if (actclass.equals(cla)) {
				goalval = actval;
			}
		} // rof enum e

		if (valsum > 0) {
			ret = goalval / valsum;
		}

		return ret;

	}

	// = find for string
	private Knoten get_nearest(String word) {

		return get(word, Pretree.getLower);
	}

	private Knoten get(String word, int mode) {

		String aktw = word;
		int i = 0; // aktuelle Pos im _stringtree
		StringBuffer aktlabel;
		String exlabel = "";
		Vector aktclasses;
		// System.out.print("Wurzelinhalt? sollte \"\" sein: \"");
		while (this._stringtree[i] != this._achtungKnoten) { // Wurzelinhalt,
																// sollte nicht
																// vorkommen
			exlabel += this._stringtree[i];
			i++;
		}
		// System.out.println("\"");
		while (true) {
			aktclasses = new Vector();
			aktlabel = new StringBuffer();
			// aktclasses = getClassesAt(i); //wieso geht das hier nicht ???

			i++; // _achtungKnoten hintermirlassen
			i++; // '[' hintermirlassen
			while (this._stringtree[i] != ']') { // classes-Angaben
													// mitmeisseln
				StringBuffer aktclass = new StringBuffer();
				while ((this._stringtree[i] != ';')
						&& (this._stringtree[i] != ']')) {
					aktclass.append(this._stringtree[i]);
					i++;
				}
				if (this._stringtree[i] != ']') {
					i++;
				}// ';' hintermirlassen
					// System.out.println("aktuelle Klasse: "+aktclass.toString());
				aktclasses.add(aktclass.toString());
			}

			if (aktw.length() == 0) { // exakter Treffer
				break;
			}
			if ((i + 1) == this._stringtree.length) {// baum zuende -> nichts
														// Besseres findbar
				if (mode == Pretree.getExact) {
					exlabel = null;
					aktclasses = null;
				}
				break;
			}
			i++; // ']' hintermirlassen
			while (this._stringtree[i] != aktw.charAt(0)) { // Labelanfaenge
															// pruefen
				if (this._stringtree[i] == this._achtungKnoten) { // an neuem
																	// Knoteneintrag
																	// angekommen,
																	// d.h.
																	// nichts
																	// (besser)
																	// passendes
																	// (keinen
																	// Kindknoten)
																	// gefunden
					if (mode == Pretree.getExact) {
						exlabel = null;
						aktclasses = null;
					}
					break;
				}
				while (this._stringtree[i] != this._achtungZahl) { // Label
																	// hintersichlassen
					i++;
				}
				i++; // _achtungZahl hintersichlassen
				i += this._offsetlaenge; // Offset ueberspringen
			}
			if (this._stringtree[i] == this._achtungKnoten) { // faengt break
																// von obiger
																// innerer
																// Schleife
				break;
			}
			while (this._stringtree[i] != this._achtungZahl) { // aktuelles
																// Label
																// mitmeisseln
				aktlabel.append(this._stringtree[i]);
				i++;
			}
			i++; // _achtungZahl hintermirlassen -> stehe am Anfang des
					// Offsets
			if (aktlabel.length() > aktw.length()) { // falls label laenger
														// als (rest)wort ->
														// unpassend -> nichts
														// gefunden, sonst
														// action
				if (mode == Pretree.getExact) {
					exlabel = null;
					aktclasses = null;
				} else if (mode == Pretree.getLower) {
					exlabel = aktlabel.toString();
					aktclasses = getClassesAt(string2int(new String(
							this._stringtree, i, this._offsetlaenge)));
				} else if (mode == Pretree.getUpper) {
					// passt schon alles
				}
				break;
			}
			String w1 = aktw.substring(0, aktlabel.length());
			String w2 = aktw.substring(aktlabel.length());
			if (!(w1.equals(aktlabel.toString()))) { // kein passender
														// Kindknoten zum
														// weitergucken
				if (mode == Pretree.getExact) {
					exlabel = null;
					aktclasses = null;
				} else if (mode == Pretree.getLower) {
					exlabel = aktlabel.toString();
					aktclasses = getClassesAt(string2int(new String(
							this._stringtree, i, this._offsetlaenge)));
				} else if (mode == Pretree.getUpper) {
					// passt schon alles
				}
				break;
			}
			if (w2.length() == 0) {
			}
			int offset;
			offset = string2int(new String(this._stringtree, i,
					this._offsetlaenge));
			aktw = w2; // um Rest zu suchen
			i = offset; // zum gefundenen Kindknoten springen
			exlabel = aktlabel.toString();
		}
		Knoten k = new Knoten(exlabel);
		k.classes = aktclasses;
		return k;

	}// end get(word, mode)

	private Vector getClassesAt(int pos) {

		int i = pos;
		Vector retClasses = new Vector();
		i++; // _achtungKnoten hintermirlassen
		i++; // '[' hintermirlassen
		while (this._stringtree[i] != ']') { // classes-Angaben mitmeisseln
			StringBuffer aktclass = new StringBuffer();
			while ((this._stringtree[i] != ';') && (this._stringtree[i] != ']')) {
				aktclass.append(this._stringtree[i]);
				i++;
			}
			if (this._stringtree[i] != ']') {
				i++;
			}// ';' hintermirlassen
				// System.out.println("aktuelle Klasse: "+aktclass.toString());
			retClasses.add(aktclass.toString());
		}
		return retClasses;
	}

	private void insert(Knoten k) {

		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		this._stringtree = null;

		if (d) {
			System.out.println("Inserting:" + k.inhalt);
		}
		this.wurzel.classes = vecAdd(this.wurzel.classes, k.classes);
		Knoten gpos = getChild(this.wurzel, k.inhalt);
		if (gpos == null) {
			gpos = k;
			this.wurzel.kinder.addElement(gpos);
		} else {
			this.wurzel.kinder.removeElement(gpos);
			this.wurzel.kinder.addElement(einf(gpos, k));
		}
	}

	// = get_nearest for object
	private Knoten find(String w) {

		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		Knoten wchild = getChild(this.wurzel, w);
		if (wchild == null) {
			return this.wurzel;
		} else {
			return suche(wchild, w);
		}
	}

	public void ttyout() {

		if (this.wurzel != null) {
			System.out.println(this.wurzel.inhalt + " "
					+ this.wurzel.classes.toString());
			anzeig(this.wurzel, 0);
		} else {
			int i = 0;
			while (this._stringtree[i] != this._achtungKnoten) {
				System.out.print(this._stringtree[i]);
				i++;
			}
			System.out.print(" ");
			i++;
			while (this._stringtree[i] != ']') {
				System.out.print(this._stringtree[i]);
				i++;
			}
			System.out.print(this._stringtree[i]);
		}
	}

	public String giveReason(String w) {

		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		if (this._ignorecase) {
			w = w.toLowerCase();
		}
		if (this._reverse) {
			w = reverse(w);
		}
		w = w + this._endOfWordChar;
		Knoten k = find(w);
		return anzeigStr(k, 0);
	}

	public Vector classDistribution(String w) {

		if (this.wurzel == null) {
			this.wurzel = getObjectTree(this._stringtree);
		}
		if (this._ignorecase) {
			w = w.toLowerCase();
		}
		if (this._reverse) {
			w = reverse(w);
		}
		w = w + this._endOfWordChar;
		Knoten k = find(w);
		return k.classes;
	}

	/**
	 * gives a map view of the pretree
	 */
	public Map toMap() {

		Map ret = new TreeMap();
		if (this.wurzel != null) {
			addObjectToMap(ret, this.wurzel, new StringBuffer());
		} else {
			addStringToMap(ret, this._stringtree, 0, new StringBuffer());
		}
		return ret;
	}

	private void addStringToMap(Map m, char[] treestring, int aktPos,
			StringBuffer aktInhalt) {

		int i = aktPos;
		i++; // _achtungKnoten hintermirlassen
		i++; // '[' hintermirlassen

		StringBuffer aktclasses = new StringBuffer();
		aktclasses.append('[');
		while (treestring[i] != ']') { // classes-Angaben mitmeisseln
			aktclasses.append(treestring[i]);
			i++;
		}
		aktclasses.append(']');
		i++; // ']' hintersichlassen

		m.put(aktInhalt.toString(), aktclasses.toString());

		if (i < treestring.length) {
			while (treestring[i] != this._achtungKnoten) { // Kinderinhalte
															// sammeln,
															// Unterbaeume
															// mitnehmen
				StringBuffer aktKindInhalt = new StringBuffer(aktInhalt);
				StringBuffer aktKindOffset = new StringBuffer();
				while (treestring[i] != this._achtungZahl) {
					aktKindInhalt.append(treestring[i]);
					i++;
				}
				i++; // achtungZahl hintersichlassen
				for (int j = 0; j < this._offsetlaenge; j++) {
					aktKindOffset.append(treestring[i]);
					i++;
				}
				addStringToMap(m, treestring,
						string2int(aktKindOffset.toString()), aktKindInhalt);
			}
		}
	}

	private void addObjectToMap(Map m, Knoten aktKnoten, StringBuffer aktInhalt) {

		aktInhalt.append(aktKnoten.inhalt);
		m.put(aktInhalt.toString(), outKlassen(aktKnoten).toString());

		for (Enumeration e = aktKnoten.kinder.elements(); e.hasMoreElements();) {

			Knoten aktKind = (Knoten) e.nextElement();
			addObjectToMap(m, aktKind, new StringBuffer(aktInhalt.toString()));
		}
	}

	/**
	 * gives the key set of the pretree
	 */
	public Set keySet() {

		Set ret = new TreeSet();
		if (this.wurzel != null) {
			addObjectToKeySet(ret, this.wurzel, new StringBuffer());
		} else {
			addStringToKeySet(ret, this._stringtree, 0, new StringBuffer());
		}
		return ret;
	}

	private void addStringToKeySet(Set s, char[] treestring, int aktPos,
			StringBuffer aktInhalt) {

		int i = aktPos;
		i++; // _achtungKnoten hintermirlassen
		i++; // '[' hintermirlassen

		while (treestring[i] != ']') { // classes-Angaben mitmeisseln
			i++;
		}
		i++; // ']' hintersichlassen

		s.add(aktInhalt.toString());

		if (i < treestring.length) {
			while (treestring[i] != this._achtungKnoten) { // Kinderinhalte
															// sammeln,
															// Unterbaeume
															// mitnehmen
				StringBuffer aktKindInhalt = new StringBuffer(aktInhalt);
				StringBuffer aktKindOffset = new StringBuffer();
				while (treestring[i] != this._achtungZahl) {
					aktKindInhalt.append(treestring[i]);
					i++;
				}
				i++; // achtungZahl hintersichlassen
				for (int j = 0; j < this._offsetlaenge; j++) {
					aktKindOffset.append(treestring[i]);
					i++;
				}
				addStringToKeySet(s, treestring,
						string2int(aktKindOffset.toString()), aktKindInhalt);
			}
		}
	}

	private void addObjectToKeySet(Set s, Knoten aktKnoten,
			StringBuffer aktInhalt) {

		aktInhalt.append(aktKnoten.inhalt);
		s.add(aktInhalt.toString());

		for (Enumeration e = aktKnoten.kinder.elements(); e.hasMoreElements();) {

			Knoten aktKind = (Knoten) e.nextElement();
			addObjectToKeySet(s, aktKind,
					new StringBuffer(aktInhalt.toString()));
		}
	}

	/**
	 * gives all entries; each line: entry tab classes;
	 */
	public String getAllEntriesString() {

		StringBuffer ret = new StringBuffer();
		if (this.wurzel != null) {
			addObjectNodesEntriesString(ret, this.wurzel, new StringBuffer());
		} else {
			addStringNodesEntriesString(ret, this._stringtree, 0,
					new StringBuffer());
		}
		return ret.toString();
	}

	private void addStringNodesEntriesString(StringBuffer s, char[] treestring,
			int aktPos, StringBuffer aktInhalt) {

		int i = aktPos;
		i++; // _achtungKnoten hintermirlassen
		i++; // '[' hintermirlassen

		s.append(aktInhalt);
		s.append(_tab);
		s.append('[');
		while (treestring[i] != ']') { // classes-Angaben mitmeisseln
			s.append(treestring[i]);
			i++;
		}
		s.append(']');
		i++; // ']' hintersichlassen
		s.append(_nl);

		if (i < treestring.length) {
			while (treestring[i] != this._achtungKnoten) { // Kinderinhalte
															// sammeln,
															// Unterbaeume
															// mitnehmen
				StringBuffer aktKindInhalt = new StringBuffer(aktInhalt);
				StringBuffer aktKindOffset = new StringBuffer();
				while (treestring[i] != this._achtungZahl) {
					aktKindInhalt.append(treestring[i]);
					i++;
				}
				i++; // achtungZahl hintersichlassen
				for (int j = 0; j < this._offsetlaenge; j++) {
					aktKindOffset.append(treestring[i]);
					i++;
				}
				addStringNodesEntriesString(s, treestring,
						string2int(aktKindOffset.toString()), aktKindInhalt);
			}
		}
	}

	private void addObjectNodesEntriesString(StringBuffer s, Knoten aktKnoten,
			StringBuffer aktInhalt) {

		aktInhalt.append(aktKnoten.inhalt);
		s.append(aktInhalt);
		s.append(_tab);
		s.append(outKlassen(aktKnoten));
		s.append(_nl);

		for (Enumeration e = aktKnoten.kinder.elements(); e.hasMoreElements();) {

			Knoten aktKind = (Knoten) e.nextElement();
			addObjectNodesEntriesString(s, aktKind,
					new StringBuffer(aktInhalt.toString()));
		}
	}

	// conversion string to object
	private Knoten getObjectTree(char[] treestring) {

		Knoten w = new Knoten("");

		int i = 0;
		StringBuffer tmp = new StringBuffer();
		while (treestring[i] != this._achtungKnoten) { // Wurzelinhalt, sollte
														// nicht vorkommen
			tmp.append(treestring[i]);
			i++;
		}
		if (tmp.length() > 0) {
			w.inhalt = tmp.toString();
		}
		tmp = null;
		Vector aktclasses = new Vector();

		i++;
		i++;
		while (treestring[i] != ']') { // classes-Angaben mitmeisseln
			StringBuffer aktclass = new StringBuffer();
			while ((treestring[i] != ';') && (treestring[i] != ']')) {
				aktclass.append(treestring[i]);
				i++;
			}
			if (treestring[i] != ']') {
				i++;
			}// ';' hintermirlassen
			aktclasses.add(aktclass.toString());
		}
		w.classes = aktclasses;
		w.kinder = new Vector();

		i++; // ']' hintersichlassen
		if (i >= treestring.length) {
			return w;
		}
		while (treestring[i] != this._achtungKnoten) { // Kinderinhalte
														// sammeln, Unterbaeume
														// mitnehmen
			StringBuffer aktInhalt = new StringBuffer();
			StringBuffer aktOffset = new StringBuffer();
			while (treestring[i] != this._achtungZahl) {
				aktInhalt.append(treestring[i]);
				i++;
			}
			i++; // achtungZahl hintersichlassen
			for (int j = 0; j < this._offsetlaenge; j++) {
				aktOffset.append(treestring[i]);
				i++;
			}
			Knoten aktKind = string2tree_neuesFormat(treestring,
					string2int(aktOffset.toString()));
			aktKind.inhalt = aktInhalt.toString();
			w.kinder.add(aktKind);
		}
		return w;
	}

	// conversion string -> object (recursive, private)
	private Knoten string2tree_neuesFormat(char[] treestring, int pos) {

		Knoten w = new Knoten("");

		int i = pos;
		Vector aktclasses = new Vector();
		i++; // _achtungKnoten hintermirlassen
		i++; // '[' hintermirlassen
		while (treestring[i] != ']') { // classes-Angaben mitmeisseln
			StringBuffer aktclass = new StringBuffer();
			while ((treestring[i] != ';') && (treestring[i] != ']')) {
				aktclass.append(treestring[i]);
				i++;
			}
			if (treestring[i] != ']') {
				i++;
			}// ';' hintermirlassen
			aktclasses.add(aktclass.toString());
		}
		w.classes = aktclasses;
		w.kinder = new Vector();

		i++; // ']' hintersichlassen
		if (i >= treestring.length) {
			return w;
		}
		while (treestring[i] != this._achtungKnoten) { // Kinderinhalte
														// sammeln, Unterbaeume
														// mitnehmen
			StringBuffer aktInhalt = new StringBuffer();
			StringBuffer aktOffset = new StringBuffer();
			while (treestring[i] != this._achtungZahl) {
				aktInhalt.append(treestring[i]);
				i++;
			}
			i++; // achtungZahl hintersichlassen
			for (int j = 0; j < this._offsetlaenge; j++) {
				aktOffset.append(treestring[i]);
				i++;
			}
			Knoten aktKind = string2tree_neuesFormat(treestring,
					string2int(aktOffset.toString()));
			aktKind.inhalt = aktInhalt.toString();
			w.kinder.add(aktKind);
		}
		return w;

	}// end string2tree_neuesFormat

	// conversion object -> string
	private char[] getStringTree(Knoten w) {

		StringBuffer ret = new StringBuffer();
		ret.append(w.inhalt);
		ret.append(tree2string_neuesFormat(w, ret.length()));
		return ret.toString().toCharArray();

	}

	/*
	 * save pretree in string representation to file
	 */
	public void save(String filename) throws IOException {

		ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(
				filename));

		if (this._stringtree == null) {
			this._stringtree = getStringTree(this.wurzel);
		}
		oos2.writeObject(new String("Pretree"));
		oos2.writeObject(new String("Stringformat char[]"));
		oos2.writeObject(new String("version=1.3"));
		oos2.writeObject(new Integer(this._startchar));
		oos2.writeObject(new Integer(this._endchar));
		oos2.writeObject(new Integer(this._achtungZahl));
		oos2.writeObject(new Integer(this._achtungKnoten));
		oos2.writeObject(new Integer(this._endOfWordChar));
		oos2.writeObject(new Boolean(this._reverse));
		oos2.writeObject(new Boolean(this._ignorecase));
		oos2.writeObject(this._stringtree); // char[]
		oos2.close();

	} // ende public void save

	// conversion Object -> String.
	private StringBuffer tree2string_neuesFormat(Knoten aktKnoten, int startPos) {

		StringBuffer ret = new StringBuffer();
		ret.append(this._achtungKnoten);
		int relPos = 1;
		StringBuffer tmp;

		tmp = outKlassen(aktKnoten);
		relPos += tmp.length();
		ret.append(tmp);

		tmp = outKinderInhalte(aktKnoten, relPos);
		relPos += tmp.length();
		ret.append(tmp);

		int vorigerTeilbaum = 0;
		for (Enumeration e = aktKnoten.kinder.elements(); e.hasMoreElements();) {

			Knoten aktKind = (Knoten) e.nextElement();
			relPos += vorigerTeilbaum;
			outPos(ret, aktKind.pos, relPos + startPos);
			tmp = tree2string_neuesFormat(aktKind, relPos + startPos);
			vorigerTeilbaum = tmp.length();
			ret.append(tmp);
		}

		return ret;
	}

	// conversion classifications -> substring in format. Used only by
	// tree2string_neuesFormat
	private StringBuffer outKlassen(Knoten aktKnoten) {

		StringBuffer ret = new StringBuffer("[");
		int k = 0;

		for (Enumeration f = aktKnoten.classes.elements(); f.hasMoreElements();) {
			k++;
			if (k != 1) {
				ret.append(";");
			}
			ret.append((String) f.nextElement());
		}
		ret.append("]");

		return ret;
	}

	// conversion subtrees -> substring in format. Used only by
	// tree2string_neuesFormat
	private StringBuffer outKinderInhalte(Knoten aktKnoten, int startPos) {

		StringBuffer ret = new StringBuffer("");
		int relPos = 0;
		for (Enumeration e = aktKnoten.kinder.elements(); e.hasMoreElements();) {
			Knoten aktKind = (Knoten) e.nextElement();
			ret.append(aktKind.inhalt);
			ret.append(this._achtungZahl);
			relPos = ret.length();
			aktKind.pos = relPos + startPos;
			ret.append(int2string(0)); // Platzhalter
		}
		return ret;
	}

	private void outPos(StringBuffer str, int pos, int offset) {

		str.replace(pos, pos + this._offsetlaenge, int2string(offset));
	}

	private String int2string(int i) {

		StringBuffer ret = new StringBuffer();
		int rest = i;
		for (int e = this._offsetlaenge - 1; e >= 0; e--) {
			int k = rest / ((int) (Math.exp(e * Math.log(this._basis))));
			rest = rest % ((int) (Math.exp(e * Math.log(this._basis))));
			char c = (char) (this._startchar + k);
			ret.append(c);
		}
		return ret.toString();
	}

	private int string2int(String s) {

		int ret = 0;
		for (int i = 0; i < this._offsetlaenge; i++) {
			char c = s.charAt(i);
			int k = (c) - this._startchar;
			ret += k
					* ((int) Math.exp((this._offsetlaenge - i - 1)
							* Math.log(this._basis)));
		}
		return ret;
	}

	public void load(InputStream stream) {
		try {

			ObjectInputStream ois = new ObjectInputStream(stream);
			String s1 = (String) (ois.readObject());
			String s2 = (String) (ois.readObject());
			String s3 = (String) (ois.readObject());
			int sc = ((Integer) (ois.readObject())).intValue();
			int ec = ((Integer) (ois.readObject())).intValue();
			int az = ((Integer) (ois.readObject())).intValue();
			int ak = ((Integer) (ois.readObject())).intValue();
			int eow = ((Integer) (ois.readObject())).intValue();
			boolean rv = ((Boolean) (ois.readObject())).booleanValue();
			boolean ic = ((Boolean) (ois.readObject())).booleanValue();
			char[] st = (char[]) (ois.readObject());
			ois.close();

			_setStartChar(sc);
			_setEndChar(ec);
			_setAchtungZahl(az);
			_setAchtungKnoten(ak);
			setEndOfWordChar(eow);
			setReverse(rv);
			setIgnoreCase(ic);

			this._stringtree = st;
			this.wurzel = null;
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		try {
			load(new FileInputStream(filename));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}// end void load

	public static void printHelp() {

		System.out
				.println("usage: Pretree command [options] treefile [treefile] [treefile] ...");
		System.out.println();
		System.out.println("commands:");
		System.out.println("print, p\t\tprints the content of given pretrees");
		System.out
				.println("printkeys, pk\t\tprints all keys of the given pretrees");
	}

	public static void main(String[] args) {

		if (args.length < 1) {
			printHelp();
			System.exit(1);
		}
		if (args[0].equals("print") || args[0].equals("p")) {
			try {
				int i;
				for (i = 1; i < args.length; i++) {
					String filename = args[i];
					Pretree p1 = new Pretree();
					p1.load(filename);
					System.out.println("tree: " + filename);
					System.out.println("number of classes: "
							+ p1.getNrOfClasses());
					System.out.println("number of nodes: " + p1.getNrOfNodes());
					System.out.println("ignore case: " + p1.getIgnoreCase());
					System.out.println("reverse: " + p1.getReverse());
					System.out.println("entries:");
					System.out.print(p1.getAllEntriesString());
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}
		if (args[0].equals("printkeys") || args[0].equals("pk")) {
			try {
				int i;
				for (i = 1; i < args.length; i++) {
					String filename = args[i];
					Pretree p1 = new Pretree();
					p1.load(filename);
					for (Iterator it = p1.keySet().iterator(); it.hasNext();) {
						System.out.println(it.next());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}
	}

} // end class Pretree

class Knoten implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2310410212279040954L;

	public Vector classes; // classifications of node

	public String inhalt = new String(); // node substring

	public Vector kinder = new Vector(); // childen pointer

	public int pos; // offset position in char array

	public Knoten() { // constructor
		this.inhalt = "";
		this.classes = new Vector();
	} // end Constructor

	public Knoten(String neuinhalt) { // Constructor mit Inhalt
		this.inhalt = neuinhalt;
		this.classes = new Vector();
	} // end Constructor

	@Override
	public String toString() {

		StringBuffer retStringB = new StringBuffer();
		retStringB.append('[');
		retStringB.append(this.inhalt);
		retStringB.append(',');
		retStringB.append(this.classes);
		retStringB.append(']');
		return retStringB.toString();
	}

}// end class knoten
