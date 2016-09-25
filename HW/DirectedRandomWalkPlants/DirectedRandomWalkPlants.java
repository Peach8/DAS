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
	// variables
	private int imgSize; // height == width
	private int numStems;
	private int stepsPerStem;
	private float transmProb; // transmission probablity ϵ [0.0,1.0]
	private float maxRotIncPerStep; // max change in rotation per step ϵ [0.0,1.0] radians
	private int growthIncPerStep; // change in growth segment length per step

	private Random rand; // only one Random obj is needed

	private BufferedImage img = null; // initialize null BufferedImage to change later
	private Graphics2D g2D;

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		this.rand = new Random(); // create new Random object for generating walk sequence

		// setup the frame's attributes
		this.setTitle("CAP 3027 2016 - HW05 - Brandon Peterson");
		this.setSize(width, height);

		addMenu(); // add a menu to the frame
	}

	private void addMenu() {
		// === File menu
		JMenu fileMenu = new JMenu("File");

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

					// create new image and Graphics2D 
					img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
					g2D = (Graphics2D) img.createGraphics();
					
					// set background white
					g2D.setColor(Color.WHITE);
					setBackground();

					g2D.setColor(Color.BLACK); // draw black stems
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


	// -----------------------------------------------------------
	// promptUserForInt() -
	// 
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
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}	
		return val;
	}


	// -----------------------------------------------------------
	// promptUserForFloat() -
	// 
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
			JOptionPane.showMessageDialog(this, "Input must be in range [0.0, 1.0].", "Input must be in range [0.0, 1.0].", JOptionPane.ERROR_MESSAGE);
		}
		return val;
	}

	// ------------------------------------------------------------------
	// setBackground() - fill entire image with rectangle of specified color
	// 	params: BufferedImage image = image to change background color of
	//          int color = desired background color
	private void setBackground() {
		this.g2D.fillRect(0,0, this.imgSize, this.imgSize);
	}

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

			// draw the initial segment
			stemSegment.setLine(x, y, x + rho*Math.cos(angle), y - rho*Math.sin(angle));
			g2D.draw(stemSegment);

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

				stemSegment.setLine(prevX, prevY, prevX + rho*Math.cos(angle), prevY - rho*Math.sin(angle));
				g2D.draw(stemSegment);

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
