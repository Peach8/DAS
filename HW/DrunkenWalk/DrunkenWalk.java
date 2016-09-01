// DrunkenWalk.java
// - Allows user to simulate a drunken walk (Brownian motion) 
//   based on two types of neighborhoods (von Neumann & Moore)
//	 and three types of topologies (infinite plane, bounded plane, 
//   & toroidal plane)
// by Brandon Peterson (8/30/16)

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Random;

public class DrunkenWalk {
	private static final int frameWIDTH  = 400; // ImageFrame width
	private static final int frameHEIGHT = 400; // ImageFrame height

	public static void main(String[] args) {
		JFrame frame = new ImageFrame(frameWIDTH, frameHEIGHT); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.setVisible(true); // make frame visible
	}
}

//###########################################################################

class ImageFrame extends JFrame {
	private static final int CREAM = 0xFFFFFFEE; // image background color
	private static final int BLACK = 0xFF000000; // step color
	private static final int RED   = 0xFFFF0000; // final step color

	private static final int imgWIDTH  = 401; // BufferedImage width
	private static final int imgHEIGHT = 401; // BufferedImage height
	private BufferedImage image = null; // initialize null BufferedImage to change later

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		// ---------------------------------------------------------
		// setup the frame's attributes

		this.setTitle("CAP 3027 2016 - HW01 - Brandon Peterson");
		this.setSize(width, height);

