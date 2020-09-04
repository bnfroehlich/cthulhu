package bfroehlich.cthulhu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Main {

	public static final Dimension ctCardSize = new Dimension(80, 123);
	
	public static final Dimension skCardSize = new Dimension(100, 100);
	
	private static Container layoutComponents(String title, float alignment) {
	    String labels[] = { "-", "-", "-" };

	    JPanel container = new JPanel();
	    container.setBorder(BorderFactory.createTitledBorder(title));
	    BoxLayout layout = new BoxLayout(container, BoxLayout.X_AXIS);
	    container.setLayout(layout);
	    for (int i = 0, n = labels.length; i < n; i++) {
		      JButton button = new JButton(labels[i]);
		      button.setAlignmentY(alignment);
		      container.add(button);
		      JButton button2 = new JButton(labels[i]);
		      button2.setAlignmentY(alignment);
		      container.add(button2);
	    }
	    return container;
	  }

	  public static void main(String args[]) {
//	    JFrame frame = new JFrame("Alignment Example");
//	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//	    Container panel1 = layoutComponents("Top", Component.TOP_ALIGNMENT);
//	    Container panel2 = layoutComponents("Center", Component.CENTER_ALIGNMENT);
//	    Container panel3 = layoutComponents("Bottom", Component.BOTTOM_ALIGNMENT);
//
//	    frame.setLayout(new GridLayout(1, 3));
//	    frame.add(panel1);
//	    frame.add(panel2);
//	    frame.add(panel3);
//
//	    frame.setSize(400, 150);
//	    frame.setVisible(true);

//			Font font = javax.swing.UIManager.getDefaults().getFont("Label.font");
//			System.out.println(font.getName());
		  
//		JFrame frame = new JFrame();
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		JScrollPane scroll = new JScrollPane(panel);
//		scroll.setPreferredSize(new Dimension(400, 800));
//		frame.add(scroll);
//		for(Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
//			font = new Font(font.getName(), Font.PLAIN, 12);
//			JLabel label = new JLabel(font.getSize() + " " + font.getName());
//			label.setFont(font);
//			panel.add(label);
//		}
//		frame.pack();
//		frame.setVisible(true);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		  String[] arr = "-a--cthulhu---".split("-");
//		  System.out.println(new ArrayList<String>(Arrays.asList(arr)));
		  
		  JFrame frame = new JFrame();
		  JPanel panel = new JPanel();
		  panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		  frame.add(panel);
		  ImageIcon hourglass = loadImageIcon("hourglass9.gif", -1, -1, false);
		  panel.add(new JLabel(hourglass));
		  frame.pack();
		  frame.setVisible(true);
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  panel.revalidate();
		  panel.repaint();
	  }

	public static Image loadImage(String path, int width, int height, boolean allowScreenScaling) {
		if(path == null) {
			return null;
		}
		Image image = null;
		try {
			image = ImageIO.read(Main.class.getResource("/" + path));
		}
		catch(IOException e) {
			System.err.println(e.getClass());
			System.err.println(e.getMessage());
		}
        image = resize(image, width, height, allowScreenScaling);
        return image;
	}

	public static ImageIcon loadImageIcon(String path, int width, int height, boolean allowScreenScaling) {
		if(path == null) {
			return null;
		}
		ImageIcon icon = new ClearImageIcon(Main.class.getResource("/" + path));
        return icon;
	}
	
	public static class ClearImageIcon extends ImageIcon{
	    public ClearImageIcon(String filename){super(filename);}
	    public ClearImageIcon(URL url){super(url);}

	    @Override
	    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
	        Graphics2D g2 = (Graphics2D)g.create();
	        g2.setBackground(new Color(0,0,0,0));
	        g2.clearRect(0, 0, getIconWidth(), getIconHeight());
	        super.paintIcon(c, g2, x, y);
	    }
	}
	
	public static BufferedImage loadBufferedImage(String path) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(Main.class.getResource("/" + path));
		}
		catch(IOException e) {
			System.err.println(e.getClass());
			System.err.println(e.getMessage());
		}
		return image;
	}
	
	public static Image resize(Image image, int width, int height, boolean allowScreenScaling) {
        if(width > 0 && height > 0) {
			//assume default screen height of 1000 and shrink images if screen is smaller
			Toolkit toolkit = Toolkit.getDefaultToolkit();
	        Dimension screenSize = toolkit.getScreenSize();
	        if(screenSize.height < 1000 && allowScreenScaling) {
	        	width = (width*screenSize.height)/1000;
	        	height = (height*screenSize.height)/1000;
	        }
		
        	image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        }
        return image;
	}
	
	public static Image screenScale(Image image) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		//assume default screen height of 1000 and shrink images if screen is smaller
		Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        if(screenSize.height < 1000) {
        	width = (width*screenSize.height)/1000;
        	height = (height*screenSize.height)/1000;
        }
        if(width > 0 && height > 0) {
        	image = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        }
        return image;
	}
	
	public static BufferedImage rotate(Image img, double angle)
	{
	    double sin = Math.abs(Math.sin(Math.toRadians(angle))),
	           cos = Math.abs(Math.cos(Math.toRadians(angle)));

	    int w = img.getWidth(null), h = img.getHeight(null);

	    int neww = (int) Math.floor(w*cos + h*sin),
	        newh = (int) Math.floor(h*cos + w*sin);

	    BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = bimg.createGraphics();

	    g.translate((neww-w)/2, (newh-h)/2);
	    g.rotate(Math.toRadians(angle), w/2, h/2);
	    g.drawRenderedImage(toBufferedImage(img), null);
	    g.dispose();

	    return bimg;
	}
	
	public static BufferedImage createFlipped(BufferedImage image)
    {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return createTransformed(image, at);
    }

	public static BufferedImage createRotated(BufferedImage image)
    {
        AffineTransform at = AffineTransform.getRotateInstance(
            Math.PI, image.getWidth()/2, image.getHeight()/2.0);
        return createTransformed(image, at);
    }

	public static BufferedImage createTransformed(
        BufferedImage image, AffineTransform at)
    {
        BufferedImage newImage = new BufferedImage(
            image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
}