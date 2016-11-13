import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;



public class JuliaPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private double minR;
	private double maxR;
	private double minI;
	private double maxI;
	private int maxIterations;
	private int width;
	private int height;
	private int red;
	private int green;
	private int blue;
	private int brightnessLevel;
	private int fractalType;
	private int orbitOption;
	private Complex c;
	private boolean orbitTrapEnabled;
	private BufferedImage offScreenJulia;
	private ArrayList<Complex> savedJulias;

	/*set the value of each color in RGB model*/
	public void setColors(int red, int green, int blue){
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/*set maximum number of iterations*/
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/*set brightness level*/
	public void setBrightnessLevel(int brightnessLevel) {
		this.brightnessLevel = brightnessLevel;
	}

	/*set which fractal to calculate and display*/
	public void setFractalType(int fractalType) {
		this.fractalType = fractalType;
	}
	
	/*set whether to use Orbit Traps or not*/
	public void setOrbitTrapEnabled(boolean option){
		orbitTrapEnabled = option;
	}

	/*set which Orbit Trap style to use*/
	public void setOrbitOption(int option){
		orbitOption = option;
	}
	
	/*save the Julia for the user selected point as a favourite in an ArrayList*/
	public void saveJulia(int index){
		savedJulias.add(MainFractalPanel.userSelectedPoint);
	}

	/*generate and display the julia for the selected favorite from the arrayList*/
	public void loadJulia(int index){
		c = savedJulias.get(index);
		if(orbitTrapEnabled){
			//generate with Orbit Trap
			setOrbitOption(SidePanel.orbitTrapList.getSelectedIndex());
			calculateOrbitTrapFractal();
		}else{
			//generate without Orbit Trap
			calculateJulia();
		}
	}

	/*save the selected favorite julia as an image file (.PNG)*/
	public void saveAsImage(String fileName){
		File outputfile = new File(fileName + ".png");
		
		//save the BufferedImage as a .PNG file
		try {
			ImageIO.write(offScreenJulia, "png", outputfile);
		} catch (IOException e) {}
	}

	/*set the value of the complex c*/
	public void setC(Complex c){
		this.c = c;
	}

	/*constructor*/
	public JuliaPanel(){
		minR = -2;
		maxR = 2;
		minI = -1.6;
		maxI = 1.6;
		maxIterations = 100;	
		brightnessLevel = 0;
		fractalType = 0;
		orbitOption = 0;
		orbitTrapEnabled = false;
		savedJulias = new ArrayList<Complex>();
		this.setPreferredSize(new Dimension(520,520));
	}

	/*Get the size of this panel and create a BufferedImage which these dimensions (needed as borderLayout is used)*/
	public void setDimensions(){
		width = this.getWidth();
		height = this.getHeight();
		offScreenJulia = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
	}

	private Complex z = new Complex(0,0);

	/*calculates the Fractal display for each pixel on the BufferedImage for the Julia fractal using
	 * the same algorithm as the main fractal. Creates Image of fractal on BufferedImage*/
	public void calculateJulia(){
		double x;
		double y;
		int iterations;

		//Calculates row by row
		for(int i = 0; i < height; i++){
			//for each pixel in the row
			for(int j = 0; j < width; j++){
				iterations = 0;
				
				//convert pixel's position to point on complex plane
				x = minR + j*(maxR - minR)/width;
				y = maxI - i*(maxI - minI)/height;
				z = new Complex(x,y);

				/*count iterations until the distance of this complex point from the origin exceeds 2 or the number of iterations
				 *  reaches the max iterations limit.
				 */
				while(z.modulusSquared() < 4 && iterations < maxIterations){
					//Use method to calculate selected fractal type
					switch (fractalType) {
					case 0:  mandelbrotJulia();
					break;
					
					case 1:  triplebrotJulia();
					break;
					
					case 2:  burningShipJulia();
					break;

					case 3:  birdOfPreyJulia();
					break;
					}

					iterations++;
				}

				float interval = 0;

				/*converts the number of iterations taken to a smooth value, allowing for a smoother gradient with more colors
				to be used*/
				if(iterations < maxIterations){
					interval = (float) (iterations + 1 - Math.log(Math.log(Math.sqrt(z.modulusSquared())))/Math.log(2));
				}
				
				//draw the 	pixel to the bufferedImage in the correct position and color
				offScreenJulia.setRGB(j, i, colorFractal(interval));
			}
		}
		repaint();
	}

	/*calculates the Fractal display for each pixel on the BufferedImage for the selected fractal.
	 * Creates Image of fractal on BufferedImage. Uses Orbit Trap Coloring method instead of Smooth Coloring*/
	public void calculateOrbitTrapFractal(){
		double x;
		double y;
		int iterations;

		//Calculates row by row
		for(int i = 0; i < height; i++){
			//for each pixel in the row
			for(int j = 0; j < width; j++){
				iterations = 0;
				
				//convert pixel's position to point on complex plane
				x = minR + j*(maxR - minR)/width;
				y = maxI - i*(maxI - minI)/height;
				z = new Complex(x,y);

				double dist = 0;

				/*count iterations until the distance of this complex point from the origin exceeds 2, the number of iterations
				 *  reaches the max iterations limit or until the complex point is trapped by the Orbit Trap
				 */
				while(z.modulusSquared() < 4 && iterations < maxIterations && dist == 0){
					//Use method to calculate selected fractal type
					switch (fractalType) {
					case 0:  mandelbrotJulia();
					break;

					case 1:  triplebrotJulia();
					break;

					case 2:  burningShipJulia();
					break;

					case 3:  birdOfPreyJulia();
					break;
					}
					
					//Check if complex point is trapped by the Orbit Trap
					dist = orbitTrap(orbitOption, iterations, dist);

					iterations++;
				}
				//draw the 	pixel to the bufferedImage in the correct position with the correct color
				offScreenJulia.setRGB(j, i, colorOrbitTrap(iterations, dist));
			}
		}
		repaint();
	}

	double trapSize;

	/*Checks whether the complex point is trapped within the orbit trap method that is being used and returns
	 * the distance of the point from the origin
	 */
	public double orbitTrap(int option, int count, double dist){
		//elipse Orbit Trap. Creates large ring in the Complex Plane which is centered on origin
		if(option == 0){
			trapSize = 0.64;
			if(Math.sqrt(z.modulusSquared()) < 0.65 && Math.sqrt(z.modulusSquared()) > 0.62){
				dist = trapSize - Math.sqrt(z.modulusSquared()) + 0.4;
				return Math.abs(dist);
			}
		}

		//circle Orbit Trap. Creates large circle in the Complex Plane which is centered on origin
		if(option == 1){
			trapSize = 0.25;
			if(Math.sqrt(z.modulusSquared()) < 0.25){
				dist = trapSize - Math.sqrt(z.modulusSquared());
				return dist;
			}
		}

		//dots Orbit Trap. Creates small circle in the Complex Plane which is centered on the origin
		if(option == 2){
			trapSize = 0.05;
			if(Math.sqrt(z.modulusSquared()) < 0.05){
				dist = trapSize - Math.sqrt(z.modulusSquared());
				return dist;
			}
		}

		//rings Orbit Trap. creates a small ring in the Complex Plane which is cenetered on the origin
		if(option == 3){
			trapSize = 0.195;
			if(Math.sqrt(z.modulusSquared()) < 0.20 && Math.sqrt(z.modulusSquared()) > 0.19){
				dist = trapSize - Math.sqrt(z.modulusSquared()) + 0.2;
				return Math.abs(dist);
			}
		}

		//lines Orbit Trap. Creates a vertical and horizontal line of the Complex Plane, through the origin
		if(option == 4){
			trapSize = 0.01;
			if(Math.abs(z.getReal()) < 0.01){
				dist = trapSize - Math.abs(z.getReal());
				return dist;
			}
			if(Math.abs(z.getImag()) < 0.01){
				dist = trapSize - Math.abs(z.getImag());
				return dist;
			}
		}

		/*Gaussian Integer algorithim. Calculates the distance of the Complex point from the nearest Guassian Integer and uses
		 * this value to color the point.
		 * NOTE: This algorithm is quite slow. I don't recommend zooming in too much.
		 */
		if(option == 5){
			double downR = Math.floor(z.getReal());
			double downI = Math.floor(z.getImag());
			double upR = Math.ceil(z.getReal());
			double upI = Math.ceil(z.getImag());

			Complex low = new Complex(Math.abs(z.getReal() - downR), Math.abs(z.getImag() - downI));
			Complex high = new Complex(Math.abs(z.getReal() - upR), Math.abs(z.getImag() - upI));

			double nearestDistance = Math.min(low.modulusSquared(), high.modulusSquared());

			trapSize = 0.01;
			if(nearestDistance < 0.01 && nearestDistance > 0.0025){
				dist = trapSize - nearestDistance;
				//System.out.println(nearestDistance);
				return Math.abs(dist);
			}
		}

		//sine wave Orbit Trap. Creates a sine wave on the Complex Plane. Transformed to be half as wide.
		if(option == 6){
			trapSize = 1.9;
			//limit domain between -1.8 and 1.8
			if(z.getReal() < 1.8 && z.getReal() > -1.8){
				if(z.getImag() < Math.sin(z.getReal()*2) && z.getImag() > Math.sin(z.getReal()*2) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					return Math.abs(dist);
				}
			}
		}

		//cos wave Orbit Trap. Creates a cos wave on the Complex Plane. Transformed to be half as wide.
		if(option == 7){
			trapSize = 2.1;
			//limit domain between -1.8 and 1.8
			if(z.getReal() < 1.8 && z.getReal() > -1.8){
				if(z.getImag() < Math.cos(z.getReal()*2) && z.getImag() > Math.cos(z.getReal()*2) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					//System.out.println(Math.abs(Math.sqrt(z.modulusSquared())));
					return Math.abs(dist);
				}
			}
		}

		/*Flower Orbit Trap. Creates four parabolas in opposing directions and limits the range/domain so that the parabolas
		 *  stop when they intersect. Uses Quadratic Equations.
		 */
		if(option == 8){
			//limit range to -1.0 and 1.0
			if(z.getImag() < 1.0 && z.getImag() > -1.0){
				trapSize = 1.0;
				if(z.getImag() < (z.getReal()*z.getReal()) && z.getImag() > (z.getReal()*z.getReal()) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					return Math.abs(dist);
				}

				if(z.getImag() < -(z.getReal()*z.getReal()) && z.getImag() > -(z.getReal()*z.getReal()) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					return Math.abs(dist);
				}
			}

			//limit domain to -1.0 and 1.0
			if(z.getReal() < 1.0 && z.getReal() > -1.0){
				trapSize = 1.0;
				if(z.getReal() < (z.getImag()*z.getImag()) && z.getReal() > (z.getImag()*z.getImag()) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					return Math.abs(dist);
				}

				if(z.getReal() < -(z.getImag()*z.getImag()) && z.getReal() > -(z.getImag()*z.getImag()) - 0.015){
					dist = trapSize - Math.sqrt(z.modulusSquared());
					return Math.abs(dist);
				}
			}
		}

		//Petal Orbit Trap. Uses both sin and cos waves to creates images on the complex plane which vaguely resemble petals of a flower
		if(option == 9){
			trapSize = 5.5;
			if(z.getImag() < Math.sin(z.getReal()) && z.getImag() > Math.cos(z.getReal() - 0.5)){
				dist = trapSize - Math.sqrt(z.modulusSquared());
				return Math.abs(dist);
			}
		}

		return dist;
	}

	/*color the current pixel depending on the smooth value calculated from the number of iterations taken*/
	public int colorFractal(float interval){		
		Color color = new Color(0,0,0);

		//calulate RGB value for red, green and blue parts. Limited to 255 as some calculations create numbers greater than 255
		int calcRed = Math.min((int)(interval/maxIterations * red), 255);
		int calcGreen = Math.min((int)(interval/maxIterations * green), 255);
		int calcBlue = Math.min((int)(interval/maxIterations * blue), 255);

		//construct RGB Color with calculated red, green and blue values
		color = new Color(calcRed, calcGreen, calcBlue);

		//brightens the color depending on brightness level selected.
		for(int i = 0; i < brightnessLevel; i++){
			color = color.brighter();
		}

		int finalColor = color.getRGB();
		
		return finalColor;
	}

	/*Color the current pixel depending on the result of the Orbit Trap.
	 * Uses region splits to color three groups of traps three specific colors. 
	 */
	public int colorOrbitTrap(int count, double dist){
		Color color = new Color(0,0,0);

		//RGB values for first oolor
		int oRed = Math.min((int)(dist/trapSize*255), 255);
		int oGreen = 0;
		int oBlue = 0;

		//color with this color if the number of iterations taken is even
		if(count%2 == 0)
			color = new Color(oRed,oGreen,oBlue);

		//RGB values for second color
		oRed = Math.min((int)(dist/trapSize*255), 255);
		oGreen = Math.min((int)(dist/trapSize*153), 255);

		//color with this color if the number of iterations taken is odd
		if(count%2 == 1)
			color = new Color(oRed,oGreen,oBlue);

		//RGB values for third color
		oRed = Math.min((int)(dist/trapSize*255), 255);
		oGreen = Math.min((int)(dist/trapSize*204), 255);
		oBlue = Math.min((int)(dist/trapSize*184), 255);

		//color with this color if the number of iterations taken is a multiple of 3
		if(count%3 == 0)
			color = new Color(oRed,oGreen,oBlue);
		
		int finalColor = color.getRGB();

		return finalColor;
	}

	/*calculates julia for Mandelbrot fractal*/
	public void mandelbrotJulia(){
		Complex tmp = new Complex(0,0);
		double nextReal;
		double nextImag;

		tmp = z.square();
		tmp = tmp.add(c);	
		nextReal = tmp.getReal();
		nextImag = tmp.getImag();
		z = new Complex(nextReal,nextImag);
	}
	
	/*While the following are technically not julia sets, I used changed the calculations used
	 * to reflect the fractal being displayed on the main fractal so that each "Julia"
	 * set is different for each fractal
	 */
	
	/*calculates julia for triplebrot fractal*/
	public void triplebrotJulia(){
		Complex tmp = new Complex(0,0);
		double nextReal;
		double nextImag;

		tmp = z.square();
		tmp = tmp.square();
		tmp = tmp.add(c);	
		nextReal = tmp.getReal();
		nextImag = tmp.getImag();
		z = new Complex(nextReal,nextImag);
	}

	/*calculates julia for Burning Ship fractal. The Julia set is flipped to reflect
	 * the flipped burning Ship on the main fractal panel*/
	public void burningShipJulia(){
		Complex tmp;
		double nextReal;
		double nextImag;

		tmp = z.absSquare();
		tmp = new Complex(tmp.getReal() + c.getReal(),tmp.getImag() - c.getImag());	
		nextReal = tmp.getReal();
		nextImag = tmp.getImag();
		z = new Complex(nextReal,nextImag);
	}
	
	/*calculates julia for bird of Prey fractal*/
	public void birdOfPreyJulia(){
		Complex tmp;
		double nextReal;
		double nextImag;

		tmp = z.cube();
		tmp = tmp.add(c);	
		nextReal = tmp.getReal();
		nextImag = tmp.getImag();
		z = new Complex(nextReal,nextImag);
	}

	/*Draws the BufferedImage*/
	public void paintComponent(Graphics g){
		super.paintComponent(g);	
		g.drawImage(offScreenJulia, 0, 0, this);
	}

}

