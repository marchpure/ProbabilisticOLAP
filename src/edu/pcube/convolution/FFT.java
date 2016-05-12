package edu.pcube.convolution;

import org.apache.commons.math3.complex.Complex;


public class FFT {

	// compute the FFT of x[], assuming its length is a power of 2
	public static Complex[] fft(Complex[] a, int level) {
		int n = a.length;

		if (n == 1)
			return a;

		Complex e = new Complex(Math.E);
		Complex principal = e.pow(Complex.I.multiply(2*Math.PI/n)).conjugate();
		principal = Tools.round(principal, 10);
		Complex omega = new Complex(1);

		int halfUp = (int)Math.ceil((double)n/2);
		int halfDown = n/2;

		Complex[] a0 = new Complex[halfUp];
		Complex[] a1 = new Complex[halfDown];
		for (int i = 0; i < a0.length; i++)
			a0[i] = a[i*2];
		for (int i = 0; i < a1.length; i++)
			a1[i] = a[i*2 + 1];

		// recursive calls
		Complex[] y0 = fft(a0, level + 1);
		Complex[] y1 = fft(a1, level + 1);

		// combine y0 and y1 back into a single vector
		Complex[] y = new Complex[n];
		for (int k = 0; k < halfDown; k++) {
			omega = Tools.round(omega, 5);

			//show(y1, "y1");

			y[k] 			= y0[k].add(omega.multiply(y1[k]));
			y[k + halfDown] = y0[k].subtract(omega.multiply(y1[k]));

			omega = omega.multiply(principal);
		}

		// y is assumed to be a column vector
		return y;
	}

	public static Complex[] ifft(Complex[] vector){
		int n = vector.length ;
		int exp = 1;
		while (Math.pow(2, exp) < n)
			exp++;
		int length = (int) Math.pow(2, exp);
		vector = pad(vector, length);


		for (int i = 0; i < vector.length; i++)
			vector[i] = vector[i].conjugate();
		vector = fft(vector, 0);

		for (int i = 0; i < vector.length; i++) {
			vector[i] = vector[i].conjugate();
			vector[i] = vector[i].divide(vector.length);
		}
		return vector;
	}


	public static Complex[] pad(Complex[] vector, int size) {
		Complex[] padded = new Complex[size];
		Complex ZERO = new Complex(0);
		for (int i = 0; i < size; i++) {
			if (i < vector.length)
				padded[i] = vector[i];
			else
				padded[i] = ZERO;
		}
		return padded;
	}

	// compute the linear convolution of x and y
	public static Complex[] convolution(Complex[] a, Complex[] b) {
		// find the smallest power of two that will contain the resulting vector
		int n = a.length + b.length - 1;
		int exp = 1;
		while (Math.pow(2, exp) < n)
			exp++;
		int length = (int) Math.pow(2, exp);
		//System.out.println("--->"+length);
		a = pad(a, length);
		b = pad(b, length);
		//System.out.println("a--->"+a.length);
		//System.out.println("b--->"+b.length);

		//show(a, "a");
		a = fft(a, 0);
		b = fft(b, 0);

		// pairwise multiplication
		Complex[] c = new Complex[length];
		for (int i = 0; i < length; i++)
			c[i] = a[i].multiply(b[i]);

		return c;
	}

	public static boolean is2pow(int n){
		int result = ((n&(n-1))==0) ? (1) : (0);
		if(result==1){
			return true;
		}else {
			return false;
		}
	}

	// compute the linear convolution of x and y
	public static Complex[] product(Complex[] a, Complex[] b) {
		// find the smallest power of two that will contain the resulting vector
		int length = a.length;
		if(!is2pow(a.length)){
			int i=1;
			for(i=1;i<a.length;i=2*i){
			}
			length=i;
			a = pad(a, length);
		}




		b = pad(b, length);

		b = fft(b, 0);

		//System.out.println(a.length+":"+b.length+"->>>>>>"+length);
		// pairwise multiplication
		Complex[] c = new Complex[length];
		for (int i = 0; i < length; i++)
			c[i] = a[i].multiply(b[i]);

		//c=ifft(c);
		//c = removeExtra(c, n);

		return c;
	}

	public static Complex[] removeExtra(Complex[] vector, int size) {
		Complex[] newVector = new Complex[size];
		for (int i = 0; i < vector.length; i++)
			if (i < size)
				newVector[i] = vector[i];
		return newVector;
	}

	public static Complex[] convolveBasedOnIter(Complex[] a,Complex[] b){

		int n_a=a.length;
		int n_b=b.length;

		Complex result[]=new Complex[n_a+n_b-1];

		for(int i=0;i<n_a+n_b-1;++i){
			double sum=0.0;
			for(int j=0;j<=i;++j){
				sum +=((j<n_a)&&(i-j<n_b))?a[j].getReal()*b[i-j].getReal():0.0;
			}
			result[i]=new Complex(sum, 0);
		}
		return result;
	} 

	// display an array of Complex numbers to standard output
	public static void show(Complex[] x, String title) {
		System.out.println(title);
		System.out.println("-------------------");
		for (int i = 0; i < x.length; i++) {
			System.out.println(x[i]);
		}
		System.out.println();
	}

	public static int maxLength(Complex[] x) {
		for (int i = x.length-1; i >=0; i--) {
			if(x[i].getReal()!=0.0){
				return i;
			}
		}
		return x.length-1;
	}
	
	public static <V> void main(String[] args) {
		Complex[] a=new Complex[4];
		for(int i=0;i<2;i++){
			a[i]=new Complex(1);
		}
		for(int i=2;i<4;i++){
			a[i]=new Complex(0);
		}

		a=fft(a, 0);

		show(a, "a");

		a=pad(a, 8);
		a=fft(a, 0);
		show(a, "a");
	}
}
