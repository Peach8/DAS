// DirectedRandomWalkPlants.java - based on an algorithm in Clifford Pickover's "Computers, Pattern, Chaos, and Beauty"
//
// by Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.Line2D;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Random;

public class DirectedRandomWalkPlants {
	private static final int frameWIDTH  = 650; // ImageFrame width
	private static final int frameHEIGHT = 650; // ImageFrame height

	// make sure EDT handles display of GUI
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		}	);
	}

	public static void createAndShowGUI() {
		JFrame frame = new ImageFrame(frameWIDTH, frameHEIGHT); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.setVisible(true); // make frame visible
	}
}

//###########################################################################

class ImageFrame extends JFrame {
	// define color/stroke constants & variables
	private int stemColor; // user-defined input
	private int[] stem_chnls; // will hold RGB values
	private int tipColor; //  user-define input
	private int[] tip_chnls; // will hold RGB values
	private int[] colors; // holds interplated colors btwn stem and tip
	private BasicStroke[] strokes; // holds BasicStroke objs w/ interpolated stroke widths btwn stem and tip

	// variables
	private int imgSize; // height == width
	private int numStems;
	private int stepsPerStem;
	private float transmProb; // transmission probablity ϵ [0.0,1.0]
	private float maxRotIncPerStep; // max change in rotation per step ϵ [0.0,1.0] radians
	private int growthIncPerStep; // change in growth segment length per step

	private Random rand; // only one Random obj is needed
	private RenderingHints hint; // hint for anti-aliasing

	private BufferedImage img = null; // initialize null BufferedImage to change later
	private Graphics2D g2D; // only one Graphics2D obj is needed

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		this.rand = new Random(); // create new Random object for generating walk sequence

