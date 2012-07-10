/*
*   Class   FileInput
*
*   Methods for entering doubles, floats, integers,
*   long integers, booleans, Complexes,lines (as String),
*   words (as String) and chars from a text file.
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       July 2002
*   REVISED:    25 July 2004
*               11 June 2005 - Made into superclass for revised FileChooser
*               13 September 2005 - numeric input - colon and semicolon stripping added
*               30 November 2005 - stem name method
*               21 February 2006  nextWord corrected
*               1 July 2006 - access status of FileInput() constructor changed
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   FileInput.html
*
*   Copyright (c) July 2002, July 2004, September 2005
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.io;

import java.io.*;
import flanagan.complex.Complex;

public class FileInput{

        // Instance variables
        protected String fileName = " ";          //input file name
        protected String stemName = " ";          //input file name without its extension
        protected String pathName = " ";          //input file path name
        protected String dirPath = " ";           //path to directory containing input file
        protected String fullLine = " ";          //current line in input file
        protected String fullLineT = " ";         //current line in input file trimmed of trailing spaces
        protected BufferedReader input = null;    //instance of BufferedReader
        protected boolean testFullLine = false;   //false if fullLine is empty
        protected boolean testFullLineT = false;  //false if fullLineT is empty
        protected boolean eof = false;            //true if reading beyond end of file attempted
        protected boolean fileFound = true;       //true if file named is found
        protected boolean inputType = false;      //false in input type is a String
                                                  //true if input type is numeric or separated char, i.e. double, float, int, long, char
        protected boolean charType = false;       //true if input type is a separated char

        // Constructor
        // Constructor to enable sub-class FileChooser to function
        public FileInput(){
        }

        // constructor for instances of this class
        public FileInput(String pathName){
                this.pathName = pathName;
                int posSlash = pathName.indexOf("//");
                int posBackSlash = pathName.indexOf("\\");
                if(posSlash!=-1 || posBackSlash!=-1){
                    File file = new File(this.pathName);
                    this.fileName = file.getName();
                    this.dirPath = (file.getParentFile()).toString();
                }
                int posDot = this.fileName.indexOf('.');
	            if(posDot==-1){
                    this.stemName = this.fileName;
                }
                else{
                    this.stemName = this.fileName.substring(0, posDot);
                }

                try{
                        this.input = new BufferedReader(new FileReader(this.pathName));
                }catch(java.io.FileNotFoundException e){
                        System.out.println(e);
                        fileFound=false;
                }
        }

        // Methods

        // Get file path
        protected String getPathName(){
            return this.pathName;
        }

        // Get file name
        public String getFileName(){
            return this.fileName;
        }

        // get file name without the extension
        public String getStemName(){
            return this.stemName;
        }

        // Get path to directory containing the file
        public String getDirPath(){
            return this.dirPath;
        }

        // Reads a double from the file
        public final synchronized double readDouble(){
                this.inputType = true;
                String word="";
                double dd=0.0D;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)dd = Double.parseDouble(word.trim());

                return dd;
        }

        // Reads a float from the file
        public final synchronized float readFloat(){
                this.inputType = true;
                String word="";
                float ff=0.0F;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ff = Float.parseFloat(word.trim());

                return ff;
        }

        // Reads an integer (int) from the file
        public final synchronized int readInt(){
                this.inputType = true;
                String word="";
                int ii=0;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ii = Integer.parseInt(word.trim());

                return ii;
        }

        // Reads a long integer from the file
        public final synchronized long readLong(){
                this.inputType = true;
                String word="";
                long ll=0L;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();
                if(!eof)ll = Long.parseLong(word.trim());

                return ll;
        }

        // Reads a Complex from the file
        // accepts strings 'real''sign''x''imag' and 'real''sign''x''.''imag'
        // where x may be i or j
        // and sign may be + or -
        // e.g.  2+j3, 2+i3,
        // no spaces are allowed within the complex number
        // e.g.  2 + j3, 2 + i3 are NOT allowed
        public final synchronized Complex readComplex(){

                this.inputType = true;
                String word="";
                Complex cc = null;

                if(!this.testFullLineT) this.enterLine();
                word = nextWord();

                if(!eof)cc = Complex.parseComplex(word.trim());
                return cc;
        }

        // Reads a boolean from the file
        public final synchronized boolean readBoolean(){

                boolean retB = true;
                String retS = this.readWord();
                if(retS.equals("false") || retS.equals("FALSE")){
                    retB = false;
                }
                else{
                    if(retS.equals("true") || retS.equals("TRUE")){
                        retB = true;
                    }
                    else{
                        throw new IllegalArgumentException("attempted input neither true nor false");
                    }
                }
                return retB;
        }

        // Reads a word (a string between spaces) from the file
        public final synchronized String readWord(){
                this.inputType = false;
                String word="";

                if(!this.testFullLineT) this.enterLine();
                if(this.fullLine.equals("")){
                    word="";
                }else
                {
                    word = nextWord();
                }

                return word;
        }

        // Public method for reading a line from the file
        public final synchronized String readLine(){
            this.inputType = false;
            return this.readLineL();
        }

        // Protected method for reading a line from the file
        protected final synchronized String readLineL(){
                String line="";
                try{
                        line = input.readLine();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
               if(line==null){
                    System.out.println("Attempt to read beyond the end of the file");
                    eof=true;
                    line="";
                }
                return line;
        }

        // Reads a character, preceeded and followed by space, comma, colon or comma, from the file
        public final synchronized char readChar(){
                inputType = true;
                charType = true;
                String word="";
                char ch=' ';

                if(!this.testFullLine) this.enterLine();
                word = nextWord();
                if(word.length()!=1)throw new IllegalArgumentException("attempt to read more than one character into type char");
                if(!eof)ch = word.charAt(0);
                return ch;
        }

        // Close file
        public final synchronized void close(){
            if(fileFound){
                try{
                        input.close();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }
            }
        }

        // Get the end of file status, eof.
        public boolean eof(){
            return eof;
        }

        // Get the file existence status, fileFound.
        public boolean fileFound(){
            return fileFound;
        }

        // enters a line from the file into the fullLine and fullLineT strings
        protected final synchronized void enterLine(){
                int i=0;

                this.fullLine=this.readLineL();
                this.fullLineT=this.fullLine;
                if(!this.fullLine.equals("")){
                    i=this.fullLineT.length()-1;
                    while(this.fullLineT.charAt(i)==' ' && i>=0){
                            this.fullLineT=this.fullLineT.substring(0,i);
                            i--;
                    }
                }
        }

        // reads the next word (a string between spaces) from the String fullLine
        protected final synchronized String nextWord(){
                this.testFullLine=true;
                this.testFullLineT=true;
                String  word = "";
                int     posspa=-1, postab=-1, possp=-1, poscom=-1, poscol=-1, possem=-1;
                boolean test = true;
                int len=this.fullLine.length();

                // strip end of the word of any leading spaces, tabs or, if numerical input, commas, colons or semicolons
                boolean test0 = true;
                boolean test1 = false;
                int pend =this.fullLine.length();
                while(test0){
                    pend--;
                    if(this.fullLine.charAt(pend)==' ')test1=true;
                    if(this.fullLine.charAt(pend)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(pend)==',')test1=true;
                        if(this.fullLine.charAt(pend)==':')test1=true;
                        if(this.fullLine.charAt(pend)==';')test1=true;
                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(0,pend);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // strip front of the word of any leading spaces, tabs or, if numerical input, commas, colons or semicolons
                test0 = true;
                test1 = false;
                while(test0){
                    if(this.fullLine.charAt(0)==' ')test1=true;
                    if(this.fullLine.charAt(0)=='\t')test1=true;
                    if(inputType){
                        if(this.fullLine.charAt(0)==',')test1=true;
                        if(this.fullLine.charAt(0)==':')test1=true;
                        if(this.fullLine.charAt(0)==';')test1=true;

                    }
                    if(test1){
                        this.fullLine = this.fullLine.substring(1);
                    }
                    else{
                        test0=false;
                    }
                    test1=false;
                }

                // find first space, tab or, if numeric, comma, colon or semicolon
                int lenPlus = this.fullLine.length() + 10;
                posspa=this.fullLine.indexOf(' ');
                postab=this.fullLine.indexOf('\t');
                int firstMin = lenPlus;
                int secondMin = lenPlus;
                int thirdMin = lenPlus;
                if(posspa==-1 && postab==-1){
                    firstMin = lenPlus;
                }
                else{
                    if(posspa==-1){
                        firstMin = postab;
                    }
                    else{
                        if(postab==-1){
                            firstMin = posspa;
                        }
                        else{
                            firstMin = (int)Math.min(posspa, postab);
                        }
                    }
                }
                if(this.inputType){
                    poscom=this.fullLine.indexOf(',');
                    poscol=this.fullLine.indexOf(':');
                    possem=this.fullLine.indexOf(';');
                    if(poscom==-1 && poscol==-1){
                        secondMin = lenPlus;
                    }
                    else{
                        if(poscom==-1){
                            secondMin = poscol;
                        }
                        else{
                            if(poscol==-1){
                                secondMin = poscom;
                            }
                            else{
                                secondMin = (int)Math.min(poscom, poscol);
                            }
                        }
                    }
                    if(possem==-1){
                        thirdMin = lenPlus;
                    }
                    else{
                        thirdMin = possem;
                    }
                    secondMin = (int)Math.min(secondMin, thirdMin);
                    firstMin = (int)Math.min(firstMin, secondMin);

                }


                // remove first word first word from string
                if(firstMin==lenPlus){
                        word=this.fullLine;
                        this.fullLine="";
                        this.testFullLine=false;
                }
                else{
                        word=this.fullLine.substring(0,firstMin);

                        if(firstMin+1>this.fullLine.length()){
                                this.fullLine="";
                                this.testFullLine=false;
                        }
                        else{
                                this.fullLine=this.fullLine.substring(firstMin+1);
                                if(this.fullLine.length()==0)this.testFullLine=false;
                        }
                }
                if(this.testFullLineT){
                        if(!this.testFullLine){
                                this.testFullLineT=false;
                                this.fullLineT="";
                         }
                         else{
                                if(firstMin+1>this.fullLineT.length()){
                                        this.fullLineT="";
                                        this.testFullLineT=false;
                                }
                        }
                }

                // return first word of the supplied string
                return word;
        }

       // reads the next char from the String fullLine
        protected final synchronized char nextCharInString(){
                this.testFullLine=true;
                char  ch=' ';
                boolean test = true;

                ch=this.fullLine.charAt(0);
                this.fullLine=this.fullLine.substring(1);
                if(this.fullLine.length()==0)this.testFullLine=false;
                if(this.testFullLineT){
                        this.fullLineT=this.fullLineT.substring(1);
                        if(this.fullLineT.length()==0)this.testFullLineT=false;
                }

                return ch;
        }
}
