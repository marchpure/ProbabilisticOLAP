package edu.pcube.statistics;


public class Cumulant {

	public Cumulant() {
		super();
	}
	
	public static double Permutation(int n,int k){
		//n*(n-1)*(n-2)...(n-k+1)
		
		if(k==0){
			return 1.0;
		}
		
		int nFactorial=1;
		for(int i=0;i<=k-1;i++){
			nFactorial=(n-i)*nFactorial;
		}
		
		//1*2*3...*k
		int kFactorial=1;
		for(int i=1;i<=k;i++){
			kFactorial=i*kFactorial;
		}
		
		return (double)nFactorial/(double)kFactorial;
	}
	
	public static double[] calucateCumulants(double rawMoments[],double centralMoments[]){
		int N=rawMoments.length-1;
		double cumulants[]=new double[N+1];
		
		cumulants[1]=rawMoments[1];
		cumulants[2]=centralMoments[2];
		cumulants[3]=centralMoments[3];
		
		for(int n=4;n<=N;n++){
			double sum=0.0;
			for(int m=1;m<n;m++){
				//System.out.println("n-1:"+(n-1)+" m-1:"+(m-1)+" Permutation:"+Permutation(n-1,m-1));
				sum+=Permutation(n-1,m-1)*cumulants[m]*rawMoments[n-m];
				//System.out.println("sum"+sum);
			}
			cumulants[n]=rawMoments[n]-sum;
		}
		//print(cumulants);
		return cumulants;
	}
	
	public static void print(double cumulants[]){
		for(int i=1;i<cumulants.length;i++){
			System.out.println(i+":"+cumulants[i]);
		}
	}
}
