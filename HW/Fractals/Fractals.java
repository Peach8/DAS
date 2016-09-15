// Fractals.java
// -- generates fractals using diffusion limited aggregation, given user defined:
//	  - image size = n for an nxn image
//    - number of seeds = stationary pixels on which particles can stick
//    - number of particles = randomly moving particles that can stick to seeds or stuck particles
//	  - max number of steps = steps particles can take before terminating program 
//	    - if all particles are stuck before this max num steps is reached, the program will terminate
//
// Author: Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Random;
import java.util.ArrayList;

public class Fractals {
	private static final int frameWIDTH  = 400; // ImageFrame width
	private static final int frameHEIGHT = 400; // ImageFrame height

	public static void main(String[] args) {
		JFrame frame = new ImageFrame(frameWIDTH, frameHEIGHT); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.setVisible(true); // make frame visible
	}
}

//##########################################################################

class ImageFrame extends JFrame {
	// define colors
	private final int WHITE = 0xFFFFFFFF; // image background color
	private final int RED   = 0xFFFF0000; // seed color
	private final int BLACK = 0xFF000000; // particle color

	// declare variables that will be determined by user input
	private int imgSize;
	private int numSeeds;
	private int numParticles;
	private int maxNumSteps;

	private Random rand; // declare Random object

	private BufferedImage image = null; // initialize null BufferedImage to change later

	//==============================================================
	// constructors
	public ImageFrame(int width, int height) {
		rand = new Random(); // create new Random object

		// ---------------------------------------------------------
		// setup the frame's attributes
		this.setTitle("CAP 3027 2016 - HW03 - Brandon Peterson");
		this.setSize(width, height); 

		// add a menu to the frame
		addMenu();
	}

