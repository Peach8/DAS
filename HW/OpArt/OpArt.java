// OpArt.java
// - 
// by Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
// import java.util.Random;

public class OpArt {
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
	private final JFileChooser chooser;
	private BufferedImage image = null; // initialize null BufferedImage to change later

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		// ---------------------------------------------------------
		// setup the frame's attributes

		this.setTitle("CAP 3027 2016 - HW04 - Brandon Peterson");
		this.setSize(width, height);

		// add a menu to the frame
		addMenu();

		// ---------------------------------------------------------
		// setup the file chooser dialog

		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));		
	}

	private void addMenu() {
		// === File menu
		JMenu fileMenu = new JMenu("File");

		// --- Load source image
		JMenuItem loadSrcImgItem = new JMenuItem("Load source image");
		loadSrcImgItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					open();	
				}
			}	);
		fileMenu.add(loadSrcImgItem);	

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

	// ---------------------------------------------------------
	// open() - choose a file, load, and display the image

	private void open() {
		File file = getFile();
		if ( file != null )
		{
			displayFile(file);
		}
	}

	// ---------------------------------------------------------
	// Open a file selected by the user.

	private File getFile() {
		File file = null;

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}

		return file;
	}	

	// ---------------------------------------------------------
	// Display specified file in the frame

	private void displayFile( File file ) {
		try {
			displayBufferedImage(ImageIO.read(file));
		}
		catch (IOException exception) {
			JOptionPane.showMessageDialog(this, exception);
		}
	}	

	// ---------------------------------------------------------
	// promptForDiameter() - 

	private int promptForDiameter() {
		String result = JOptionPane.showInputDialog(""); // get String input from option pane
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
	// Display BufferedImage

	public void displayBufferedImage(BufferedImage image) {
		// display resulting image 
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}