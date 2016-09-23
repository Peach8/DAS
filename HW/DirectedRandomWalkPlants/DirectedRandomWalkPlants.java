// DirectedRandomWalkPlants.java -
//
// by Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class DirectedRandomWalkPlants {
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
	// variables
	private int imgSize; // height == width
	private int numStems;
	private int stepsPerStem;
	private float transProb; // transmission probablity ϵ [0.0,1.0]
	private float maxRotInc; // max rotation increment ϵ [0.0,1.0]
	private int growthInc; // growth segment increment


	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
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
					transProb = promptUserForFloat("Enter the desired transmission probability.");
					maxRotInc = promptUserForFloat("Enter the desired maximum rotation increment.");
					growthInc = promptUserForInt("Enter the desired growth segment increment.");
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
	// promptUserForFloatt() -
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

	// ---------------------------------------------------------------
	// displayBufferedImage() - displayes an image in the content pane
	// 	params: BufferedImage image = image to display
	public void displayBufferedImage(BufferedImage image) {
		// display resulting image
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}
