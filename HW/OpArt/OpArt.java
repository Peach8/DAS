// OpArt.java
// - generates a circular pixelated version of a user-selected image
//   using circles of a user-specified diameter
// - non-square source images will be truncated to their maximum top-left portion
// by Brandon Peterson

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class OpArt {
	private static final int frameWIDTH  = 750; // ImageFrame width
	private static final int frameHEIGHT = 750; // ImageFrame height

	public static void main(String[] args) {
		JFrame frame = new ImageFrame(frameWIDTH, frameHEIGHT); // create ImageFrame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close widget quits app (doesn't just hide)
		frame.setVisible(true); // make frame visible
	}
}

//###########################################################################

class ImageFrame extends JFrame {
	private static final int BLACK = 0xFF000000; // background color

	private final JFileChooser chooser; // create object to open files

	private int diameter; // diameter of circles
	private int srcWidth; // width of original opened file
	private int srcHeight; // height of original opened file
	private BufferedImage srcImg = null; // init null BufferedImage for source image
	private double tgtSize; // width == height of target image
	private BufferedImage tgtImg = null; // init null BufferedImage for target image

	private Ellipse2D.Double circle; // reuse same circle obj during recursion

	//==============================================================
	// constructor

	public ImageFrame(int width, int height) {
		// setup the frame's attributes
		this.setTitle("CAP 3027 2016 - HW04 - Brandon Peterson");
		this.setSize(width, height);

		addMenu(); // add a menu to the frame

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
					open();	// open/display img file
					promptUser(); // prompt user for necessary fields (diameter)					
					tgtImg = new BufferedImage((int) tgtSize, (int) tgtSize, BufferedImage.TYPE_INT_ARGB);
					genOpArt(tgtImg); // generate circular pixel recursively
					displayBufferedImage(tgtImg); // display final image
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

	// ---------------------------------------------------
	// open() - choose a file, load, and display the image
	private void open() {
		File file = getFile();
		if (file != null) {
			displayFile(file);
		}
	}

	// --------------------------------------------
	// getFile() - open a file selected by the user
	private File getFile() {
		File file = null;

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}

		return file;
	}

	// ------------------------------------------------------------
	// displayFile() - read and display specified file in the frame
	// 	params: File file = file to display
	private void displayFile(File file) {	
		try {
			this.srcImg = ImageIO.read(file); // save file source img
			// assign width/height of selected file to class variables
			this.srcWidth = this.srcImg.getWidth();
			this.srcHeight = this.srcImg.getHeight();
			this.tgtSize = Math.min(this.srcWidth, this.srcHeight); // assign target width/height
			displayBufferedImage(this.srcImg); // display img file
		}
		catch (IOException exception) {
			JOptionPane.showMessageDialog(this, exception);
		}
	}	

	// ----------------------------------------------------------
	// promptUser(): assign private variables based on user input
	private void promptUser() {
		this.diameter = promptForDiameter("Enter the minimum circle diameter.");
	}

	// -----------------------------------------------------------
	// promptForDiameter() - get minimum desired diamter from user
	// 	params: String msg = message w/ which to prompt user
	private int promptForDiameter(String msg) {
		String result = JOptionPane.showInputDialog(msg); // get String input from option pane
		int diam = 0; // init diameter to zero before checking correct format

		try {
			diam = Integer.parseInt(result); // convert diameter to int form
		}
		catch (NumberFormatException exception) {
			JOptionPane.showMessageDialog(this, exception); // throw exception
		}
		if (diam < 0) {
			// don't allow negative input
			JOptionPane.showMessageDialog(this, "Input must be non-negative.", "Input must be non-negative.", JOptionPane.ERROR_MESSAGE);
		}
		return diam;
	}

