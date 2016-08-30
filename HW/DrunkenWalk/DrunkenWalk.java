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
					infinitePlaneWalkVN(image, numSteps); // simulate drunken walk on infinite plane using von Neumann neighborhood
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
					boundedPlaneWalkVN(image, numSteps); // simulate drunken walk on bounded plane using von Neumann neighborhood
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
					toroidalPlaneWalkVN(image, numSteps); // simulate drunken walk on toroidal plane using von Neumann neighborhood
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
					infinitePlaneWalkM(image, numSteps); // simulate drunken walk on infinite plane using Moore neighborhood
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
					boundedPlaneWalkM(image, numSteps); // simulate drunken walk on bounded plane using Moore neighborhood
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
					toroidalPlaneWalkM(image, numSteps); // simulate drunken walk on toroidal plane using Moore neighborhood
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
	// infinitePlaneWalkVN() - simulation of drunken walk on an infinite plane using von Neumann neighborhood

	private void infinitePlaneWalkVN(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK); // mark start pos in black
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(4); // step ϵ [0,4)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								break;
						case 2: currPosy--; // move up 1 pixel
								break;
						case 3: currPosx++; // move right 1 pixel
								break;
					}

					// let the drunk walk outside/through the boundaries,
					// but only mark steps made inside the 401 x 401 window
					if ((currPosx >= 0) && (currPosx < imgWIDTH)) {
						if ((currPosy >= 0) && (currPosy < imgHEIGHT)) {
							if (i == numSteps) {
								// mark the pos where the drunk falls down (last step) with red
								image.setRGB(currPosx, currPosy, RED);
							}
							else {
								// mark all other steps with black
								image.setRGB(currPosx, currPosy, BLACK);
							}							
						}	
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// boundedPlaneWalkVN() - simulation of drunken walk on a bounded plane using von Neumann neighborhood

	private void boundedPlaneWalkVN(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK);
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(4); // step ϵ [0,4)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								break;
						case 2: currPosy--; // move up 1 pixel
								break;
						case 3: currPosx++; // move right 1 pixel
								break;
					}

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

					if (i == numSteps) {
						// mark the pos where the drunk falls down (last step) with red
						image.setRGB(currPosx, currPosy, RED);
					}
					else {
						// mark all other steps with black
						image.setRGB(currPosx, currPosy, BLACK);
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// toroidalPlaneWalkVN() - simulation of drunken walk on an toroidal plane using von Neumann neighborhood

	private void toroidalPlaneWalkVN(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK);
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(4); // step ϵ [0,4)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								break;
						case 2: currPosy--; // move up 1 pixel
								break;
						case 3: currPosx++; // move right 1 pixel
								break;
					}

					// connect top/bottom boundaries 
					// and left/right boundaries to let drunk
					// walk freely between
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

					if (i == numSteps) {
						// mark the pos where the drunk falls down (last step) with red
						image.setRGB(currPosx, currPosy, RED);
					}
					else {
						// mark all other steps with black
						image.setRGB(currPosx, currPosy, BLACK);
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// infinitePlaneWalkM() - simulation of drunken walk on an infinite plane using Moore neighborhood

	private void infinitePlaneWalkM(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK);
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(8); // step ϵ [0,8)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								currPosy++; // & down 1 pixel
								break;
						case 2: currPosx--; // move left 1 pixel
								break;
						case 3: currPosx--; // move left 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 4:	currPosy--; // move up 1 pixel
								break;
						case 5: currPosx++; // move right 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 6: currPosx++; // move right 1 pixel
								break;
						case 7: currPosx++; // move right 1 pixel
								currPosy++; // & down 1 pixel
					}

					// let the drunk walk outside/through the boundaries,
					// but only mark steps made inside the 401 x 401 window
					if ((currPosx >= 0) && (currPosx < imgWIDTH)) {
						if ((currPosy >= 0) && (currPosy < imgHEIGHT)) {
							if (i == numSteps) {
								// mark the pos where the drunk falls down (last step) with red
								image.setRGB(currPosx, currPosy, RED);
							}
							else {
								// mark all other steps with black
								image.setRGB(currPosx, currPosy, BLACK);
							}							
						}	
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// boundedPlaneWalkM() - simulation of drunken walk on a bounded plane using Moore neighborhood

	private void boundedPlaneWalkM(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK);
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(8); // step ϵ [0,8)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								currPosy++; // & down 1 pixel
								break;
						case 2: currPosx--; // move left 1 pixel
								break;
						case 3: currPosx--; // move left 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 4:	currPosy--; // move up 1 pixel
								break;
						case 5: currPosx++; // move right 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 6: currPosx++; // move right 1 pixel
								break;
						case 7: currPosx++; // move right 1 pixel
								currPosy++; // & down 1 pixel
					}

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

					if (i == numSteps) {
						// mark the pos where the drunk falls down (last step) with red
						image.setRGB(currPosx, currPosy, RED);
					}
					else {
						// mark all other steps with black
						image.setRGB(currPosx, currPosy, BLACK);
					}
				}
			}
		}
	}

	// ---------------------------------------------------------
	// toroidalPlaneWalkM() - simulation of drunken walk on an toroidal plane using Moore neighborhood

	private void toroidalPlaneWalkM(BufferedImage image, int numSteps) {
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

		Random rand = new Random(); // create new Random object for generating walk sequence

		if (numSteps > 0) { // if numSteps = 0, only the cream background will be displayed
			image.setRGB(startPosx, startPosy, BLACK);
			if (numSteps > 1) {
				currPosx = startPosx; // define current pos
				currPosy = startPosy; // @ start pos
				for (int i = 2; i <= numSteps; i++) {
					int step = rand.nextInt(8); // step ϵ [0,8)
					switch (step) {
						case 0: currPosy++; // move down 1 pixel
								break;
						case 1: currPosx--; // move left 1 pixel
								currPosy++; // & down 1 pixel
								break;
						case 2: currPosx--; // move left 1 pixel
								break;
						case 3: currPosx--; // move left 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 4:	currPosy--; // move up 1 pixel
								break;
						case 5: currPosx++; // move right 1 pixel
								currPosy--; // & up 1 pixel
								break;
						case 6: currPosx++; // move right 1 pixel
								break;
						case 7: currPosx++; // move right 1 pixel
								currPosy++; // & down 1 pixel
					}

					// connect top/bottom boundaries  and
					// left/right boundaries to let drunk
					// walk freely between
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

					if (i == numSteps) {
						// mark the pos where the drunk falls down (last step) with red
						image.setRGB(currPosx, currPosy, RED);
					}
					else {
						// mark all other steps with black
						image.setRGB(currPosx, currPosy, BLACK);
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













