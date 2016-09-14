// BilinearGradient.java
// -- creates a bilinear gradient of four defined colors in each corner of the image
// Author: Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class BilinearGradient {
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
	// name colors by their positions so variable names still make sense when new colors are tested
	private final int NE = 0xFFFFFF00; // yellow in NE corner
	private final int NW = 0xFF0000FF; // blue in NW corner
	private final int SE = 0xFFFF0000; // red in SE corner
	private final int SW = 0xFF00FF00; // green in SW corner

	// declare arrays to store extracted color channels
	private final int[] NE_chnls;
	private final int[] NW_chnls;
	private final int[] SE_chnls;
	private final int[] SW_chnls;

	private BufferedImage image = null; // initialize null BufferedImage to change later

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		// extract color channels
		NE_chnls = new int[] {(NE >>> 24), ((NE >>> 16) & 0xFF), ((NE >>> 8) & 0xFF), (NE & 0xFF)};
		NW_chnls = new int[] {(NW >>> 24), ((NW >>> 16) & 0xFF), ((NW >>> 8) & 0xFF), (NW & 0xFF)};
		SE_chnls = new int[] {(SE >>> 24), ((SE >>> 16) & 0xFF), ((SE >>> 8) & 0xFF), (SE & 0xFF)};
		SW_chnls = new int[] {(SW >>> 24), ((SW >>> 16) & 0xFF), ((SW >>> 8) & 0xFF), (SW & 0xFF)};


		// ---------------------------------------------------------
		// setup the frame's attributes

		this.setTitle("CAP 3027 2016 - HW02 - Brandon Peterson");
		this.setSize(width, height);

		// add a menu to the frame
		addMenu();
	}

	private void addMenu() {
		// ---------------------------------------------------------
		// setup the frame's attributes

		// === File menu

		JMenu fileMenu = new JMenu("File");

		// --- Bilinear gradient

		JMenuItem bilinGradItem = new JMenuItem("Bilinear gradient");
		bilinGradItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					int imgSize = promptForSize(); // get image size from user
					image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB); // construct image for bilinear gradient
					createBilinGrad(image, imgSize); // create bilinear gradient
					displayBufferedImage(image); // display final image		
				}
			}	);

		fileMenu.add(bilinGradItem);	

		// --- Exit

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event) {
					System.exit(0);
				}
			}	);

		fileMenu.add(exitItem);	

		// === attach menu to a menu bar

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		this.setJMenuBar(menuBar);
	}

	// ---------------------------------------------------------
	// promptForSize() - prompt the user for the image size (height == width)

	private int promptForSize() {
		String result = JOptionPane.showInputDialog("Enter your desired size n (for an nxn size image)"); // get String input from option pane
		int size = 0; // initialize to zero before checking correct format

		try {
			size = Integer.parseInt(result); // convert size to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (size < 0) {
			// don't allow negative input
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		return size;
	}

	// ---------------------------------------------------------
	// createBilinGrad(): draws bilinear gradient on image using 4 previously defined colors
	// - interpolations are created along each row from top to bottom

	// params:
	// - image = square image on which to draw
	// - imgSize = size n of said nxn image

	private void createBilinGrad(BufferedImage image, int imgSize) {
		// declare arrays to store step values for each edge channel
		double[] leftEdge_deltas = new double[4];
		double[] rightEdge_deltas = new double[4];
		// compute step values for each edge channel
		for (int i = 0; i < 4; i++) {
			leftEdge_deltas[i]  = (double) (SW_chnls[i] - NW_chnls[i])/(imgSize-1);
			rightEdge_deltas[i] = (double) (SE_chnls[i] - NE_chnls[i])/(imgSize-1);
		}

		// declare arrays to store edge channel values
		double[] leftEdge_chnls = new double[4];
		double[] rightEdge_chnls = new double[4];
		// initialize left/right edge channel values to NW/NE colors
		for (int i = 0; i < 4; i++) {
			leftEdge_chnls[i]  = NW_chnls[i];
			rightEdge_chnls[i] = NE_chnls[i];
		}	

		// declare array to store step values for each channel for each row interpolation
		double[] row_deltas = new double[4];

		// declare array to store current value for each channel
		double[] curr_chnls = new double[4];

		for (int row = 0; row < imgSize; row++) {
			// compute new row step values for each channel
			for (int i = 0; i < 4; i++) {
				row_deltas[i] = (rightEdge_chnls[i] - leftEdge_chnls[i])/(imgSize-1);
			}

			// set current pixel channels to left edge pixel channels (start value)
			for (int i = 0; i < 4; i++) {
				curr_chnls[i] = leftEdge_chnls[i];
			}

			for (int col = 0; col < imgSize; col++) {
				// set current pixel color
				image.setRGB(col,row, synthColor(curr_chnls[0], curr_chnls[1], curr_chnls[2], curr_chnls[3]));

				// increment current channel values by corresponding row deltas
				for (int i = 0; i < 4; i++) {
					curr_chnls[i] += row_deltas[i];
					curr_chnls[i] = clamp(curr_chnls[i]);
				}
			}

			// increment edge channel values by corresponding edge deltas
			for (int i = 0; i < 4; i++) {
				leftEdge_chnls[i] += leftEdge_deltas[i];
				rightEdge_chnls[i] += rightEdge_deltas[i];
			}	
		}
	}

	// synthesize int color given indivual ARGB int channels
	private int synthColor(double alpha, double red, double green, double blue) {
		return ((int) alpha << 24) | ((int) red << 16) | ((int) green << 8 ) | (int) blue;		
	}

	// clamp channel value to smallest/largest possible value if out of possible range [0,255]
	private double clamp(double val) {
		if (val < 0.0) {
			val = 0.0;
		}
		else if (val > 255.0) {
			val = 255.0;
		}
		return val;
	}

	// ---------------------------------------------------------
	// Display BufferedImage

	public void displayBufferedImage(BufferedImage image) {
		// display resulting image 
		this.setContentPane(new JScrollPane(new JLabel(new ImageIcon(image))));

		this.validate();
	}
}