		// add a menu to the frame
		addMenu();
	}

	private void addMenu() {
		// ---------------------------------------------------------
		// setup the frame's attributes

		// === File menu

		JMenu fileMenu = new JMenu("File");

		// --- Exit

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					System.exit(0);
				}
			}	);

		fileMenu.add(exitItem);	
		
		// === von Neumann menu

		JMenu vonNeumannMenu = new JMenu("von Neumann");	

		// --- Drunken walk on an infinite plane

		JMenuItem infinitePlaneItemVN = new JMenuItem("Drunken walk on an infinite plane");
		infinitePlaneItemVN.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 0, 0); // simulate drunken walk on infinite plane using von Neumann neighborhood
					displayBufferedImage(image); // display final image		
				}
			}	);

		vonNeumannMenu.add(infinitePlaneItemVN);

		// --- Drunken walk on a bounded plane

		JMenuItem boundedPlaneItemVN = new JMenuItem("Drunken walk on a bounded plane");
		boundedPlaneItemVN.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 0, 1); // simulate drunken walk on bounded plane using von Neumann neighborhood
					displayBufferedImage(image); // display final image		
				}
			}	);

		vonNeumannMenu.add(boundedPlaneItemVN);

		// --- Drunken walk on a toroidal plane

		JMenuItem toroidalPlaneItemVN = new JMenuItem("Drunken walk on a toroidal plane");
		toroidalPlaneItemVN.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 0, 2); // simulate drunken walk on toroidal plane using von Neumann neighborhood
					displayBufferedImage(image); // display final image						
				}
			}	);

		vonNeumannMenu.add(toroidalPlaneItemVN);			

		// === Moore menu

		JMenu mooreMenu = new JMenu("Moore");					

		// --- Drunken walk on an infinite plane

		JMenuItem infinitePlaneItemM = new JMenuItem("Drunken walk on an infinite plane");
		infinitePlaneItemM.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 1, 0); // simulate drunken walk on infinite plane using Moore neighborhood
					displayBufferedImage(image); // display final image		
				}
			}	);

		mooreMenu.add(infinitePlaneItemM);

		// --- Drunken walk on a bounded plane

		JMenuItem boundedPlaneItemM = new JMenuItem("Drunken walk on a bounded plane");
		boundedPlaneItemM.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 1, 1); // simulate drunken walk on bounded plane using Moore neighborhood
					displayBufferedImage(image); // display final image		
				}
			}	);

		mooreMenu.add(boundedPlaneItemM);

		// --- Drunken walk on a toroidal plane

		JMenuItem toroidalPlaneItemM = new JMenuItem("Drunken walk on a toroidal plane");
		toroidalPlaneItemM.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					int numSteps = promptForSteps(); // get number of steps from user
					image = new BufferedImage(imgWIDTH, imgHEIGHT, BufferedImage.TYPE_INT_ARGB); // construct image for drunken walk simulation
					drunkenWalk(image, numSteps, 1, 2); // simulate drunken walk on toroidal plane using Moore neighborhood
					displayBufferedImage(image); // display final image						
				}
			}	);

		mooreMenu.add(toroidalPlaneItemM);	

		// === attach menus to a menu bar

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(vonNeumannMenu);
		menuBar.add(mooreMenu);
		this.setJMenuBar(menuBar);
	}

	// ---------------------------------------------------------
	// promptForSteps() - prompt the user for number of steps the drunk takes

	private int promptForSteps() {
		String result = JOptionPane.showInputDialog("Enter the number of steps the drunk takes"); // get String input from option pane
		int numSteps = 0; // initialize to zero before checking correct format

		try {
			numSteps = Integer.parseInt(result); // convert number of steps to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception (only the cream background will be displayed)
		}
		if (numSteps < 0) {
			// don't allow negative input
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		return numSteps;
	}

	// ---------------------------------------------------------
	// drunkenWalk() - simulation of drunken walk using a
	//				   - von Neumann neighborhood or
	//				   - Moore neighborhood
	//					 on
	// 				     - an infinite plane,
	//				     - a bounded plane, or
	//  			     - a toroidal plane, 

	// params:
	// - BufferedImage image: image on which to display drunken walk sequence
	// - int numSteps: number of steps in drunken walk
	// - int neighborhood: 0 = von Neumann, 1 = Moore
	// - int topology: 0 = infinite plane, 1 = bounded plane, 2 = toroidal plane


	private void drunkenWalk(BufferedImage image, int numSteps, int neighborhood, int topology) {
		// fill background with cream color
		for (int i = 0; i < imgWIDTH; i++) {
			for (int j = 0; j < imgHEIGHT; j++) {
				image.setRGB(i, j, CREAM);
			}
		}

		int startPosx = imgWIDTH/2;  // define starting x pos
		int startPosy = imgHEIGHT/2; // define starting y pos
		int currPosx; // var to store current x pos during walk
		int currPosy; // var to store current y pos during walk

		int stepColor = BLACK; // every step will be black, except the last step will be red

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK); // mark start pos in black
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					if (i == numSteps) {
						stepColor = RED;
					}
					int step = 0; // initialize step to 0 before generating rand val
					switch (neighborhood) {
						case 0: // von Neumann
								step = rand.nextInt(4); // step ϵ [0,4)
								break;
						case 1: // Moore
								step = rand.nextInt(8); // step ϵ [0,8)
								break;						
					}
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								break;
						case 2: currPosy--; // move up 1 pixel
								break;
						case 3: currPosx++; // move right 1 pixel
								break;
						case 4: currPosx--; // move left 1 pixel
								currPosy++; // & down 1 pixel
								break;
						case 5: currPosx--; // move left 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 6: currPosx++; // move right 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 7: currPosx++; // move right 1 pixel
								currPosy++; // & down 1 pixel
								break;								
					}
					switch (topology) {
						case 0: // infinite plane - 
								// let the drunk walk outside/through the boundaries,
								// but only mark steps made inside the 401 x 401 window
								if ((currPosx >= 0) && (currPosx < imgWIDTH)) {
									if ((currPosy >= 0) && (currPosy < imgHEIGHT)) {
										image.setRGB(currPosx, currPosy, stepColor);							
									}	
								}							
								break;
						case 1: // bounded plane -
								// don't let the drunk walk outside/through the boundaries
								// instead, make him "bounce off the walls" 
								if (currPosx == imgWIDTH) {
									currPosx = imgWIDTH-1;
								}
								else if (currPosx < 0) {
									currPosx = 0;
								}
								if (currPosy == imgHEIGHT) {
									currPosy = imgHEIGHT-1;
								}
								else if (currPosy < 0) {
									currPosy = 0;
								}
								image.setRGB(currPosx, currPosy, stepColor);							
								break;
						case 2: // toroidal plane -
								// connect top/bottom boundaries and left/right 
								// boundaries to let drunk walk freely between; 
								// note: opposite corners are connected as a result
								if (currPosx == imgWIDTH) {
									currPosx = 0;
								}
								else if (currPosx < 0) {
									currPosx = imgWIDTH-1;
								}
								if (currPosy == imgHEIGHT) {
									currPosy = 0;
								}
								else if (currPosy < 0) {
									currPosy = imgHEIGHT-1;
								}
								image.setRGB(currPosx, currPosy, stepColor);							
								break;																
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// Display BufferedImage

	public void displayBufferedImage(BufferedImage image) {
		// display resulting image 
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}