import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

public class LSystem3D {
	private static final int frameWidth  = 750; // ImageFrame width
	private static final int frameHeight = 750; // ImageFrame height

	// make sure EDT handles display of GUI
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		}	);
	}

	public static void createAndShowGUI() {
		JFrame frame = new ImageFrame(frameWidth, frameHeight); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.validate();
		frame.setVisible(true); // make frame visible
	}
}

//###########################################################################

class ImageFrame extends JFrame {
	/*
		parameters to setup image creation and description file selection
	*/
	private final int imgWidth  = 500;
	private final int imgHeight = 500;
	BufferedImage image = null;
	Graphics2D g2D;
	private final JFileChooser chooser; // to open L-System description file
	private File description_file;	

	/*
		parameters parsed from L-system description file
	*/
	private double delta; // angle in degrees
	private int scale_factor; // adjusts growth unit based on desired dispalyed generation
	private String initiator; // axiom
	// up to 10 production rules can be specified per L-System:
	// rules hashmap will have keys representing symbols that have replacement strings,
	// and the corresponding values will be those replacement strings
	private HashMap<Character, String> production_rules = new HashMap<Character, String>(10);	

	/*
		parameters defined immediately following assignment of delta
	*/
	private double cos_delta, sin_delta; // will be assigned after delta is parsed
	// 3x3 rotation matrices (+/- directions) about H-axis, assigned after delta is parsed
	private double[][] h_pos_rot_mat; // +delta used
	private double[][] h_neg_rot_mat; // -delta used
	// 3x3 rotation matrices (+/- directions) about L-axis, assigned after delta is parsed
	private double[][] l_pos_rot_mat; // +delta used
	private double[][] l_neg_rot_mat; // -delta used
	// 3x3 rotation matrices (+/- directions) about U-axis, assigned after delta is parsed
	private double[][] u_pos_rot_mat; // +delta used
	private double[][] u_neg_rot_mat; // -delta used

	/*
		parameters defined by user
	*/
	private int num_gens; // number of generations displayed
	private int background_color;
	private int foreground_color; // color used to draw fractals
	private double base_segment_length; // growth unit adjusted by scaling factor

	/*
		parameters to define turtle's inital pose
		// may let user define these in future
	*/
	// turtle's inital position:
	// movements defined by orientation (see below) will begin from this location
	private double[] init_pos = new double[] {0,0,125};
	// turtle's inital orientation:
	// defined by a 3x3 matrix, where 
	// column1 is the turtle's heading,
	// column2 is the direction to the left of that heading,
	// column3 is the direction up of that heading;
	// each vector has unit length and H x L = U
	private double[] init_heading  = new double[] {0.0,1.0,0.0};  // +y direction
	private double[] init_left_dir = new double[] {1.0,0.0,0.0};  // +x direction
	private double[] init_up_dir   = new double[] {0.0,0.0,-1.0}; // -z direction
	private double[][] init_orientation = new double[][] {
		{init_heading[0], init_left_dir[0], init_up_dir[0]},
		{init_heading[1], init_left_dir[1], init_up_dir[1]},
		{init_heading[2], init_left_dir[2], init_up_dir[2]}
	};

	/*
		parameters used in projection of 3D points to 2D points
		// will need to be tweaked to produce best looking image
	*/
	private final double focal_length = 275.0;
	private final double x_cam = 50.0;
	private final double y_cam = 150.0;		

	//==========================================
	// constructor
	public ImageFrame(int width, int height) {
		// setup the frame's attributes
		this.setTitle("3D L-Systems");
		this.setSize(width, height);

		this.image = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_ARGB);
		this.g2D = (Graphics2D) this.image.createGraphics();