	// ------------------------------------------------------------------
	// genOpArt() - generate pixelated version of image
	// 	params: BufferedImage image = target image on which to draw based
	//          on pixel value fetched from source image
	private void genOpArt(BufferedImage image) {
		setBackground(image, BLACK);

		// create Graphics2D object to draw & minimize lines with antialiasing
		Graphics2D g2D = (Graphics2D) image.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		this.circle = new Ellipse2D.Double(); // use the same circle object for each image generation
		g2D.setColor(getAvgColor(0, 0, (int) this.tgtSize)); // get avg color of entire source image
		circle.setFrame(0, 0, this.tgtSize, this.tgtSize); // inscribe circle in target image square
		g2D.fill(this.circle); // fill big circle w/ avg color of entire source image

		// recursively split image into 4 quadrants and draw circles based on corresponding region's avg color in source image
		fillQuadrants(g2D, 0, 0, this.tgtSize);
	}

	// ------------------------------------------------------------------
	// setBackground() - set every pixel in an image the same color
	// 	params: BufferedImage image = image to change background color of
	//          int color = desired background color
	private void setBackground(BufferedImage image, int color) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				image.setRGB(i,j, color);
			}
		}
	}

	// -----------------------------------------------------------------------------------------
	// fillQuadrants() - recurive method to split image into 4 quadrants and draw
	//                   circles based on corresponding region's avg color in source image
	// 	params: Graphics2D g2D = Graphics2D object used to draw/fill circles
	//          double x = top left x position of frame in which to inscribe circle
	//          double y = top left y position of frame in which to inscribe circle
	//			double width = width of frame in which to inscribe circle, i.e., circle diameter
	private void fillQuadrants(Graphics2D g2D, double x, double y, double width) {
		// top-left quadrants
		g2D.setColor(getAvgColor((int) x, (int) y, (int) (width/2.0)));
		circle.setFrame(x, y, width/2.0, width/2.0);
		g2D.fill(this.circle);
		if ((int) width >= this.diameter) {
			fillQuadrants(g2D, x, y, width/2.0);
		}		

		// top-right quadrants
		g2D.setColor(getAvgColor((int) (x + width/2.0), (int) y, (int) (width/2.0)));
		circle.setFrame(x + width/2.0, y, width/2.0, width/2.0);
		g2D.fill(this.circle);
		if ((int) width >= this.diameter) {
			fillQuadrants(g2D, x + width/2.0, y, width/2.0);
		}

		// bottom-left quadrants
		g2D.setColor(getAvgColor((int) x, (int) (y + width/2.0), (int) (width/2.0)));
		circle.setFrame(x, y + width/2.0, width/2.0, width/2.0);
		g2D.fill(this.circle);
		if ((int) width >= this.diameter) {
			fillQuadrants(g2D, x, y + width/2.0, width/2.0);
		}

		// bottom-right quadrants
		g2D.setColor(getAvgColor((int) (x + width/2.0), (int) (y + width/2.0), (int) (width/2.0)));
		circle.setFrame(x + width/2.0, y + width/2.0, width/2.0, width/2.0);
		g2D.fill(this.circle);
		if ((int) width >= this.diameter) {
			fillQuadrants(g2D, x + width/2.0, y + width/2.0, width/2.0);
		}
	}

	// ----------------------------------------------------------------------------------
	// getAvgColor() - returns average color of a specified square region in source image
	// 	params: int x = top left x position of desired region
	//          int y = top left y position of desired region
	//          int width = width (and height) of desired region
	private Color getAvgColor(int x, int y, int width) {
		int red   = 0;
		int green = 0;
		int blue  = 0;
		int n     = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < width; j++) {
				red   += ((this.srcImg.getRGB(i+x,j+y) >>> 16) & 0xFF);
				green += ((this.srcImg.getRGB(i+x,j+y) >>> 8)  & 0xFF);
				blue  += (this.srcImg.getRGB(i+x,j+y)          & 0xFF);
				++n;
			}
		}
		Color avgColor = new Color(red/n, green/n, blue/n);
		return avgColor;
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
