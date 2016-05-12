package edu.pcube.statistics;

import org.apache.commons.math3.complex.Complex;

public class Moment {
	
	public Moment() {
		super();
	}
	
	public static double sum(Complex raw[],int pow){
		double sum=0.0;
		for(int i=0;i<raw.length;i++){
			sum+=raw[i].getReal()*Math.pow(i, pow);
		}
		return sum;
	}
	
	public static double sumRejectMean(Complex raw[],int pow,double expect){
		double sum=0.0;
		for(int i=0;i<raw.length;i++){
			sum+=Math.pow(i-expect, pow)*raw[i].getReal();
		}
		return sum;
	}
	
	public static double[] calculateRawMoments(Complex raw[],int N){
		double rawMoments[]=new double[N+1];
		for(int i=1;i<N+1;i++){
			rawMoments[i]=sum(raw,i);
		}
		
		//print(rawMoments);
	
		return rawMoments;
	}
	
	public static double[] calculateCentralMoments(Complex raw[],int N, double expect){
		double centralMoments[]=new double[N+1];
		for(int i=1;i<N+1;i++){
			centralMoments[i]=sumRejectMean(raw,i,expect);
		}
		return centralMoments;
	}
	
	public static void print(double moments[]){
		for(int i=1;i<moments.length;i++){
			System.out.println(i+"m:"+moments[i]);
		}
	}
	
	public static void main(String[] args){
		Complex a[]=new Complex[6];
		a[0]=new Complex(0.0, 0);
		a[1]=new Complex(0.3, 0);
		a[2]=new Complex(0.7, 0);
		a[3]=new Complex(0.0, 0);
		a[4]=new Complex(0.0, 0);
		a[5]=new Complex(0.0, 0);
		
		double resultRawMoments[];
		double resultCentralMoments[];
		resultRawMoments=Moment.calculateRawMoments(a, 4);
		
		Moment.print(resultRawMoments);
		
		resultCentralMoments=Moment.calculateCentralMoments(a, 4, resultRawMoments[1]);
		
		Moment.print(resultCentralMoments);
		
		double resultCumulant[]=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);
		
		Cumulant.print(resultCumulant);
		
		System.out.println(2.4*2.4*0.4+1.6*1.6*0.6);
	}
}
