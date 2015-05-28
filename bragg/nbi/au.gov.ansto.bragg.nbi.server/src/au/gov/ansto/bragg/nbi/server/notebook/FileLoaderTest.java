package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.gumtree.service.db.RecordReader;
import org.gumtree.service.db.RecordWriter;
import org.gumtree.service.db.RecordsFile;
import org.gumtree.service.db.RecordsFileException;
import org.gumtree.service.db.TextDb;


public class FileLoaderTest {

	
	public static RecordsFile createFile(String filename) {
		RecordsFile file = null;
		File f = new File(filename);
		if (!f.exists()) {
			try {
				file = new RecordsFile(filename, 1024);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RecordsFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("file exists");
		}
		return file;
	}
	
	public static void writeFile(String filename) {
		RecordsFile file = null;
		File f = new File(filename);
		if (!f.exists()) {
			try {
				file = new RecordsFile(filename, 1024);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RecordsFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				file = new RecordsFile(filename, "rws");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RecordsFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < 10 ; i++) {
			String key = "Key" + i;
			RecordWriter rw = new RecordWriter(key);
			try {
				rw.writeObject("hello" + i);
				file.insertRecord(rw);
			} catch (RecordsFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public static void readFile(String filename) {
		RecordsFile file = null;
		try {
			file = new RecordsFile(filename, "rws");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RecordsFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < 10 ; i++) {
			String key = "Key" + i;
			RecordReader rr;
			try {
				rr = file.readRecord(key);
				System.out.println(rr.readObject());
			} catch (RecordsFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}
	
	public static void readRecord(String filename, int index) {
		RecordsFile file = null;
		try {
			file = new RecordsFile(filename, "rws");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RecordsFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RecordReader rr;
		try {
			rr = file.readRecord(index);
			System.out.println(rr.readObject());
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readRecord(String filename, String key) {
		RecordsFile file = null;
		try {
			file = new RecordsFile(filename, "rws");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RecordsFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RecordReader rr;
		try {
			rr = file.readRecord(key);
			System.out.println(rr.readObject());
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getIndex(String filename, String key) {
		TextDb db = null;
		try {
			db = new TextDb(filename, "rws");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RecordsFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int index = -1;
		try {
			index = db.keyToIndex(key);
			System.out.println(index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void makeDb(String orgFile, String tgtFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(orgFile));
			TextDb db = new TextDb(tgtFile, 1024);
			String line;
			boolean start = false;
			String text = "";
			int index = 0;
			while((line = reader.readLine()) != null) {
				if (start) {
					text += line;
					if (line.trim().endsWith("</div>")) {
						String key = "div_" + index;
						RecordWriter rw = new RecordWriter(key);
						rw.writeObject(text);
						db.insertRecord(rw);
						text = "";
						start = false;
						index ++;
					}
				}
				if (!start) {
					if (line.trim().startsWith("<div ")){
						text += line;
						start = true;
						if (line.trim().endsWith("</div>")){
							String key = "div_" + index;
							RecordWriter rw = new RecordWriter(key);
							rw.writeObject(text);
							db.insertRecord(rw);
							text = "";
							start = false;
							index ++;
						}
					}
				}
			}
			reader.close();
			db.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void appendDb(String orgFile, String tgtFile, int number) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(orgFile));
			TextDb db = new TextDb(tgtFile, "rw");
			String line;
			boolean start = false;
			String text = "";
			int count = db.getNumRecords();
			db.deleteRecord(count - 1);
			int index = 0;
			while((line = reader.readLine()) != null) {
				if (start) {
					text += line;
					if (line.trim().endsWith("</div>")) {
						String key = "div_" + (count + index);
						RecordWriter rw = new RecordWriter(key);
						rw.writeObject(text);
						db.insertRecord(rw);
						text = "";
						start = false;
						index ++;
						if (index >= number) {
							break;
						}
					}
				}
				if (!start) {
					if (line.trim().startsWith("<div ")){
						text += line;
						start = true;
						if (line.trim().endsWith("</div>")){
							String key = "div_" + (count + index);
							RecordWriter rw = new RecordWriter(key);
							rw.writeObject(text);
							db.insertRecord(rw);
							text = "";
							start = false;
							index ++;
							if (index >= number) {
								break;
							}
						}
					}
				}
			}
			reader.close();
			db.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filename = "W:/data/current/notebook/db.rdf";
//		readFile(filename);
//		readRecord(filename, "Key4");
//		getIndex(filename, "Key7");
//		makeDb("W:/data/current/notebook/db.xml", filename);
		appendDb("W:/data/current/notebook/db.xml", filename, 1);
	}

}
