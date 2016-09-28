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

	public static void main(String[] args) {
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
			                           RenderingHints.VALUE_ANTIALIAS_OFF);

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
		setColorsItem.addActionListener(new ActionListener()
			{
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
		drwpItem.addActionListener(new ActionListener()
			{
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
					g2D.setRenderingHints(hint); // 
					
					// set background black
					g2D.setColor(Color.BLACK);
					setBackground();

					DRWP(); // directed random walk plants algorithm 

					displayBufferedImage(img); // display final image
				}
			}	);
		fileMenu.add(drwpItem);

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

	// -------------------------------------------------------------------------------
	// promptUserForColor() - user will input hex value as string that is then 
	// 						  interpreted as int
	// params: String msg = prompt message for user
	private int promptUserForColor(String msg) {
		String hexStr = JOptionPane.showInputDialog(msg); // get String input from option pane
		int val = 0; // init input to 0

		try {
			val = (int) Long.parseLong(hexStr.substring(2, hexStr.length()), 16); // convert input string to int form
			System.out.println(val);
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
		return (0xFF000000 | ((int) red << 16) | ((int) green << 8 ) | (int) blue);		
	}	

	// ---------------------------------------------------------------------
	// setBackground() - fill entire image with rectangle of specified color
	// 	Note: desired background color should be set prior to calling this method
	private void setBackground() {
		this.g2D.fillRect(0,0, this.imgSize, this.imgSize);
	}

	// -----------------------------------------------
	// DRWP() - Directed Randown Walk Plants algorithm
	private void DRWP() {
		double angle; // angle of stem growth in radians
		double rho; // growth segment length
		
		// grow from point (x,y)
		double x = this.imgSize/2.0; 
		double y = x;

		// create line obj used to draw stem segments
		Line2D.Double stemSegment = new Line2D.Double();

		double reflProb = 1 - this.transmProb; // reflection probability
		int direction = 1; // +1 = left; -1 = right

		double bias, prevX, prevY;
		int i;
		while (this.numStems > 0) {
			angle = Math.PI/2.0; // initial angle (stem starts growing "upwards")
			rho = 1.0; // initial growth segment length

			// define new line color and stroke from arrays of interpolated values
			g2D.setColor(new Color(this.colors[0]));
			g2D.setStroke(this.strokes[0]);
			// draw the initial segment
			stemSegment.setLine(x, y, x + rho*Math.cos(angle), y - rho*Math.sin(angle));
			g2D.draw(stemSegment);

			// define "new" previous position
			prevX = x + rho*Math.cos(angle);
			prevY = y - rho*Math.sin(angle);

			for (i = 0; i < this.stepsPerStem; i++) {
				// determine new bias
				if (direction == -1) {
					bias = this.transmProb;
				}
				else {
					bias = reflProb;
				}
				// use bias to determine next direction
				if (rand.nextFloat() > bias) {
					direction = 1; // left
				}
				else {
					direction = -1; // right
				}
				// calculate end position of next line to draw
				rho = rho + this.growthIncPerStep;
				angle = angle + (this.maxRotIncPerStep * rand.nextFloat() * direction);

				// define new line color and stroke from arrays of interpolated values
				g2D.setColor(new Color(this.colors[i]));
				g2D.setStroke(this.strokes[i]);				
				// draw new segment from end of previous segment to newly calculated end position
				stemSegment.setLine(prevX, prevY, prevX + rho*Math.cos(angle), prevY - rho*Math.sin(angle));
				g2D.draw(stemSegment);

				// define "new" previous position
				prevX = prevX + rho*Math.cos(angle);
				prevY = prevY - rho*Math.sin(angle);
			}
			this.numStems--;
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