		this.hint = new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
			                           RenderingHints.VALUE_ANTIALIAS_ON);

		// setup the frame's attributes
		this.setTitle("CAP 3027 2016 - HW05a - Brandon Peterson");
		this.setSize(width, height);

		addMenu(); // add a menu to the frame
	}

	private void addMenu() {
		// === File menu
		JMenu fileMenu = new JMenu("File");

		// --- Set colors
		JMenuItem setColorsItem = new JMenuItem("Set colors");
		setColorsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// prompt user for stem and tip colors
					stemColor = promptUserForColor("Enter the desired hex RGB value for the plant stem color.");
					tipColor = promptUserForColor("Enter the desired hex RGB value for the plant tip color.");
					// extract RGB values of each
					stem_chnls = new int[] {((stemColor >>> 16) & 0xFF), ((stemColor >>> 8) & 0xFF), (stemColor & 0xFF)};					
					tip_chnls = new int[] {((tipColor >>> 16) & 0xFF), ((tipColor >>> 8) & 0xFF), (tipColor & 0xFF)};					
				}
			}	);
		fileMenu.add(setColorsItem);

		// --- Load source image
		JMenuItem drwpItem = new JMenuItem("Directed random walk plant");
		drwpItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// prompt user for input to assign imgSize, numSeeds, numParticles, maxNumSteps
					imgSize = promptUserForInt("Enter the desired image size n (for an nxn image).");
					numStems = promptUserForInt("Enter the desired number of stems.");
					stepsPerStem = promptUserForInt("Enter the desired number of steps per stem.");
					transmProb = promptUserForFloat("Enter the desired transmission probability.");
					maxRotIncPerStep = promptUserForFloat("Enter the desired maximum rotation increment.");
					growthIncPerStep = promptUserForInt("Enter the desired growth segment increment.");
 
					// fill color/stroke array with interpolated colors/strokes
					interpolateColorAndStroke();

					// create new image and Graphics2D 
					img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
					g2D = (Graphics2D) img.createGraphics();
					g2D.setRenderingHints(hint);
					
					// set background black
					clearBackground();

					DRWP(); // directed random walk plants algorithm 
					displayBufferedImage(img);
				}
			}	);
		fileMenu.add(drwpItem);

		// --- Exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
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

	// -------------------------------------------------------------------------------
	// promptUserForColor() - user will input hex value as string that is then 
	// 						  interpreted as int
	// params: String msg = prompt message for user
	private int promptUserForColor(String msg) {
		String hexStr = JOptionPane.showInputDialog(msg); // get String input from option pane
		int val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		return val;
	}

	// -------------------------------------------------------------------------------
	// promptUserForInt() - throw exception/error message if input is not positive int
	// 
	// params: String msg = prompt message for user
	private int promptUserForInt(String msg) {
		String result = JOptionPane.showInputDialog(msg); // get String input from option pane
		int val = 0; // init input to 0

		try {
			val = Integer.parseInt(result); // convert val to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (val < 0) {
			// don't allow negative input
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", 
				                                "Input must be non-negative.", 
				                                    JOptionPane.ERROR_MESSAGE);
		}	
		return val;
	}

	// -------------------------------------------------------------------------------
	// promptUserForFloat() - throw exception/error if input is not float in [0.0,1.0]
	// 
	// params: String msg = prompt message for user
	private float promptUserForFloat(String msg) {
		String result = JOptionPane.showInputDialog(msg); // get String input from option pane
		float val = 0; // init input to 0

		try {
			val = Float.parseFloat(result); // convert val to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if ((val < 0) || (val > 1.0)) {
			// don't allow negative input
			JOptionPane.showMessageDialog(this, "Input must be in range [0.0, 1.0].", 
				                                "Input must be in range [0.0, 1.0].", 
				                                           JOptionPane.ERROR_MESSAGE);
		}
		return val;
	}

	// ----------------------------------------------------------------------------------------------
	// interpolateColorAndStroke() - fill colors array with interpolated intRGBs between user-defined
	//								 tip and stem color; fill strokes array with BasicStroke objects
	//								 of interpolated widths from 6.0f to 0.5f
	private void interpolateColorAndStroke() {
		int i,j; // loop counters
		// initialize size of color array based on user input
		colors = new int[this.stepsPerStem];
		// define arrays to hold color channel values (RGB)
		double[] color_chnls = new double[3];
		// declare array to store step values for each color channel between tip and stem color
		double[] color_deltas = new double[3];

		// compute step values for interpolation between stem and tip colors
		for (i = 0; i < 3; i++) {
			color_deltas[i]  = (double) (this.tip_chnls[i] - this.stem_chnls[i])/(this.stepsPerStem-1);
		}
		// define start color channels to stem color channels
		for (i = 0; i < 3; i++) {
			color_chnls[i] = this.stem_chnls[i];
		}
		// define first color as stem color
		this.colors[0] = synthColor(color_chnls[0], color_chnls[1], color_chnls[2]);
		// define remaining channel values stepping towards tip color
		// and fill color array with colors synthesized from color channels
		for (i = 1; i < this.stepsPerStem; i++) {
			for (j = 0; j < 3; j++) {
				color_chnls[j] += color_deltas[j];
				color_chnls[j] = clamp(color_chnls[j]);
			}
			this.colors[i] = synthColor(color_chnls[0], color_chnls[1], color_chnls[2]);
		}

		// initialize size of stroke array
		this.strokes = new BasicStroke[stepsPerStem];
		float currStrokeWidth = 6.0f; // define current width with stem width
		this.strokes[0] = new BasicStroke(currStrokeWidth);
		// compute step value for interpolation between stem and tip widths
		float delta_strokeWidth = (float) ((6.0f - 0.5f) / (this.stepsPerStem-1));
		for (i = 1; i < this.stepsPerStem; i++) {
			currStrokeWidth -= delta_strokeWidth;
			this.strokes[i] = new BasicStroke(currStrokeWidth);
		}
	}

	// ----------------------------------------------------------------
	// clamp() - clamp channel value to smallest/largest possible value
	//			 if out of possible range [0,255]
	private double clamp(double val) {
		if (val < 0.0) {
			val = 0.0;
		}
		else if (val > 255.0) {
			val = 255.0;
		}
		return val;
	}

	// synthColor() - synthesize int color given indivual RGB int channels;
	//                define full alpha channel
	private int synthColor(double red, double green, double blue) {
		int result = (0xFF000000 | ((int) red << 16) | ((int) green << 8 ) | (int) blue);	
		return result;
	}	

	// ---------------------------------------------------------------------
	// clearBackground() - fill entire image with black rectangle
	private void clearBackground() {
		g2D.setColor(Color.BLACK);
		this.g2D.fillRect(0,0, this.imgSize, this.imgSize);
	}

	// // template
	// private void doLongJob() {
	// 	new Thread(new Runnable() { 
	// 		public void run() { // defines what worker thread does
	// 			final BufferedImage image = createFrame();

	// 			// ask EDT to display image:
	// 			// create task (Runnable) and add that task to
	// 			// EDT's event queue
	// 			SwingUtilities.invokeLater(new Runnable() { 
	// 				public void run() { // defines what EDT should do
	// 					displayBufferedImage(image);
	// 				}
	// 			}	);
	// 		}
	// 	}).start();
	// }

	// -----------------------------------------------
	// DRWP() - Directed Randown Walk Plants algorithm
	private void DRWP() {
		int i,j,k; // loop counters
		// create arrays to store polar coordinate components of stem segments
		double angles[][] = new double[this.stepsPerStem+1][this.numStems];
		double radii[][] = new double[this.stepsPerStem+1][this.numStems];
		for (i = 0; i < this.numStems; i++) {
			angles[0][i] = Math.PI/2.0; // initial angles (stems start growing "upwards")
			radii[0][i] = 1.0; // initial segment length
		}

		// create arrays to store start/end coordinates for each stem's segments
		double xCoords[][] = new double[this.stepsPerStem+1][this.numStems];
		double yCoords[][] = new double[this.stepsPerStem+1][this.numStems];
		for (i = 0; i < this.numStems; i++) {
			// add starting coordinates for each stem
			xCoords[0][i] = this.imgSize/2.0;
			yCoords[0][i] = this.imgSize/2.0;
		}

		// create arrays to store previous coordinate defined in each stem
		double prevX[] = new double[this.numStems];
		double prevY[] = new double[this.numStems];
		for (i = 0; i < this.numStems; i++) {
			prevX[i] = xCoords[0][i];
			prevY[i] = yCoords[0][i];
		}

		// create line obj used to draw stem segments
		Line2D.Double stemSegment = new Line2D.Double();

		double reflProb = 1 - this.transmProb; // reflection probability

		// create array to store growth directions of each stem
		int direction[] = new int[this.numStems];
		// initialize all stems growing left
		for (i = 0; i < this.numStems; i++) {
			direction[i] = 1; // +1 = left; -1 = right
		}

		// draw initial segments to start growth "upwards" but don't display in animation
		g2D.setColor(new Color(this.colors[0]));
		g2D.setStroke(this.strokes[0]);
		for (i = 0; i < this.numStems; i++) {
			stemSegment.setLine(xCoords[0][i], yCoords[0][i], xCoords[0][i] + radii[0][i]*Math.cos(angles[0][i]), yCoords[0][i] - radii[0][i]*Math.sin(angles[0][i]));
			g2D.draw(stemSegment);
		}

		// create array to store a different bias for each stem (used in random walk algorithm)
		double bias[] = new double[this.numStems];

		for (i = 0; i < this.stepsPerStem; i++) {
			// define "new" previous position
			for (j = 0; j < this.numStems; j++)  {
				prevX[j] = prevX[j] + radii[i][j]*Math.cos(angles[i][j]);
				prevY[j] = prevY[j] - radii[i][j]*Math.sin(angles[i][j]);
			}
		
			// add each new coordinate to each stem's coordinate arrays
			for (j = 0; j < this.numStems; j++) {
				xCoords[i+1][j] = prevX[j];
				yCoords[i+1][j] = prevY[j];
			}

			// draw each stem's new growth (starting at beginning)
			for (j = 0; j < i+1; j++) {
				for (k = 0; k < this.numStems; k++) {
					// define new line color and stroke from arrays of interpolated values
					g2D.setColor(new Color(this.colors[j]));
					g2D.setStroke(this.strokes[j]);				
					// draw new segment from end of previous segment to newly calculated end position
					stemSegment.setLine(xCoords[j+1][k], yCoords[j+1][k], xCoords[j+1][k] + radii[j+1][k]*Math.cos(angles[j+1][k]), yCoords[j+1][k] - radii[j+1][k]*Math.sin(angles[j+1][k]));
					g2D.draw(stemSegment);
				}
			}

			/* COMPUTE END COORDINATES OF NEXT SEGMENTS (for each stem) */
			for (j = 0; j < this.numStems; j++) {
				// determine new bias
				if (direction[j] == -1) {
					bias[j] = this.transmProb;
				}
				else {
					bias[j] = reflProb;
				}
				// use bias to determine next direction
				if (rand.nextFloat() > bias[j]) {
					direction[j] = 1; // left
				}
				else {
					direction[j] = -1; // right
				}
				// calculate end position of next line to draw
				radii[i+1][j] = radii[i][j] + this.growthIncPerStep;
				angles[i+1][j] = angles[i][j] + (this.maxRotIncPerStep * rand.nextFloat() * direction[j]);
			}
			/* --------------------------------------- */
		}
	}

	// ---------------------------------------------------------------
	// displayBufferedImage() - displays an image in the content pane
	// 	params: BufferedImage image = image to display
	public void displayBufferedImage(BufferedImage image) {
		// display resulting image
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}