// IteratedFunctionalSystem.java
//
// Author: Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Scanner;

public class IteratedFunctionalSystem {
	private static final int frameWIDTH  = 750; // ImageFrame width
	private static final int frameHEIGHT = 750; // ImageFrame height

	public static void main(String[] args) {
		JFrame frame = new ImageFrame(frameWIDTH, frameHEIGHT); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.setVisible(true); // make frame visible
	}
}

//###########################################################################

class ImageFrame extends JFrame {
	private final JFileChooser chooser; // to opens files
	private File ifsDescriptionFile; // will contain tform matrix entries

	//========================================
	// constructor
	public ImageFrame(int width, int height) {
		// setup the frame's attributes
		this.setTitle("CAP 3027 2016 - HW07 - Brandon Peterson");
		this.setSize(width, height);

		addMenu(); // add a menu to the frame

		// setup the file chooser dialog
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));		
	}

	private void addMenu() {
		// ================================
		// === File menu
		JMenu fileMenu = new JMenu("File");

		// ------------------------------------------------------------------------
		// --- Load MRCM description
		JMenuItem loadIFSDescriptionItem = new JMenuItem("Load IFS Description");
		loadIFSDescriptionItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// when selected...
					System.out.println("prompt");
					promptUserForFile(); // get & assign ifsDescriptionFile
					System.out.println("load");
					loadMatrixEntries(); // parse IFS description file for tform matrix entries
				}
			}	);
		fileMenu.add(loadIFSDescriptionItem);

		// --------------------------------------------------------
		// --- Configure image
		JMenuItem configImgItem = new JMenuItem("Configure image");
		configImgItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// actions to perform				
				}
			}	);
		fileMenu.add(configImgItem);		

		// -------------------------------------------------------
		// --- Display IFS
		JMenuItem displayIFSItem = new JMenuItem("Display IFS");
		displayIFSItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// actions to perform				
				}
			}	);
		fileMenu.add(displayIFSItem);		

		// -------------------------------------------------
		// --- Save image
		JMenuItem saveImgItem = new JMenuItem("Save image");
		saveImgItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// actions to perform				
				}
			}	);
		fileMenu.add(saveImgItem);		

		// ----------------------------------------
		// --- Exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					System.exit(0);
				}
			}	);
		fileMenu.add(exitItem);
		
		// attach menus to a menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}

	// ========================================================
	// promptUserForFile() - user chooses IFS description file
	private void promptUserForFile() {
		File file = getFile();
		if (file != null) {
			this.ifsDescriptionFile = file;
		}
	}

	// ============================================
	// getFile() - open a file selected by the user
	private File getFile() {
		File file = null;

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}

		return file;
	}

	// ===============================
	// loadMatrixEntries() - 
	private void loadMatrixEntries() {
		String matrixEntries = null; // each line will represent entries of a transform matrix
		double[] entries; // initialize scanned matrix entries
		boolean seven = false; // only true if 7 values scanned on first line of description file
		// use an array list to store the affine transforms and (possibly) probability params 
		//defined in the desctiption file since the number of lines (number of tforms) is unknown
		ArrayList<AffineTransform> aff_tforms = new ArrayList<AffineTransform>();
		ArrayList<Double> probs = new ArrayList<Double>();	

		try {
			BufferedReader br = new BufferedReader(new FileReader(this.ifsDescriptionFile));
			System.out.println("reading file...");
			Scanner sc;
			int i;
			// each line will have either 6 or 7 entries (either all lines with have 6 entries 
			// or all lines will have 7 entries), where the first 6 are the matrix entries 
			// a,b,c,d,e,f as defined in "Computational Beauty of Nature":
			// | a b e |   and the 7th entry is an optional probability that represents the
			// | c d f | , probability that matrix defined by the prev 6 entries will be
			// | 0 0 1 |   selected in the IFS algorithm;
			// we test the first line of the description file to see if there 
			// will be 6 or 7 entries to read on each line:
			if ((matrixEntries = br.readLine()) != null) {
				System.out.println("scanning first line...");
				sc = new Scanner(matrixEntries);
				// assume 7 entries per line; values in the entries array are initialized
				// to 0.0 & we know the probability parameters (if defined) will be nonzero
				entries = new double[7]; 
				i = 0;
				// store values on first line in entries array and create affine tform
				while (sc.hasNextDouble()) { 
					entries[i] = sc.nextDouble();
					i++;
				}
				// create/add new affine tform
				aff_tforms.add(matrixEntriesToAffineTform(entries));

				// if the 7th value in the array is zero, then we can conclude that each 
				// line will only have 6 entries (no probability parameter), so we can
				// redefine the size of the entries array to 6
				if (entries[6] == 0.0) {
					System.out.println("6 entries scanned");
					entries = new double[6];
				}
				// if the 7th value is nonzero, then adjust boolean value so the 
				// probability params are stored in the next step
				else {
					System.out.println("7 entries scanned"); 
					seven = true;				
				}
			}
			// the entries array is now of appropriate size, so we iterate through the
			// remaining lines in the file and build/save the corresponding AffineTransforms
			// and (depending on boolean seven) save the probability parameters
			if (seven) { // store affine tforms AND probability params
				while ((matrixEntries = br.readLine()) != null) {
					System.out.println("scanning next line...");
					sc = new Scanner(matrixEntries);
					entries = new double[7];
					i = 0;
					while (sc.hasNextDouble()) {
						entries[i] = sc.nextDouble();
						i++;
					}
					System.out.println("entries array created");
					// create/add new affine tform
					aff_tforms.add(matrixEntriesToAffineTform(entries));
					probs.add(entries[6]);

					// DEGUG TEST
					for (int j = 0; j < 7; j++) {
						System.out.println(entries[j]);
					}
				}
			}
			else { // store affine tforms (no probability params)
				while ((matrixEntries = br.readLine()) != null) {
					sc = new Scanner(matrixEntries);
					entries = new double[7];
					i = 0;
					while (sc.hasNextDouble()) {
						entries[i] = sc.nextDouble();
						i++;
					}
					// create/add new affine tform
					aff_tforms.add(matrixEntriesToAffineTform(entries));

					// DEGUG TEST
					for (int j = 0; j < 7; j++) {
						System.out.println(entries[j]);
					}					
				}				
			}
		} catch (FileNotFoundException e1) {
			System.out.println("File not found!");
		} catch (IOException e2) {
			System.out.println(e2);
		}
	}


	// ==============================================================
	// matrixEntriesToAffineTform(): creates and returns an 
	// AffineTransform given an array of matrix entries
	//
	// params: double[] mat holds a,b,c,d,e,f which form the matrix:
	// | a b e |   
	// | c d f | 
	// | 0 0 1 |
	// AffineTransforms are constructed by defining
	// m00, m10, m01, m11, m02, m12, which form the matrix:
	// | m00 m01 m02 |
	// | m10 m11 m12 |
	// |  0   0   1  |	
	public AffineTransform matrixEntriesToAffineTform(double[] mat) {
		AffineTransform at = new AffineTransform(mat[0],mat[2],mat[1],mat[3],mat[4],mat[5]);
		return at;
	}

/*
	
	// ---------------------------------------------------------------
	// displayBufferedImage() - displayes an image in the content pane
	// 	params: BufferedImage image = image to display
	public void displayBufferedImage(BufferedImage image) {
		// display resulting image
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}

*/

}

// create AT and double probability ArrayLists since num lines of description file is unknown
// include AT
