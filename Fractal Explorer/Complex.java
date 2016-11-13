
public class Complex {
	private double real;
	private double imag;
	
	/*constructor*/
	public Complex(double real, double imag){
		this.real = real;
		this.imag = imag;
	}
	
	/*returns the real part of this complex number*/
	public double getReal() {
		return real;
	}
	
	/*returns the Imaginary part of this complex number*/
	public double getImag() {
		return imag;
	}
	
	/*square this complex number and return the squared complex*/
	public Complex square(){
		double sReal = (real * real) - (imag * imag);
		double sImag = 2 * (real * imag);
		
		return new Complex(sReal,sImag);
	}
	
	/*square this complex number using absolute values (Used for Burning Ship fractal)*/
	public Complex absSquare(){
		double sReal = (real * real) - (imag * imag);
		double sImag = 2 * Math.abs(real * imag);
		
		return new Complex(sReal,sImag);
	}
	
	/*cube this complex number (Used for bird of prey fractal)*/
	public Complex cube(){
		double sReal = (real*real - (imag * imag * 3)) * Math.abs(real);
		double sImag = ((real * real * 3) - imag * imag) * Math.abs(imag);
		
		return new Complex(sReal,sImag);
	}
	
	/*returns the modulus square of this complex number*/
	public double modulusSquared(){
		 return (real * real) + (imag * imag);
	}
	
	/*adds a complex number to this complex number*/
	public Complex add(Complex d){
		double newReal = real + d.getReal();
		double newImag = imag + d.getImag();
		
		return new Complex(newReal, newImag);
	}
}
