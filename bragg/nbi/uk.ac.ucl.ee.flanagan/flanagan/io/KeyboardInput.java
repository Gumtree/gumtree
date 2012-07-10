/*
*   Class   KeyboardInput
*
*   Methods for entering doubles, floats, integers,
*   long integers, strings, chars, booleans and Complexes from the key board
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    July 2002
*   REVISED: 26 July 2004
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   KeyboardInput.html
*
*   Copyright (c) July 2002, July 2004
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

public class KeyboardInput
{
        // Data variable - buffered stream for the keyboard
        private BufferedReader input = null;

        // Constructor
        public KeyboardInput(){

                this.input = new BufferedReader(new InputStreamReader(System.in));
        }

        // Reads a double from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized double readDouble(String mess){
                String line="";
                double d=0.0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                d = Double.parseDouble(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid double\nRe-enter the number");
                        }
                }

                return d;
        }

        // Reads a double from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized double readDouble(String mess, double dflt){
                String line="";
                double d=0.0D;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                d = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        d = Double.parseDouble(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid double\nRe-enter the number");
                                }
                        }
                }
                return d;
        }

        // Reads a double from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized double readDouble(){
                String line="";
                double d=0.0D;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                d = Double.parseDouble(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid double\nRe-enter the number");
                        }
                }

                return d;
        }

        // Reads a float from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized float readFloat(String mess){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                f = Float.parseFloat(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid float\nRe-enter the number");
                        }
                }

                return f;
        }

        // Reads a float from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized float readFloat(String mess, float dflt){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                f = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        f = Float.parseFloat(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid float\nRe-enter the number");
                                }
                        }
                }
                return f;
        }

        // Reads a float from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized float readFloat(){
                String line="";
                float f=0.0F;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                f = Float.parseFloat(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid float\nRe-enter the number");
                        }
                }

                return f;
        }

        // Reads an int (integer) from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized int readInt(String mess){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ii = Integer.parseInt(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid int\nRe-enter the number");
                        }
                }

                return ii;
        }

        // Reads an int (integer) from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized int readInt(String mess, int dflt){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                ii = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        ii = Integer.parseInt(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid int\nRe-enter the number");
                                }
                        }
                }
                return ii;
        }

        // Reads an int (integer) from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized int readInt(){
                String line="";
                int ii = 0;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ii = Integer.parseInt(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid int\nRe-enter the number");
                        }
                }

                return ii;
        }

        // Reads a long integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized long readLong(String mess){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ll = Long.parseLong(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid long\nRe-enter the number");
                        }
                }

                return ll;
        }

        // Reads a long integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized long readLong(String mess, long dflt){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.length()==0){
                                ll = dflt;
                                finish = true;
                        }
                        else{
                                try{
                                        ll = Long.parseLong(line.trim());
                                        finish=true;
                                }catch(NumberFormatException e){
                                        System.out.println("You did not enter a valid long\nRe-enter the number");
                                }
                        }
                }
                return ll;
        }

        // Reads a long integer from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized long readLong(){
                String line="";
                long ll = 0L;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        try{
                                ll = Long.parseLong(line.trim());
                                finish=true;
                        }catch(NumberFormatException e){
                                System.out.println("You did not enter a valid long\nRe-enter the number");
                        }
                }

                return ll;
        }

        // Reads a long integer from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized char readChar(String mess){
                String line="";
                char ch=' ';
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a long integer from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized char readChar(String mess, char dflt){
                String line="";
                char ch=' ';
                boolean finish = false;

                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a char from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized char readChar(){
                String line = "";
                char ch = ' ';
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                line = this.enterLine();
                line = line.trim();
                ch = line.charAt(0);

                return ch;
        }

        // Reads a boolean from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized boolean readBoolean(String mess){
                String line="";
                boolean b=false;
                boolean finish = false;

                System.out.print(mess + " ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a boolean from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized boolean readBoolean(String mess, boolean dflt){
                String line="";
                boolean b=false;
                boolean finish = false;
                System.out.print(mess + " [default value = " + dflt + "] ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a boolean from the keyboard
        // No prompt message, No default option
        // Input terminated by new line return
        public final synchronized boolean readBoolean(){
                String line="";
                boolean b=false;
                boolean finish = false;

                System.out.print(" ");
                System.out.flush();

                while(!finish){
                        line = this.enterLine();
                        if(line.trim().equals("true") || line.trim().equals("TRUE")){
                            b = true;
                            finish = true;
                        }
                        else{
                            if(line.trim().equals("false") || line.trim().equals("FALSE")){
                                b = false;
                                finish=true;
                            }
                            else{
                                System.out.println("You did not enter a valid boolean\nRe-enter the number");
                            }
                        }
                }
                return b;
        }

        // Reads a Complex from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess){
                return Complex.readComplex(mess);
        }

        // Reads a Complex from the keyboard with a prompt message and Stringdefault value option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess, String dflt){
                return Complex.readComplex(mess, dflt);
        }

        // Reads a Complex from the keyboard with a prompt message and Complexdefault value option
        // Input terminated by new line return
        public final synchronized Complex readComplex(String mess, Complex dflt){
                return Complex.readComplex(mess, dflt);
        }


        // Reads a Complex from the keyboard
        // No prompt
        // No default option
        // Input terminated by new line return
        public final synchronized Complex readComplex(){
                return Complex.readComplex();
        }

        // Reads a line from the keyboard with a prompt message
        // No default option
        // Input terminated by new line return
        public final synchronized String readLine(String mess){
                System.out.print(mess + " ");
                System.out.flush();

                return this.enterLine();
        }

        // Reads a line from the keyboard with a prompt message and the return
        // of a default option if the return key alone is pressed
        // Input terminated by new line return
        public final synchronized String readLine(String mess, String dflt){
                String line = "";
                System.out.print(mess + " [default option = " + dflt + "] ");
                System.out.flush();

                line = this.enterLine();
                if(line.length()==0)line = dflt;

                return line;
        }

        // Reads a line from the keyboard
        // No prompt message, no default option
        // Input terminated by new line return
        private final synchronized String readLine(){

                return this.enterLine();
        }

        // Enters a line from the keyboard
        // Private method called by public methods
        private final synchronized String enterLine(){
                String line = "";

                try{
                        line = input.readLine();
                }catch(java.io.IOException e){
                        System.out.println(e);
                }

                return line;
        }
}
