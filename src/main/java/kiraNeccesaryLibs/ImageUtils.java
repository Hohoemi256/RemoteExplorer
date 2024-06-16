package kiraNeccesaryLibs;

/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval �
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 30.11.14 13:51
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.Kernel;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * Some little helper methods.<br>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 02.02.2006
 * <br>Time: 23:33:36
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageUtils {
	
	/**
     * Resizes the image to the given new boundaries. Will not resize anything if the new boundaries match the original ones
     * @param img the image to resize
     * @param newWidth the new Width of the resized image
     * @param newHeight the new heigth of the resized image
     * @return the resized image with the new boundaries
     */
	public static BufferedImage resize(BufferedImage img, int newWidth, int newHeight)
	{
		//Check if boundaries did NOT change
		if(img.getWidth()==newWidth && img.getHeight()==newHeight) {return img;}
		
		//Resize image
		Image toolkitImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);


			// width and height are of the toolkit image
		BufferedImage newImage = new BufferedImage(toolkitImage.getWidth(null), toolkitImage.getHeight(null), 
//			BufferedImage newImage = new BufferedImage(newWidth, newHeight, 
			      BufferedImage.TYPE_INT_ARGB);
			Graphics g = newImage.getGraphics();
			g.drawImage(toolkitImage, 0, 0, null);
			g.dispose();
			return newImage;
	}
	
	public static Image resize(Image img, int newWidth, int newHeight){
		return img.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
	}
	
	
	public static BufferedImage resizeImage(Image image, int w, int h) {
//	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//	    Graphics2D g2 = resizedImg.createGraphics();
//
//	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//	    g2.drawImage(image, 0, 0, w, h, null);
//	    g2.dispose();
//
//	    return resizedImg;
//	    
		Image toolkitImage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);


		// width and height are of the toolkit image
		BufferedImage newImage = new BufferedImage(toolkitImage.getWidth(null), toolkitImage.getHeight(null), 
		      BufferedImage.TYPE_INT_ARGB);
		Graphics g = newImage.getGraphics();
		g.drawImage(toolkitImage, 0, 0, null);
		g.dispose();
		return newImage;
}

	//--------------------------------------------------------------------------------------------------------------------------
	
	  public static Image setWhiteTransperant (Image image){return makeColorTransparent(image, Color.WHITE);}

	/**
	 * Returnes the picture, where every pixel of the specified color will be set to transparent. 
	 * @param im the image to work on
	 * @param color the color which will be transparent in the end
	 * @return the resulting image
	 */
	  public static Image makeColorTransparent (Image im, final Color color) {
		    ImageFilter filter = new RGBImageFilter() {
		      // the color we are looking for... Alpha bits are set to opaque
		      public int markerRGB = color.getRGB() | 0xFF000000;
	
		      public final int filterRGB(int x, int y, int rgb) {
		        if ( ( rgb | 0xFF000000 ) == markerRGB ) {
		          // Mark the alpha bits as zero - transparent
		          return 0x00FFFFFF & rgb;
		          }
		        else {
		          // nothing to do
		          return rgb;
		          }
		        }
		      }; 
	
		    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		    return Toolkit.getDefaultToolkit().createImage(ip);
		    }

	  
	  /**
	   * Changes all pixels with the specific color to a new one
	   * @param im The Image which is beeing worked on
	   * @param replace the Color to replace
 	   * @param newColor the new Color the pixels will have
	   * @return the Image with the new Pixels
	   */
		public static Image colorChanger(Image im, Color replace, Color newColor){

		    ImageFilter filter = new RGBImageFilter() {
		      // the color we are looking for... Alpha bits are set to opaque

		      public final int filterRGB(int x, int y, int rgb) {
		        if ( rgb  == replace.getRGB() ) {
		          // Mark the alpha bits as zero - transparent
		          return newColor.getRGB();
		          }
		        else {
		          // nothing to do
		          return rgb;
		          }
		        }
		      }; 

		    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		    return Toolkit.getDefaultToolkit().createImage(ip);
		    
		}
		
	//--------------------------------------------------------------------------------------------------------------------------


	
}
