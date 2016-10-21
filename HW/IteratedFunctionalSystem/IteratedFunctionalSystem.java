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
import java.lang.Math;
import java.util.Random;

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

	private int imgWidth;
	private int imgHeight;

	private int backgroundColor;
	private int foregroundColor;

	// use an array list to store the affine transforms and matrix weights 
	// defined in the desctiption file since the number of lines (number of tforms) is unknown;
	// if the weights aren't specified in the file, they will be computed
	ArrayList<AffineTransform> aff_tforms = new ArrayList<AffineTransform>();
	ArrayList<Double> weights = new ArrayList<Double>();

	private int numGens; // number of generations in IFS algorithm

	private Random rand; // declare Random object

	//========================================
	// constructor
	public ImageFrame(int width, int height) {
		rand = new Random(); // create new Random object

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
					promptUserForFile(); // get & assign ifsDescriptionFile
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
					// when selected...
					promptUserForImgSize(); // get image width & height
					promptUserForColors(); // get background & foreground colors as hex values
					BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
				}
			}	);
		fileMenu.add(configImgItem);		

		// -------------------------------------------------------
		// --- Display IFS
		JMenuItem displayIFSItem = new JMenuItem("Display IFS");
		displayIFSItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// when selected...
					promptUserForNumGens();	
					IFS(); // Iterated Functional System algorithm
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

	// ==================================
	// promptUserForImgSize(): user will
	// input string values for the image
	// width and height that are 
	// interpreted as ints and assigned
	// to the corresponding class params
	private void promptUserForImgSize() {
		String result = JOptionPane.showInputDialog("Enter the image width"); // get String input from option pane
		int width = 0; // initialize to zero before checking correct format

		try {
			width = Integer.parseInt(result); // convert width to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (width < 0) {
			// don't allow negative
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		this.imgWidth = width;

		result = JOptionPane.showInputDialog("Enter the image height"); // get String input from option pane
		int height = 0; // initialize to zero before checking correct format

		try {
			height = Integer.parseInt(result); // convert height to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (height < 0) {
			// don't allow negative
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		this.imgHeight = height;
	}

	// =================================
	// promptUserForColor(): user will 
	// input hex value as string that is
	// then interpreted as int
	private void promptUserForColors() {
		String hexStr = JOptionPane.showInputDialog("Enter the desired hex RGB value for the image background color."); // get String input from option pane
		int val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		this.backgroundColor = val;

		hexStr = JOptionPane.showInputDialog("Enter the desired hex RGB value for the image foreground color."); // get String input from option pane
		val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		this.foregroundColor = val;
	}

	// ==================================
	// promptUserForNumGens(): user will
	// input string value for the number
	// of generations for the IFS 
	// algorithm to run; the value will 
	// interpreted as an int and assigned
	// to the corresponding class param
	private void promptUserForNumGens() {
		String result = JOptionPane.showInputDialog("Enter the number of generations"); // get String input from option pane
		int val = 0; // initialize to zero before checking correct format

		try {
			val = Integer.parseInt(result); // convert val to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (val < 0) {
			// don't allow negative
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		this.numGens = val;
	}

	// ===============================
	// loadMatrixEntries() - 
	private void loadMatrixEntries() {
		String matrixEntries = null; // each line will represent entries of a transform matrix
		double[] entries; // initialize scanned matrix entries
		boolean seven = false; // only true if 7 values scanned on first line of description file	

		try {
			BufferedReader br = new BufferedReader(new FileReader(this.ifsDescriptionFile));
			Scanner sc;
			int i;
			// each line will have either 6 or 7 entries (either all lines with have 6 entries 
			// or all lines will have 7 entries), where the first 6 are the matrix entries 
			// a,b,c,d,e,f as defined in "Computational Beauty of Nature":
			// | a b e |   and the 7th entry is an optional weight that represents the
			// | c d f | , probability that matrix defined by the prev 6 entries will be
			// | 0 0 1 |   selected in the IFS algorithm;
			// we test the first line of the description file to see if there 
			// will be 6 or 7 entries to read on each line:
			if ((matrixEntries = br.readLine()) != null) {
				sc = new Scanner(matrixEntries);
				// assume 7 entries per line; values in the entries array are initialized
				// to 0.0 & we know that the weights (if defined) will be nonzero
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
				// line will only have 6 entries (no weight), so we can redefine the size
				// of the entries array to 6
				if (entries[6] == 0.0) {
					entries = new double[6];
				}
				// if the 7th value is nonzero, then adjust boolean value so the 
				// weights are scanned and stored in the next step
				else {
					seven = true;				
				}
			}
			// the entries array is now of appropriate size, so we iterate through the
			// remaining lines in the file and build/save the corresponding AffineTransforms
			// and (depending on boolean seven) save the weights or compute them
			if (seven) { // store affine tforms AND weights
				while ((matrixEntries = br.readLine()) != null) {
					sc = new Scanner(matrixEntries);
					entries = new double[7];
					i = 0;
					while (sc.hasNextDouble()) {
						entries[i] = sc.nextDouble();
						i++;
					}
					// create/add new affine tform
					this.aff_tforms.add(matrixEntriesToAffineTform(entries));
					this.weights.add(entries[6]);
				}
				// make each weight an accumulation of all previous weights 
				for (i = 1; i < this.weights.size(); i++) {
					weights.set(i, weights.get(i-1) + weights.get(i));
				}
			}
			else { // store affine tforms then compute weights
				while ((matrixEntries = br.readLine()) != null) {
					sc = new Scanner(matrixEntries);
					entries = new double[7];
					i = 0;
					while (sc.hasNextDouble()) {
						entries[i] = sc.nextDouble();
						i++;
					}
					// create/add new affine tform
					this.aff_tforms.add(matrixEntriesToAffineTform(entries));					
				}
				// compute weights of each newly created affine tform
				this.weights = computeWeights(aff_tforms);
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
	private AffineTransform matrixEntriesToAffineTform(double[] mat) {
		AffineTransform at = new AffineTransform(mat[0],mat[2],mat[1],mat[3],mat[4],mat[5]);
		return at;
	}

	// ==============================================================================
	// computeWeights(): 
	private ArrayList<Double> computeWeights(ArrayList<AffineTransform> aff_tforms) {
		int i; // loop counter

		// compute determinants of each affine tform
		double[] determs = new double[aff_tforms.size()];
		for (i = 0; i < aff_tforms.size(); i++) {
			if (aff_tforms.get(i).getDeterminant() != 0) {
				determs[i] = aff_tforms.get(i).getDeterminant();
			}
			else { // fudge the zero value so DBZ error doesn't occur
				determs[i] = 0.01;	
			}
		}

		// compute sum of all determinants
		double determs_sum = 0;
		for (i = 0; i < aff_tforms.size(); i++) {
			determs_sum += Math.abs(determs[i]);
		}		

		// compute weights and store as accumulations of all previous weights
		ArrayList<Double> temp_weights = new ArrayList<Double>(aff_tforms.size());
		temp_weights.add(Math.abs(determs[0])/determs_sum);
		for (i = 1; i < aff_tforms.size(); i++) {
			temp_weights.add((Math.abs(determs[i])/determs_sum) + temp_weights.get(i-1));
		}		

		return temp_weights;
	}

	// =================
	// IFS(): 
	private void IFS() {

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