	private void addMenu() {
		// === File menu
		JMenu fileMenu = new JMenu("File");

		// --- Crystal (toroid)
		JMenuItem crystalToroidItem = new JMenuItem("Crystal (toroid)");
		crystalToroidItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// prompt user for input to assign imgSize, numSeeds, numParticles, maxNumSteps
					promptUser();

					image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB); // construct image for bilinear gradient
					fractalSim(image, imgSize, "toroid", numSeeds, numParticles, maxNumSteps); // generate crystals/fractals
					displayBufferedImage(image); // display final image		
				}
			}	);
		fileMenu.add(crystalToroidItem);	

		// --- Crystal (bounded plane)
		JMenuItem crystalBoundedItem = new JMenuItem("Crystal (bounded plane)");
		crystalBoundedItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					// prompt user for input to assign imgSize, numSeeds, numParticles, maxNumSteps
					promptUser();
					
					image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB); // construct image for bilinear gradient
					fractalSim(image, imgSize, "bounded", numSeeds, numParticles, maxNumSteps); // generate crystals/fractals
					displayBufferedImage(image); // display final image		
				}
			}	);
		fileMenu.add(crystalBoundedItem);		

		// --- Exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					System.exit(0);
				}
			}	);
		fileMenu.add(exitItem);	

		// attach menu to a menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}

	// ----------------------------------------------------------
	// promptUser(): assign private variables based on user input
	private void promptUser() {
		this.imgSize      = promptForPosNum("Enter your desired size n (for an nxn size image).");
		this.numSeeds     = promptForPosNum("Enter the number of seeds.");
		this.numParticles = promptForPosNum("Enter the number of particles.");
		this.maxNumSteps  = promptForPosNum("Enter the maximum number of steps.");
	}

	// ---------------------------------------------------------
	// promptForPosNum(): prompt the user for a positive int based on a message dialog,
	//					  and returns that int
	// params:
	// String msg = message dialog describing what the user's input will define
	private int promptForPosNum(String msg) {
		String result = JOptionPane.showInputDialog(msg); // get String input from option pane
		int size = 0; // initialize to zero before checking correct format

		try {
			size = Integer.parseInt(result); // convert size to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (size < 0) {
			// don't allow negative
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		return size;
	}

	// ------------------------------------------------------------------------------
	// fractalSim(): simulates fractal generation using diffusion limited aggregation
	//
	// params: 
	// - BufferedImage image = image on which to draw fractals
	// - int imgSize = size of BufferedImage
	// - String topology = defines movement constraints of particles (bounded plane or toroidal plane)
	// - int numSeeds = number of stationary seeds on which particles stick and form fractals
	// - int numParticles = number of particles moving around image that may stick to seeds and form fractals
	// - maxNumSteps = maximum number of steps particles can take 
	private void fractalSim(BufferedImage image, int imgSize, String topology, int numSeeds, int numParticles, int maxNumSteps) {
		// fill background with white
		for (int i = 0; i < imgSize; i++) {
			for (int j = 0; j < imgSize; j++) {
				image.setRGB(i, j, WHITE);
			}
		}

		// randomly position seeds and make them red
		for (int i = 0; i < numSeeds; i++) {
			image.setRGB(rand.nextInt(imgSize),rand.nextInt(imgSize), RED);
		}

		// create ArrayList to store Particle objects
		ArrayList<Particle> particles = new ArrayList<Particle>(numParticles);
		// create randomly positioned Particle objects
		for (int i = 0; i < numParticles; i++) {
			Particle p = new Particle(this.rand, imgSize);
			particles.add(p);
		}

		// generate fractals
		int[] particlePos; // position of current particle being checked
		// continuing moving particles until all particles are stuck or
		// each has taken the maximum number of steps (whichever comes first)
		for (int i = 0; i < maxNumSteps; i++) {
			// create new ArrayList before each step to store newly stuck partciles
			ArrayList<Particle> stuck = new ArrayList<Particle>();
			// iterate through remaining un-stuck particles
			for (Particle particle : particles) {				
				particlePos = particle.getPos();
				// if particle is next to seed or already stuck particle,
				// add to stuck ArrayList and make it black
				if (isNextToSeed(particlePos) == true) {
					stuck.add(particle);
					image.setRGB(particlePos[0],particlePos[1], BLACK);
				}
			}
			// remove newly stuck particles from particle ArrayList
			particles.removeAll(stuck);
			// if all particles are stuck, no more steps need to be taken
			if (particles.size() == 0) {
				break;
			}
			// move remaining un-stuck particles based on topology
			for (Particle particle : particles) {
				particle.step(topology);
			}
		}
	}

	// ---------------------------------------------------------------------------------
	// isNextToSeed(): returns boolean whether a pixel is adjacent to a non-white pixel, 
	//                 i.e., a seed (red) or stuck particle (black)
	// params:
	// - int[] pos = position of pixel (pos[0] = x, pos[1] = y)
	private boolean isNextToSeed(int[] pos) {
		boolean adjacent = false;

		// check color of adjacent pixels in Moore neighborhood
		if ((inBounds(pos[0], pos[1]+1) == true) && (image.getRGB(pos[0], pos[1]+1) != WHITE)) { // check pixel below
			adjacent = true;
		}
		else if ((inBounds(pos[0]-1, pos[1]+1) == true) && (image.getRGB(pos[0]-1, pos[1]+1) != WHITE)) { // check pixel below-left
			adjacent = true;
		}
		else if ((inBounds(pos[0]-1, pos[1]) == true) && (image.getRGB(pos[0]-1, pos[1]) != WHITE)) { // check pixel left
			adjacent = true;
		}		
		else if ((inBounds(pos[0]-1, pos[1]-1) == true) && (image.getRGB(pos[0]-1, pos[1]-1) != WHITE)) { // check pixel above-left
			adjacent = true;
		}		
		else if ((inBounds(pos[0], pos[1]-1) == true) && (image.getRGB(pos[0], pos[1]-1) != WHITE)) { // check pixel above
			adjacent = true;
		}		
		else if ((inBounds(pos[0]+1, pos[1]-1) == true) && (image.getRGB(pos[0]+1, pos[1]-1) != WHITE)) { // check pixel above-right
			adjacent = true;
		}		
		else if ((inBounds(pos[0]+1, pos[1]) == true) && (image.getRGB(pos[0]+1, pos[1]) != WHITE)) { // check pixel right
			adjacent = true;
		}
		else if ((inBounds(pos[0]+1, pos[1]+1) == true) && (image.getRGB(pos[0]+1, pos[1]+1) != WHITE)) { // check pixel below-right
			adjacent = true;
		}		

		return adjacent;
	}

	// ----------------------------------------------------------------------------------
	// inBounds(): returns boolean whether a coordinate is within the bounds of the image
	//
	// params:
	// - int x: x coordinate
	// - int y: y coordinate
	private boolean inBounds(int x, int y) {
		boolean result = false;

		if ((x >= 0) && (x < this.imgSize) &&
			(y >= 0) && (y < this.imgSize)) {
			result = true;
		}

		return result;
	}

	// ---------------------------------------------------------
	// displayBufferedImage(): displays BufferedImage as an ImageIcon
	//						   in a JLabel, in a JScrollPane
	// params:
	// - BufferedImage image = image to display
	private void displayBufferedImage(BufferedImage image) {
		// display resulting image 
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}