package stss.qlearningproject.module.qlearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author tsimonfine
 *
 */
public class QExportImport {
	private boolean filewasinit = false;
	private File file;

	/**
	 * @param filename
	 */
	public QExportImport(String filename) {
		this.file = new File(filename);
		initFile();
	}

	/**
	 * @return The file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to import/export QLearning object
	 */
	public void setFile(File file) {
		this.file = file;
		initFile();
	}

	/**
	 * Init the file (create file and parent dirs)
	 */
	public void initFile() {
		try {
			if (this.file.getParentFile() != null)
				this.file.getParentFile().mkdirs();
			this.file.createNewFile();
			this.filewasinit = true;
		} catch (IOException e) {
			System.out.println("File not writable, file isn't init, try another with 'setfile()'");
			e.printStackTrace();
		}
	}

	/**
	 * @param q
	 */
	public void saveQlearning(QLearning q) {
		if (filewasinit) {
			try {
				FileOutputStream f = new FileOutputStream(this.file, false);
				ObjectOutputStream o = new ObjectOutputStream(f);

				// Write objects to file
				o.writeObject(q);

				o.close();
				f.close();
			} catch (FileNotFoundException e) {
				System.out.println("File not found");
			} catch (IOException e) {
				System.out.println("Error initializing stream");
			}
		} else {
			System.out.println("ERROR: Try to save object to an inexistent file");
		}
	}

	/**
	 * @return
	 */
	public QLearning getQlearning() {
		QLearning q = null;
		if (filewasinit) {
			try {
				FileInputStream fi = new FileInputStream(this.file);
				ObjectInputStream oi = new ObjectInputStream(fi);

				// Read objects
				q = (QLearning) oi.readObject();

				oi.close();
				fi.close();
			} catch (FileNotFoundException e) {
				System.out.println("File not found");
			} catch (IOException e) {
				System.out.println("Error initializing stream");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Try to get an object from inexistent file");
		}

		return q;
	}
}