		// setup the file chooser dialog
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));			

		addMenu(); // add a menu to the frame
	}

	private void addMenu() {
		// ================================
		// === File menu
		JMenu fileMenu = new JMenu("File");	

		// --------------------------------------------------------
		// Load L-System
		JMenuItem loadLSystemItem = new JMenuItem("Load L-System");
		loadLSystemItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// when selected...
					promptUserForFile(); // get & assign L-System description file
					loadLSystem(); // parse L-System description file and assign class variables
				}
			}	);
		fileMenu.add(loadLSystemItem);

		// --------------------------------------------------------
		// --- Configure image
		JMenuItem configImgItem = new JMenuItem("Configure image");
		configImgItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// when selected...
					promptUserForColors(); // get background & foreground colors as hex values
					g2D.fill(new Rectangle(0, 0, imgWidth, imgHeight));
					displayBufferedImage(image); // display filled background
					g2D.setColor(new Color(foreground_color));
				}
			}	);
		fileMenu.add(configImgItem);

		// --------------------------------------------------------------
		// --- Display L-System
		JMenuItem displayLSystemItem = new JMenuItem("Display L-System");
		displayLSystemItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// when selected...
					promptUserForNumGens();
					promptUserForBaseSegmentLength();

					String finalString = computeNthGenString(); // create string of commands
					interpretString(finalString); // interpret those commands

					displayBufferedImage(image); // display nth generation image
				}
			}	);
		fileMenu.add(displayLSystemItem);		

		// -------------------------------------------------
		// --- Save image
		JMenuItem saveImgItem = new JMenuItem("Save image");
		saveImgItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// actions to perform		
					saveImage();		
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

	/*
		methods to prompt user for input 
		and assign the corresponding class variables
	*/
	// ---------------------------------
	// user chooses IFS description file
	private void promptUserForFile() {
		File file = getFile();
		if (file != null) {
			this.description_file = file;
		}
	}
	// --------------------------------
	// open a file selected by the user
	private File getFile() {
		File file = null;

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}

		return file;
	}
	// ----------------------------------------------------------------------
	// user will input hex values as string that are then interpreted as ints
	// and assigned to background_color and foreground_color
	private void promptUserForColors() {
		String hexStr = JOptionPane.showInputDialog("Enter the desired hex RGB value for the image background color."); // get String input from option pane
		int val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		this.background_color = val;

		hexStr = JOptionPane.showInputDialog("Enter the desired hex RGB value for the image foreground color."); // get String input from option pane
		val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		this.foreground_color = val;
	}
	// ----------------------------------------------------------------
	// user will input string number that is then interpreted as an int
	// and assigned to num_gens
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
		this.num_gens = val;
	}
	// -----------------------------------------------------------
	// user will input value in rage [0.0, 1.0] that is later used
	// to determine the segment length in the nth generation;
	// an input of 1.0 corresponds to half of the image height
	private void promptUserForBaseSegmentLength() {
		String result = JOptionPane.showInputDialog("Enter the base segment length in range [0.0, 1.0] (where 1.0 means 1/2 image height)"); // get String input from option pane
		double val = 0.0; // initialize to zero before checking correct format

		try {
			val = Double.parseDouble(result); // convert val to double form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		// map [0.0, 1.0] to [0, height/2]
		val = val * (this.imgHeight/2.0);
		this.base_segment_length = val;
	}	

	/*================================================
					   loadLSystem
	  ================================================
	  - parse L-system description file and assign
	    delta, scale factor, initiator, and up to 10
	    production rules
	*/
	private void loadLSystem() {
		Scanner sc;

		// assuming the description file is in the correct format:
		// delta
		// segment length scaling factor
		// initiator string
		// up to 10 production rules (one per line)
		try {
			sc = new Scanner(this.description_file);
			this.delta = sc.nextInt();
			this.scale_factor = sc.nextInt();
			sc.nextLine();
			this.initiator = sc.nextLine();

			// DEBUG
			// System.out.println(this.delta);
			// System.out.println(this.scalingFactor);
			// System.out.println(this.initiator);

			// store production rules in HashMap
			int i = 0;
			String temp, replacement;
			char symbol;
			while (sc.hasNextLine()) {
				temp = sc.nextLine();
				symbol = temp.charAt(0);
				replacement = temp.substring(4);
				this.production_rules.put(symbol, replacement);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/*===============================================
				    computeNthGenString
	  ===============================================
	  - build nth generation string using production
	    rules, etc.
	*/
	private String computeNthGenString() {
		StringBuilder sb = new StringBuilder(this.initiator);

		String input = this.initiator;
		for (int i = 0; i < this.num_gens; i++) {
			StringBuilder output = new StringBuilder("");
			for (int j = 0; j < input.length(); j++) {
				if (this.production_rules.containsKey(input.charAt(j))) {
					output.append(this.production_rules.get(input.charAt(j)));
				}
				else {
					output.append(input.charAt(j));
				}
			}
			input = output.toString();
		}
		return input;
	}

	/*================================================
					   interpretString
	  ================================================
	  - execute commands described in final generation 
	    of L-system string by looking up each symbol 
	    in the dictionary
	*/
	private void interpretString(String commands) {
		// define variable arrays to keep track of turtle's previous position and orientation
		double[] prev_pos = new double[3];
		prev_pos = this.init_pos;
		double[][] prev_orientation = new double[3][3];
		prev_orientation = this.init_orientation;

		// define variable array to keep track of turtle's current position and orientation
		// after each movement, the previous will be updated with the current
		double[] curr_pos = new double[3];
		double[][] curr_orientation = new double[3][3];

		// define empty stacks to store positions and orientations
		Stack<double[]> positions = new Stack<double[]>();
		Stack<double[][]> orientations = new Stack<double[][]>();

		// these points will become the 2D projections of the 3D points 
		// that make up each drawn line segment
		Point2D.Double prev_point = new Point2D.Double();
		Point2D.Double curr_point = new Point2D.Double();
		Line2D.Double line = new Line2D.Double();

		// adjust line segment length so the entire image fits in the frame
		double length = this.base_segment_length/(Math.pow(this.scale_factor,this.num_gens));

		// iterate through command string and execute based on corresponding dictionary entry
		for (int i = 0; i < commands.length(); i++) {
			switch(commands.charAt(i)) {
				case 'F': // these all move forward one unit, drawing a line segment
				case 'A': // above-left  3D frame
				case 'B': // above-right 3D frame
				case 'C': // below-left  3D frame
				case 'D': // below-right 3D frame
					// to move forward one unit, we add the unit heading vector to the current position
					curr_pos[0] = prev_pos[0] + length*curr_orientation[0][0];
					curr_pos[1] = prev_pos[1] + length*curr_orientation[1][0];
					curr_pos[2] = prev_pos[2] + length*curr_orientation[2][0];
					// convert previous and current positions to 2D projections and draw line
					prev_point = pointProjection(prev_pos);
					curr_point = pointProjection(curr_pos);
					line.setLine(prev_point.x, prev_point.y, curr_point.x, curr_point.y);
					this.g2D.draw(line);

					prev_pos = curr_pos; // re-assign previous position
					break;

				case 'f': // these all move forward one unit, without drawing a line segment
				case 'a': // above-left  3D frame
				case 'b': // above-right 3D frame
				case 'c': // below-left  3D frame
				case 'd': // below-right 3D frame
					// to move forward one unit, we add the unit heading vector to the current position
					curr_pos[0] = prev_pos[0] + length*curr_orientation[0][0];
					curr_pos[1] = prev_pos[1] + length*curr_orientation[1][0];
					curr_pos[2] = prev_pos[2] + length*curr_orientation[2][0];

					prev_pos = curr_pos; // re-assign previous position
					break;				

				case '+': // yaw left (about U)
					curr_orientation = rotate(prev_orientation, 2, true);
					prev_orientation = curr_orientation;
					break;

				case '-': // yaw right (about U)
					curr_orientation = rotate(prev_orientation, 2, false);
					prev_orientation = curr_orientation;
					break;

				case '&': // pitch down (about L)
					curr_orientation = rotate(prev_orientation, 1, true);
					prev_orientation = curr_orientation;
					break;

				case '^': // pitch up (about L)
					curr_orientation = rotate(prev_orientation, 1, false);
					prev_orientation = curr_orientation;
					break;

				case '\\': // roll left (about H)
					curr_orientation = rotate(prev_orientation, 0, true);
					prev_orientation = curr_orientation;
					break;

				case '/': // roll right (about H)
					curr_orientation = rotate(prev_orientation, 0, false);
					prev_orientation = curr_orientation;
					break;

				case '[': // save state (position and orientation)
					positions.push(prev_pos);
					orientations.push(prev_orientation);
					break;

				case ']': // restore state (position and orientation)
					prev_pos = positions.pop();
					prev_orientation = orientations.pop();
					break;
			}
		}
	}

	/*==============================================
						  rotate
	  ==============================================
	  - apply rotation matrix to current orientation
	  - type of rotation (about H,L,U) is chosen
	    based on "axis" and "positive" params
	  - axis = 0: H
	    axis = 1: L
	    axis = 2: U
	  - positive = 1: positive rotation
	    positive = 0: negative rotation
	*/
	private double[][] rotate(double[][] orientation, int axis, Boolean positive) {
		// define 3x3 product of matrix multiplication to return
		double[][] new_orientation = new double[3][3];

		// apply rotation matrix based on axis
		switch (axis) {
			case 0: // H-axis
				if (positive)
					new_orientation = matrixMult(orientation, this.h_pos_rot_mat);
				else
					new_orientation = matrixMult(orientation, this.h_neg_rot_mat);	
				break;
			case 1: // L-axis
				if (positive)
					new_orientation = matrixMult(orientation, this.l_pos_rot_mat);
				else
					new_orientation = matrixMult(orientation, this.l_neg_rot_mat);	
				break;
			case 2: // U-axis
				if (positive)
					new_orientation = matrixMult(orientation, this.u_pos_rot_mat);
				else
					new_orientation = matrixMult(orientation, this.u_neg_rot_mat);
				break;
		}

		return new_orientation;
	}

	/*=============================================
					    matrixMult
	  =============================================
	  - input: two 3x3 matrices
	  - output: 3x3 product of left_mat * right_mat
	*/	
	private double[][] matrixMult(double[][] left_mat, double[][] right_mat) {
		double[][] product = new double[3][3];

		product[0][0] = left_mat[0][0]*right_mat[0][0] + left_mat[0][1]*right_mat[1][0] + left_mat[0][2]*right_mat[2][0];
		product[0][1] = left_mat[0][0]*right_mat[0][1] + left_mat[0][1]*right_mat[1][1] + left_mat[0][2]*right_mat[2][1];
		product[0][2] = left_mat[0][0]*right_mat[0][2] + left_mat[0][1]*right_mat[1][2] + left_mat[0][2]*right_mat[2][2];
		product[1][0] = left_mat[1][0]*right_mat[0][0] + left_mat[1][1]*right_mat[1][0] + left_mat[1][2]*right_mat[2][0];
		product[1][1] = left_mat[1][0]*right_mat[0][1] + left_mat[1][1]*right_mat[1][1] + left_mat[1][2]*right_mat[2][1];
		product[1][2] = left_mat[1][0]*right_mat[0][2] + left_mat[1][1]*right_mat[1][2] + left_mat[1][2]*right_mat[2][2];
		product[2][0] = left_mat[2][0]*right_mat[0][0] + left_mat[2][1]*right_mat[1][0] + left_mat[2][2]*right_mat[2][0];
		product[2][1] = left_mat[2][0]*right_mat[0][1] + left_mat[2][1]*right_mat[1][1] + left_mat[2][2]*right_mat[2][1];
		product[2][2] = left_mat[2][0]*right_mat[0][2] + left_mat[2][1]*right_mat[1][2] + left_mat[2][2]*right_mat[2][2];

		return product;
	}

	/*===============================================
					  pointProjection
	  ===============================================
	  - input: 3D point
	  - output: 2D projection of point on image plane
	*/		
	private Point2D.Double pointProjection(double[] point_3D) {
		double x_2d, y_2d; 

		x_2d = ((point_3D[0] - this.x_cam) * (this.focal_length/point_3D[2])) + this.x_cam;
		y_2d = ((point_3D[1] - this.y_cam) * (this.focal_length/point_3D[2])) + this.y_cam;

		return new Point2D.Double(x_2d, y_2d);
	}	

	/*===========================================
					  saveImage
	  ===========================================
	  - save current image in current directory
	    as "saved.png"
	  - EDIT: let user define filename
	*/
	private void saveImage() {
		try {
			File output_file = new File("saved.png");
			javax.imageio.ImageIO.write(this.image, "png", output_file);
		}
		catch (IOException e) {
		   JOptionPane.showMessageDialog(ImageFrame.this, "Error saving file", "oops!", JOptionPane.ERROR_MESSAGE);
		}		
	}

	/*=====================================================
					  displayBufferedImage
	  =====================================================	
	  - resets the JFrame's content pane with a new image
	*/
	public void displayBufferedImage(BufferedImage image) {
		// display resulting image
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}	
}