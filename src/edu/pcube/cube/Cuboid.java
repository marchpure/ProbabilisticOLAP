package edu.pcube.cube;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.complex.Complex;

import edu.pcube.convolution.FFT;
import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.datastore.ObjectGroupedTuples;
import edu.pcube.statistics.Cumulant;
import edu.pcube.statistics.Moment;
import edu.pcube.util.ConvertBetweenStringArray;

public class Cuboid<V> implements Serializable{

	private static final long serialVersionUID = -5809782578272943999L;

	//cube元数据
	private Dimensions[] dimensions;
	private HashMap<String, HashMap<Object, Complex[]>> cells;  				
	private HashMap<String, Integer> cellsPmfLength;
	//private HashMap<String, ArrayList<StringBuffer>> cellsPWD;
	private HashMap<Object, Boolean> isTruncated;
	private HashMap<String, ArrayList<String>> cellsToSubCells;

	private HashMap<String, double[]> cellsPWD;

	//convolution的中间结果、最终结果
	private HashMap<String, HashMap<Object, Complex[]>> fftQuarantineZone;
	private HashMap<String, Complex[]> convolutionAggregationResult;	
	private HashMap<String, Complex[]> completePMFAggregationResult;

	//sketch的中间结果、最终结果
	private HashMap<String, HashMap<Object, ArrayList<double[]>>> sketchedQuarantineZone;
	private HashMap<String, double[]> sketchedCompleteAggregationResult;
	private HashMap<String, double[]> sketchedAggregationResult;

	//基于sketch计算avg的中间结果、最终结果
	private HashMap<String, double[]> avgStatistic;
	private HashMap<String,HashMap<Integer, Complex[]>> countSumJointPro;
	private HashMap<String, double[]> countStatistic;
	private HashMap<String, ArrayList<Double>> countPro;

	//===============================================================

	public Cuboid(Cube cube) {
		//cube元数据
		this.cells=new HashMap<String, HashMap<Object,Complex[]>>();
		this.cellsPmfLength=new HashMap<>();
		//this.cellsPWD=new HashMap<>();
		this.isTruncated=new HashMap<>();
		this.cellsToSubCells=new HashMap<String, ArrayList<String>>();

		//convolution的中间结果、最终结果
		this.fftQuarantineZone=new HashMap<String, HashMap<Object,Complex[]>>();
		this.convolutionAggregationResult=new HashMap<String, Complex[]>();
		this.completePMFAggregationResult=new HashMap<String, Complex[]>();

		//sketch的中间结果、最终结果
		this.sketchedQuarantineZone=new HashMap<String, HashMap<Object,ArrayList<double[]>>>();
		this.sketchedAggregationResult=new HashMap<String, double[]>();
		this.sketchedCompleteAggregationResult=new HashMap<>();

		//基于sketch计算avg的中间结果、最终结果
		this.avgStatistic=new HashMap<>();
		this.countSumJointPro=new HashMap<>();
		this.countPro=new HashMap<>();
		this.countStatistic=new HashMap<>();
	}


	public Cuboid(Cube cube, Dimensions[] dimensions) {
		//cube元数据
		this.dimensions=dimensions;
		this.cells=new HashMap<String, HashMap<Object,Complex[]>>();
		this.cellsPmfLength=new HashMap<>();
		//this.cellsPWD=new HashMap<>();
		this.isTruncated=new HashMap<>();
		this.cellsToSubCells=new HashMap<String, ArrayList<String>>();

		//convolution的中间结果、最终结果
		this.fftQuarantineZone=new HashMap<String, HashMap<Object,Complex[]>>();
		this.convolutionAggregationResult=new HashMap<String, Complex[]>();
		this.completePMFAggregationResult=new HashMap<String, Complex[]>();

		//sketch的中间结果、最终结果
		this.sketchedQuarantineZone=new HashMap<String, HashMap<Object,ArrayList<double[]>>>();
		this.sketchedAggregationResult=new HashMap<String, double[]>();
		this.sketchedCompleteAggregationResult=new HashMap<>();

		//基于sketch计算avg的中间结果、最终结果
		this.avgStatistic=new HashMap<>();
		this.countSumJointPro=new HashMap<>();
		this.countPro=new HashMap<>();
		this.countStatistic=new HashMap<>();
	}


	public HashMap<String, double[]> getSketchedCompleteAggregationResult() {
		return sketchedCompleteAggregationResult;
	}

	public double[] getSketchedCompleteAggregationResult(String cell) {
		return sketchedCompleteAggregationResult.get(cell);
	}

	//代修改
	/*public double getCountReciprocalVariance(String cell) {
		return this.countStatistic.get(cell)[2];
	}

	public double getEXT(String cell) {
		return this.countStatistic.get(cell)[3];
	}


	public double getCountReciprocalExpection(String cell) {
		return this.countStatistic.get(cell)[1];
	}

	public double getSumExpection(String cell) {
		return this.countStatistic.get(cell)[0];
	}*/

	public Dimensions[] getDimensions() {
		return this.dimensions;
	}

	public Dimensions getDimensions(int index) {
		return this.dimensions[index];
	}

	public HashMap<String, HashMap<Object, Complex[]>> getCells() {
		return cells;
	}

	public HashMap<Object, Complex[]> getCells(String cell) {
		return cells.get(cell);
	}

	public HashMap<String, ArrayList<String>> getCellsToSubCells() {
		return cellsToSubCells;
	}

	public HashMap<String, Complex[]> getConvolutionAggregationResult() {
		return convolutionAggregationResult;
	}

	public Complex[] getConvolutionAggregationResult(String key) {
		return convolutionAggregationResult.get(key);
	}

	public HashMap<String, Complex[]> getCompletePMFAggregationResult() {
		return completePMFAggregationResult;
	}

	public Complex[] getCompletePMFAggregationResult(String key) {
		return completePMFAggregationResult.get(key);
	}

	public HashMap<String, double[]> getSketchedAggregationResult() {
		return sketchedAggregationResult;
	}

	public Complex[] getCompletePMFAggregationResult(V[] key) {
		return this.completePMFAggregationResult.get(key);
	}

	public HashMap<Object, Complex[]> getFftQuarantineZone(String cell) {
		return fftQuarantineZone.get(cell);
	}

	public Complex[] getFftQuarantineZone(String cell,Object object) {
		return fftQuarantineZone.get(cell).get(object);
	}

	public HashMap<String, HashMap<Object, Complex[]>> getFftQuarantineZone() {
		return fftQuarantineZone;
	}

	public HashMap<String, HashMap<Object, ArrayList<double[]>>> getSketchedQuarantineZone() {
		return sketchedQuarantineZone;
	}

	public HashMap<Object, ArrayList<double[]>> getSketchedQuarantineZone(String cell) {
		return sketchedQuarantineZone.get(cell);
	}

	@SuppressWarnings("rawtypes")
	public int getCellCount(Cuboid cuboid){
		buildCellsBasedOnCuboid(cuboid);
		return this.cellsToSubCells.size();
	}

	public int getTruncateObjectCount(){
		int sum=0;
		for(String cellKey:this.fftQuarantineZone.keySet()){
			sum+=this.fftQuarantineZone.get(cellKey).size();
		}
		return sum;
	}

	@SuppressWarnings("rawtypes")
	public int getTruncateObjectCount(Cuboid cuboid){
		buildCellsBasedOnCuboid(cuboid);
		for(String cellKey:this.cellsToSubCells.keySet()){
			this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){
				@SuppressWarnings("unchecked")
				HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);
				if(objectPmfs.size()!=0){
					for(Object objectId:objectPmfs.keySet()){
						//Object objectId=objectPmfs.keySet().iterator().next();
						Complex[] pmfs=objectPmfs.get(objectId);
						if(!this.fftQuarantineZone.get(cellKey).containsKey(objectId)){
							this.fftQuarantineZone.get(cellKey).put(objectId, pmfs);
						}else {
							Complex[] resultCombine=combineComplex(this.fftQuarantineZone.get(cellKey).get(objectId), pmfs);
							this.fftQuarantineZone.get(cellKey).put(objectId, resultCombine);
						}
					}
				}
			}
		}
		int sum=0;
		for(String cellKey:this.fftQuarantineZone.keySet()){
			for(Object objectId:this.fftQuarantineZone.get(cellKey).keySet()){
				if(isTruncated(objectId)){
					sum++;
				}
			}
		}
		return sum;
	}




	private boolean isTruncated(Object object) {
		return this.isTruncated.get(object);
	}


	public void printCells(){
		System.out.println("+++++++++ Cells+++++++++"+this.cells.size());
		for(String key:this.cellsToSubCells.keySet()){
			System.out.println(key);
			System.out.println();
		}
	}

	public File buildBasedOnFFT(InMemoryDataStore<V> inMemoryDataStore) {
		//build cells
		buildCells(inMemoryDataStore.getObjectGroupedTuples());

		//convolution in per cell
		for(String cell:cells.keySet()){

			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			Complex resultCompleteObjects[] = new Complex[2];
			resultCompleteObjects[0]=new Complex(1.0, 0.0);	resultCompleteObjects[1]=new Complex(0.0, 0.0);

			HashMap<Object,Complex[]> objectPmfs=cells.get(cell);

			this.fftQuarantineZone.put(cell, new HashMap<Object, Complex[]>());

			int i=0;    //用来代表FFT的次数
			int max=0;    //代表FFT之后的结果长度
			int maxComplete=0;   
			boolean flagComplete=false;    //标记是否存在truncate object
			for(Object objectId:objectPmfs.keySet()){
				Complex[] zeroPaddedPmfs=objectPmfs.get(objectId);
				if(inMemoryDataStore.getObjectGroupedTuples().get(objectId).isTruncated()){
					this.fftQuarantineZone.get(cell).put(objectId, objectPmfs.get(objectId));
					zeroPaddedPmfs=zeroPadded(objectPmfs.get(objectId));
				}else {
					if(!flagComplete){
						maxComplete=resultCompleteObjects.length+zeroPaddedPmfs.length-1;
						resultCompleteObjects=FFT.convolution(resultCompleteObjects, zeroPaddedPmfs);
					}else {
						if(resultCompleteObjects.length<=(maxComplete+zeroPaddedPmfs.length-1)){
							resultCompleteObjects=FFT.ifft(resultCompleteObjects);
							resultCompleteObjects=FFT.removeExtra(resultCompleteObjects, maxComplete);
							maxComplete=resultCompleteObjects.length+zeroPaddedPmfs.length-1;
							resultCompleteObjects=FFT.convolution(resultCompleteObjects, zeroPaddedPmfs);
						}else {
							maxComplete=maxComplete+zeroPaddedPmfs.length-1;
							resultCompleteObjects=FFT.product(resultCompleteObjects, zeroPaddedPmfs);
						}
					}
					flagComplete=true;
				}

				if(i==0){
					max=result.length+zeroPaddedPmfs.length-1;
					result=FFT.convolution(result, zeroPaddedPmfs);
				}else {
					if(result.length<=(max+zeroPaddedPmfs.length-1)){
						result=FFT.ifft(result);
						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+zeroPaddedPmfs.length-1;
						result=FFT.convolution(result, zeroPaddedPmfs);
					}else {
						max=max+zeroPaddedPmfs.length-1;
						result=FFT.product(result, zeroPaddedPmfs);
					}
				}
				i++;
			}
			if(flagComplete){
				resultCompleteObjects=FFT.ifft(resultCompleteObjects);
				resultCompleteObjects=FFT.removeExtra(resultCompleteObjects, maxComplete);	
			}

			result=FFT.ifft(result);
			result=FFT.removeExtra(result, max);
			this.convolutionAggregationResult.put(cell, result);
			this.completePMFAggregationResult.put(cell, resultCompleteObjects);

		}
		//object Serializable 		
		return this.refreshFile();
	}

	//但是这个应该是用PWD的方法，但是这个函数还没有改过来
	public File buildBasedOnFFTnonH(InMemoryDataStore<V> inMemoryDataStore) {
		//build cells
		buildCells(inMemoryDataStore.getObjectGroupedTuples());

		//convolution in per cell
		for(String cell:cells.keySet()){

			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			Complex resultCompleteObjects[] = new Complex[2];
			resultCompleteObjects[0]=new Complex(1.0, 0.0);	resultCompleteObjects[1]=new Complex(0.0, 0.0);

			HashMap<Object,Complex[]> objectPmfs=cells.get(cell);

			this.fftQuarantineZone.put(cell, new HashMap<Object, Complex[]>());

			int i=0;    //用来代表FFT的次数
			int max=0;    //代表FFT之后的结果长度
			int maxComplete=0;   
			boolean flagComplete=false;    //标记是否存在truncate object
			for(Object objectId:objectPmfs.keySet()){
				Complex[] zeroPaddedPmfs=objectPmfs.get(objectId);

				if(i==0){
					max=result.length+zeroPaddedPmfs.length-1;
					result=FFT.convolution(result, zeroPaddedPmfs);
				}else {
					if(result.length<=(max+zeroPaddedPmfs.length-1)){
						result=FFT.ifft(result);
						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+zeroPaddedPmfs.length-1;
						result=FFT.convolution(result, zeroPaddedPmfs);
					}else {
						max=max+zeroPaddedPmfs.length-1;
						result=FFT.product(result, zeroPaddedPmfs);
					}
				}
				i++;
			}
			if(flagComplete){
				resultCompleteObjects=FFT.ifft(resultCompleteObjects);
				resultCompleteObjects=FFT.removeExtra(resultCompleteObjects, maxComplete);	
			}

			result=FFT.ifft(result);
			result=FFT.removeExtra(result, max);
			this.convolutionAggregationResult.put(cell, result);
			this.completePMFAggregationResult.put(cell, resultCompleteObjects);
		}
		return this.refreshFile();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File buildAvgBasedOnFFT(InMemoryDataStore<V> inMemoryDataStore) {
		//build cells
		buildCells(inMemoryDataStore.getObjectGroupedTuples());

		//convolution in per cell
		for(String cell:cells.keySet()){
			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			Complex resultCompleteObjects[] = new Complex[2];
			resultCompleteObjects[0]=new Complex(1.0, 0.0);	resultCompleteObjects[1]=new Complex(0.0, 0.0);

			HashMap<Object,Complex[]> objectPmfs=cells.get(cell);

			this.fftQuarantineZone.put(cell, new HashMap<Object, Complex[]>());
			this.countPro.put(cell, new ArrayList());
			this.countPro.get(cell).add(1.0);

			int i=0;    //用来代表FFT的次数
			int max=0;    //代表FFT之后的结果长度
			int maxComplete=0;   
			boolean flagComplete=false;    //标记是否存在truncate object
			for(Object objectId:objectPmfs.keySet()){
				Complex[] zeroPaddedPmfs=objectPmfs.get(objectId);
				if(inMemoryDataStore.getObjectGroupedTuples().get(objectId).isTruncated()){
					this.fftQuarantineZone.get(cell).put(objectId, objectPmfs.get(objectId));
					zeroPaddedPmfs=zeroPadded(objectPmfs.get(objectId));
				}else {
					if(!flagComplete){
						maxComplete=resultCompleteObjects.length+zeroPaddedPmfs.length-1;
						resultCompleteObjects=FFT.convolution(resultCompleteObjects, zeroPaddedPmfs);
					}else {
						if(resultCompleteObjects.length<=(maxComplete+zeroPaddedPmfs.length-1)){
							resultCompleteObjects=FFT.ifft(resultCompleteObjects);
							resultCompleteObjects=FFT.removeExtra(resultCompleteObjects, maxComplete);
							maxComplete=resultCompleteObjects.length+zeroPaddedPmfs.length-1;
							resultCompleteObjects=FFT.convolution(resultCompleteObjects, zeroPaddedPmfs);
						}else {
							maxComplete=maxComplete+zeroPaddedPmfs.length-1;
							resultCompleteObjects=FFT.product(resultCompleteObjects, zeroPaddedPmfs);
						}
					}
					flagComplete=true;
				}

				//统计count的PMF，待定，看是否需要删掉
				double truncatedPro=zeroPaddedPmfs[0].getReal();
				double nonTruncatedPro=1-truncatedPro;

				ArrayList<Double> oldCountProPerCell=this.countPro.get(cell);
				ArrayList<Double> newCountProPerCell=new ArrayList<>(oldCountProPerCell.size()+1);

				for(int index=0;index<oldCountProPerCell.size()+1;index++){
					newCountProPerCell.add(0.0);
				}

				newCountProPerCell.set(oldCountProPerCell.size(), oldCountProPerCell.get(oldCountProPerCell.size()-1)*nonTruncatedPro);

				for(int count=oldCountProPerCell.size()-1;count>=1;count--){
					newCountProPerCell.set(count, oldCountProPerCell.get(count-1)*nonTruncatedPro+oldCountProPerCell.get(count)*truncatedPro);
				}
				newCountProPerCell.set(0, oldCountProPerCell.get(0)*truncatedPro);
				this.countPro.put(cell, newCountProPerCell);
				//待定，看是否要删掉


				if(i==0){
					max=result.length+zeroPaddedPmfs.length-1;
					result=FFT.convolution(result, zeroPaddedPmfs);
				}else {
					if(result.length<=(max+zeroPaddedPmfs.length-1)){
						result=FFT.ifft(result);
						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+zeroPaddedPmfs.length-1;
						result=FFT.convolution(result, zeroPaddedPmfs);
					}else {
						max=max+zeroPaddedPmfs.length-1;
						result=FFT.product(result, zeroPaddedPmfs);
					}
				}
				i++;
			}
			if(flagComplete){
				resultCompleteObjects=FFT.ifft(resultCompleteObjects);
				resultCompleteObjects=FFT.removeExtra(resultCompleteObjects, maxComplete);	
			}

			result=FFT.ifft(result);
			result=FFT.removeExtra(result, max);
			this.convolutionAggregationResult.put(cell, result);
			this.completePMFAggregationResult.put(cell, resultCompleteObjects);

			HashMap<Integer, Complex[]> cellValues=new HashMap<>();
			cellValues.put(objectPmfs.size(), result);

			this.countSumJointPro.put(cell, cellValues);
		}

		//this.printCountSumJointPro();
		this.buildAvgBySketched();
		return this.refreshFile();
	}


	public void buildAvgBySketched(){

		for(String cell:cells.keySet()){
			double avgExp=0.0;
			double productExp=0.0;
			double avgVar=0.0;

			HashMap<Integer, Complex[]> countSumJointPerCell=countSumJointPro.get(cell);

			for(Iterator<Integer> countKeyItertor=countSumJointPerCell.keySet().iterator();countKeyItertor.hasNext();){
				int countKey=countKeyItertor.next();
				if(countKey==0){
					continue;
				}

				Complex resultComplex[]=countSumJointPerCell.get(countKey);

				for(int i=0;i<resultComplex.length;i++){
					int sum=i;
					double pro=resultComplex[i].getReal();
					avgExp+=((double)sum/(double)countKey)*pro;
					productExp+=((double)sum*(double)countKey)*pro;
				}
			}

			for(Iterator<Integer> countKeyItertor=countSumJointPerCell.keySet().iterator();countKeyItertor.hasNext();){
				int countKey=countKeyItertor.next();
				if(countKey==0){
					Complex resultComplex[]=countSumJointPerCell.get(countKey);
					for(int i=0;i<resultComplex.length;i++){
						double pro=resultComplex[i].getReal();
						double avg=0;
						avgVar+=(avg-avgExp)*(avg-avgExp)*pro;
					}

					continue;
				}

				Complex resultComplex[]=countSumJointPerCell.get(countKey);
				for(int i=0;i<resultComplex.length;i++){
					int sum=i;
					double pro=resultComplex[i].getReal();
					double avg=(double)sum/(double)countKey;
					avgVar+=(avg-avgExp)*(avg-avgExp)*pro;
				}
			}

			double expection[]=new double[3];
			expection[0]=avgExp;
			expection[1]=productExp;
			expection[2]=avgVar;

			this.avgStatistic.put(cell, expection);
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File buildAvgBasedOnCuboid(Cuboid cuboid) throws Exception{

		//首先判断cuboid是否是祖先
		//根据cuboid的cells，建立自己的cells
		if(this.cells.isEmpty()){
			buildCellsBasedOnCuboid(cuboid);
		}

		//Step1. 对于每个cell，融合每个sub-cell的隔离区	

		for(String cellKey:this.cellsToSubCells.keySet()){

			int completeObjetcNum=0;
			int truncatedObjectNum=0;
			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			boolean flag=false;
			int max=0;
			this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){
				HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);

				if(objectPmfs.size()!=0){
					for(Object objectId:objectPmfs.keySet()){
						Complex[] pmfs=objectPmfs.get(objectId);
						if(!this.fftQuarantineZone.get(cellKey).containsKey(objectId)){
							this.fftQuarantineZone.get(cellKey).put(objectId, pmfs);
						}else {
							Complex[] resultCombine=combineComplex(this.fftQuarantineZone.get(cellKey).get(objectId), pmfs);
							this.fftQuarantineZone.get(cellKey).put(objectId, resultCombine);
						}
					}
				}

				Complex[] pmfSubCell=cuboid.getCompletePMFAggregationResult(subCellKey);
				completeObjetcNum+=((HashMap<Object, Complex[]>) cuboid.getCells().get(subCellKey)).size();
				truncatedObjectNum+=cuboid.getFftQuarantineZone(subCellKey).size();

				if(!flag){
					max=result.length+pmfSubCell.length-1;
					result=FFT.convolution(result, pmfSubCell);
					flag=true;
				}else {
					if(result.length<=(max+pmfSubCell.length-1)){
						result=FFT.ifft(result);
						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+pmfSubCell.length-1;
						result=FFT.convolution(result, pmfSubCell);
					}else {
						max=max+pmfSubCell.length-1;
						result=FFT.product(result, pmfSubCell);
					}
				}
			}
			if(flag){
				result=FFT.ifft(result);
				if(result.length>max){
					result=FFT.removeExtra(result, max);
				}
			}

			this.completePMFAggregationResult.put(cellKey, result);
			HashMap<Integer, Complex[]> cellValues=new HashMap<>();
			cellValues.put(completeObjetcNum-truncatedObjectNum, result);
			this.countSumJointPro.put(cellKey, cellValues);

			if(cellKey.equals("Manufacturer#2|1992|AMERICA")){
				FFT.show(result, "result");
			}
		}

		//Step2. 对于quarantine zone中的complete object 进行convolution
		int maxComplete=0;

		if(this.fftQuarantineZone.size()==0){
			for(String cellKey:this.completePMFAggregationResult.keySet()){
				this.convolutionAggregationResult.put(cellKey, this.completePMFAggregationResult.get(cellKey));
			}
		}

		for(String cellKey:this.fftQuarantineZone.keySet()){

			boolean flagComplete=false;
			Complex[] resultComplete=this.completePMFAggregationResult.get(cellKey);


			int completeObjectCount=0;

			if(cellKey.equals("Manufacturer#2|1992|AMERICA")){
				System.out.println("aa");
			}

			for(Iterator<Object> iterator=this.fftQuarantineZone.get(cellKey).keySet().iterator();iterator.hasNext();){

				Object objectId=iterator.next();
				Complex[] pmfs=this.getFftQuarantineZone(cellKey, objectId);
				if(isTruncated(objectId)){

				}else {
					completeObjectCount++;
					iterator.remove();
					if(!flagComplete){
						maxComplete=resultComplete.length+pmfs.length-1;
						resultComplete=FFT.convolution(resultComplete, pmfs);
					}else {
						if(resultComplete.length<=(maxComplete+pmfs.length-1)){
							resultComplete=FFT.ifft(resultComplete);
							if(resultComplete.length>maxComplete){
								resultComplete=FFT.removeExtra(resultComplete, maxComplete);
							}
							maxComplete=resultComplete.length+pmfs.length-1;
							resultComplete=FFT.convolution(resultComplete, pmfs);
						}else {
							maxComplete=maxComplete+pmfs.length-1;
							resultComplete=FFT.product(resultComplete, pmfs);
						}
					}
					flagComplete=true;
				}
			}

			if(flagComplete){
				resultComplete=FFT.ifft(resultComplete);
				if(resultComplete.length>maxComplete){
					resultComplete=FFT.removeExtra(resultComplete, maxComplete);
				}
			}
			this.completePMFAggregationResult.put(cellKey, resultComplete);

			int oldCountKey=this.countSumJointPro.get(cellKey).keySet().iterator().next();
			int newCountKey=oldCountKey+completeObjectCount;

			this.countSumJointPro.get(cellKey).remove(oldCountKey);
			this.countSumJointPro.get(cellKey).put(newCountKey, resultComplete);

		}

		//Step3. 和truncated object进行convlution

		for(String cellKey:this.fftQuarantineZone.keySet()){

			if(cellKey.equals("Manufacturer#2|1992|AMERICA")){
				System.out.println("aa");
			}

			Complex[] result=null;
			for(Iterator<Object> iterator=this.fftQuarantineZone.get(cellKey).keySet().iterator();iterator.hasNext();){

				Object objectId=iterator.next();
				if(isTruncated(objectId)){
					Complex[] pmfs=this.getFftQuarantineZone(cellKey, objectId);
					HashMap<Integer, Complex[]> resultMapTrue=this.countSumJointPro.get(cellKey);
					HashMap<Integer, Complex[]> resultMapTemp=new HashMap<>();

					for(int countKey:resultMapTrue.keySet()){
						result=resultMapTrue.get(countKey);

						int max=result.length+pmfs.length-1;
						result=FFT.convolution(result, pmfs);
						result=FFT.ifft(result);
						result=FFT.removeExtra(result, max);

						resultMapTemp.put(countKey+1, result);
					}

					double zeroPro=zeroPro(pmfs);

					for(int countKey:resultMapTrue.keySet()){
						result=resultMapTrue.get(countKey);

						for(int k=0;k<result.length;k++){
							result[k]=result[k].multiply(zeroPro);
						}

						if(resultMapTemp.containsKey(countKey)){
							resultMapTemp.put(countKey,combineComplex(resultMapTemp.get(countKey), result));
						}else {
							resultMapTemp.put(countKey,result);
						}
					}

					this.countSumJointPro.get(cellKey).clear();
					this.countSumJointPro.get(cellKey).putAll(resultMapTemp);

				}
			}
		}

		this.buildAvgBySketched();

		//object Serializable 		
		File file=this.refreshFile();
		return file;	
	}

	public Complex[] buildSingleCellBasedOnCuboid(Cuboid cuboid,String cellKey) throws Exception{
		ArrayList<Long> r=new ArrayList<>();

		//首先判断cuboid是否是祖先
		//根据cuboid的cells，建立自己的cells
		buildCellsBasedOnCuboid(cuboid);

		int i=0;

		//对于每个cell，融合每个sub-cell的隔离区	

		int subcellnum=0;

		Complex result[] = new Complex[2];
		result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

		boolean flag=false;
		int max=0;
		this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
		for(String subCellKey:cellsToSubCells.get(cellKey)){

			HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);

			if(objectPmfs.size()!=0){
				for(Object objectId:objectPmfs.keySet()){
					Complex[] pmfs=objectPmfs.get(objectId);
					if(!this.fftQuarantineZone.get(cellKey).containsKey(objectId)){
						this.fftQuarantineZone.get(cellKey).put(objectId, pmfs);
					}else {
						Complex[] resultCombine=combineComplex(this.fftQuarantineZone.get(cellKey).get(objectId), pmfs);
						this.fftQuarantineZone.get(cellKey).put(objectId, resultCombine);
					}
				}
			}

			Complex[] pmfSubCell=cuboid.getCompletePMFAggregationResult(subCellKey);

			if(!flag){
				max=result.length+pmfSubCell.length-1;
				result=FFT.convolution(result, pmfSubCell);
				flag=true;
			}else {
				if(result.length<=(max+pmfSubCell.length-1)){

					result=FFT.ifft(result);

					if(result.length>max){
						result=FFT.removeExtra(result, max);
					}
					max=result.length+pmfSubCell.length-1;
					result=FFT.convolution(result, pmfSubCell);
				}else {
					max=max+pmfSubCell.length-1;
					result=FFT.product(result, pmfSubCell);
				}
			}
		}
		if(flag){
			result=FFT.ifft(result);
			if(result.length>max){
				result=FFT.removeExtra(result, max);
			}
		}
		this.completePMFAggregationResult.put(cellKey, result);

		int maxall=0;
		int maxComplete=0;
		i=0;
		boolean flagComplete=false;
		Complex[] resultComplete=this.completePMFAggregationResult.get(cellKey);
		result=resultComplete;

		for(Iterator<Object> iterator=this.fftQuarantineZone.get(cellKey).keySet().iterator();iterator.hasNext();){

			Object objectId=iterator.next();
			Complex[] pmfs=this.getFftQuarantineZone(cellKey, objectId);

			if(isTruncated(objectId)){
				pmfs=zeroPadded(pmfs);
			}else {

				iterator.remove();
				if(!flagComplete){
					maxComplete=resultComplete.length+pmfs.length-1;
					resultComplete=FFT.convolution(resultComplete, pmfs);
				}else {
					if(resultComplete.length<=(maxComplete+pmfs.length-1)){
						resultComplete=FFT.ifft(resultComplete);

						if(resultComplete.length>maxComplete){
							resultComplete=FFT.removeExtra(resultComplete, maxComplete);
						}
						maxComplete=resultComplete.length+pmfs.length-1;

						resultComplete=FFT.convolution(resultComplete, pmfs);
					}else {
						maxComplete=maxComplete+pmfs.length-1;
						resultComplete=FFT.product(resultComplete, pmfs);
					}
				}
				flagComplete=true;
			}

			if(i==0){

				maxall=result.length+pmfs.length-1;
				result=FFT.convolution(result, pmfs);
			}else {

				if(result.length<=(maxall+pmfs.length-1)){

					result=FFT.ifft(result);
					if(result.length>maxall){
						result=FFT.removeExtra(result, maxall);
					}
					maxall=result.length+pmfs.length-1;

					result=FFT.convolution(result, pmfs);
				}else {
					maxall=maxall+pmfs.length-1;
					result=FFT.product(result, pmfs);
				}
			}
			i++;
		}

		if(flagComplete){
			resultComplete=FFT.ifft(resultComplete);
			if(resultComplete.length>maxComplete){
				resultComplete=FFT.removeExtra(resultComplete, maxComplete);
			}
		}
		this.completePMFAggregationResult.put(cellKey, resultComplete);

		if(maxall==0){
			this.convolutionAggregationResult.put(cellKey, resultComplete);
		}else {
			result=FFT.ifft(result);
			if(result.length>maxall){
				result=FFT.removeExtra(result, maxall);
			}
			this.convolutionAggregationResult.put(cellKey, result);
		}

		cellsPmfLength.clear();	
		return result;
	}

	public File buildBasedOnCuboid(Cuboid cuboid) throws Exception{
		ArrayList<Long> r=new ArrayList<>();

		//首先判断cuboid是否是祖先
		//根据cuboid的cells，建立自己的cells
		buildCellsBasedOnCuboid(cuboid);


		int i=0;

		//对于每个cell，融合每个sub-cell的隔离区	

		int subcellnum=0;
		for(String cellKey:this.cellsToSubCells.keySet()){
			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			boolean flag=false;
			int max=0;
			this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){

				HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);

				if(objectPmfs.size()!=0){
					for(Object objectId:objectPmfs.keySet()){
						Complex[] pmfs=objectPmfs.get(objectId);
						if(!this.fftQuarantineZone.get(cellKey).containsKey(objectId)){
							this.fftQuarantineZone.get(cellKey).put(objectId, pmfs);
						}else {
							Complex[] resultCombine=combineComplex(this.fftQuarantineZone.get(cellKey).get(objectId), pmfs);
							this.fftQuarantineZone.get(cellKey).put(objectId, resultCombine);
						}
					}
				}

				Complex[] pmfSubCell=cuboid.getCompletePMFAggregationResult(subCellKey);

				if(!flag){
					max=result.length+pmfSubCell.length-1;
					result=FFT.convolution(result, pmfSubCell);
					flag=true;
				}else {
					if(result.length<=(max+pmfSubCell.length-1)){

						result=FFT.ifft(result);

						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+pmfSubCell.length-1;
						result=FFT.convolution(result, pmfSubCell);
					}else {
						max=max+pmfSubCell.length-1;
						result=FFT.product(result, pmfSubCell);
					}
				}
			}
			if(flag){
				result=FFT.ifft(result);
				if(result.length>max){
					result=FFT.removeExtra(result, max);
				}
			}
			this.completePMFAggregationResult.put(cellKey, result);

		}

		if(this.fftQuarantineZone.size()==0){
			for(String cellKey:this.completePMFAggregationResult.keySet()){
				this.convolutionAggregationResult.put(cellKey, this.completePMFAggregationResult.get(cellKey));
			}
		}
		for(String cellKey:this.fftQuarantineZone.keySet()){

			int maxall=0;
			int maxComplete=0;
			i=0;
			boolean flagComplete=false;
			Complex[] resultComplete=this.completePMFAggregationResult.get(cellKey);
			Complex[] result=resultComplete;

			for(Iterator<Object> iterator=this.fftQuarantineZone.get(cellKey).keySet().iterator();iterator.hasNext();){

				Object objectId=iterator.next();
				Complex[] pmfs=this.getFftQuarantineZone(cellKey, objectId);

				if(isTruncated(objectId)){
					pmfs=zeroPadded(pmfs);
				}else {

					iterator.remove();
					if(!flagComplete){
						maxComplete=resultComplete.length+pmfs.length-1;
						resultComplete=FFT.convolution(resultComplete, pmfs);
					}else {
						if(resultComplete.length<=(maxComplete+pmfs.length-1)){
							resultComplete=FFT.ifft(resultComplete);

							if(resultComplete.length>maxComplete){
								resultComplete=FFT.removeExtra(resultComplete, maxComplete);
							}
							maxComplete=resultComplete.length+pmfs.length-1;

							resultComplete=FFT.convolution(resultComplete, pmfs);
						}else {
							maxComplete=maxComplete+pmfs.length-1;
							resultComplete=FFT.product(resultComplete, pmfs);
						}
					}
					flagComplete=true;
				}

				if(i==0){

					maxall=result.length+pmfs.length-1;
					result=FFT.convolution(result, pmfs);
				}else {

					if(result.length<=(maxall+pmfs.length-1)){

						result=FFT.ifft(result);
						if(result.length>maxall){
							result=FFT.removeExtra(result, maxall);
						}
						maxall=result.length+pmfs.length-1;

						result=FFT.convolution(result, pmfs);
					}else {
						maxall=maxall+pmfs.length-1;
						result=FFT.product(result, pmfs);
					}
				}
				i++;
			}

			if(flagComplete){
				resultComplete=FFT.ifft(resultComplete);
				if(resultComplete.length>maxComplete){
					resultComplete=FFT.removeExtra(resultComplete, maxComplete);
				}
			}
			this.completePMFAggregationResult.put(cellKey, resultComplete);

			if(maxall==0){
				this.convolutionAggregationResult.put(cellKey, resultComplete);
			}else {
				result=FFT.ifft(result);
				if(result.length>maxall){
					result=FFT.removeExtra(result, maxall);
				}
				this.convolutionAggregationResult.put(cellKey, result);
			}
		}

		cellsPmfLength.clear();

		//Cuboid Serializable 		
		File file=this.refreshFile();	
		return file;	
	}

	public void buildBasedOnCuboidWithoutPersist(Cuboid cuboid) throws Exception{
		ArrayList<Long> r=new ArrayList<>();

		//首先判断cuboid是否是祖先
		//根据cuboid的cells，建立自己的cells
		buildCellsBasedOnCuboid(cuboid);


		int i=0;

		//对于每个cell，融合每个sub-cell的隔离区	

		int subcellnum=0;
		for(String cellKey:this.cellsToSubCells.keySet()){
			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			boolean flag=false;
			int max=0;
			this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){

				HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);

				if(objectPmfs.size()!=0){
					for(Object objectId:objectPmfs.keySet()){
						Complex[] pmfs=objectPmfs.get(objectId);
						if(!this.fftQuarantineZone.get(cellKey).containsKey(objectId)){
							this.fftQuarantineZone.get(cellKey).put(objectId, pmfs);
						}else {
							Complex[] resultCombine=combineComplex(this.fftQuarantineZone.get(cellKey).get(objectId), pmfs);
							this.fftQuarantineZone.get(cellKey).put(objectId, resultCombine);
						}
					}
				}

				Complex[] pmfSubCell=cuboid.getCompletePMFAggregationResult(subCellKey);

				if(!flag){
					max=result.length+pmfSubCell.length-1;
					result=FFT.convolution(result, pmfSubCell);
					flag=true;
				}else {
					if(result.length<=(max+pmfSubCell.length-1)){

						result=FFT.ifft(result);

						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+pmfSubCell.length-1;
						result=FFT.convolution(result, pmfSubCell);
					}else {
						max=max+pmfSubCell.length-1;
						result=FFT.product(result, pmfSubCell);
					}
				}
			}
			if(flag){
				result=FFT.ifft(result);
				if(result.length>max){
					result=FFT.removeExtra(result, max);
				}
			}
			this.completePMFAggregationResult.put(cellKey, result);

		}

		if(this.fftQuarantineZone.size()==0){
			for(String cellKey:this.completePMFAggregationResult.keySet()){
				this.convolutionAggregationResult.put(cellKey, this.completePMFAggregationResult.get(cellKey));
			}
		}
		for(String cellKey:this.fftQuarantineZone.keySet()){

			int maxall=0;
			int maxComplete=0;
			i=0;
			boolean flagComplete=false;
			Complex[] resultComplete=this.completePMFAggregationResult.get(cellKey);
			Complex[] result=resultComplete;

			for(Iterator<Object> iterator=this.fftQuarantineZone.get(cellKey).keySet().iterator();iterator.hasNext();){

				Object objectId=iterator.next();
				Complex[] pmfs=this.getFftQuarantineZone(cellKey, objectId);

				if(isTruncated(objectId)){
					pmfs=zeroPadded(pmfs);
				}else {

					iterator.remove();
					if(!flagComplete){
						maxComplete=resultComplete.length+pmfs.length-1;
						resultComplete=FFT.convolution(resultComplete, pmfs);
					}else {
						if(resultComplete.length<=(maxComplete+pmfs.length-1)){
							resultComplete=FFT.ifft(resultComplete);

							if(resultComplete.length>maxComplete){
								resultComplete=FFT.removeExtra(resultComplete, maxComplete);
							}
							maxComplete=resultComplete.length+pmfs.length-1;

							resultComplete=FFT.convolution(resultComplete, pmfs);
						}else {
							maxComplete=maxComplete+pmfs.length-1;
							resultComplete=FFT.product(resultComplete, pmfs);
						}
					}
					flagComplete=true;
				}

				if(i==0){

					maxall=result.length+pmfs.length-1;
					result=FFT.convolution(result, pmfs);
				}else {

					if(result.length<=(maxall+pmfs.length-1)){

						result=FFT.ifft(result);
						if(result.length>maxall){
							result=FFT.removeExtra(result, maxall);
						}
						maxall=result.length+pmfs.length-1;

						result=FFT.convolution(result, pmfs);
					}else {
						maxall=maxall+pmfs.length-1;
						result=FFT.product(result, pmfs);
					}
				}
				i++;
			}

			if(flagComplete){
				resultComplete=FFT.ifft(resultComplete);
				if(resultComplete.length>maxComplete){
					resultComplete=FFT.removeExtra(resultComplete, maxComplete);
				}
			}
			this.completePMFAggregationResult.put(cellKey, resultComplete);

			if(maxall==0){
				this.convolutionAggregationResult.put(cellKey, resultComplete);
			}else {
				result=FFT.ifft(result);
				if(result.length>maxall){
					result=FFT.removeExtra(result, maxall);
				}
				this.convolutionAggregationResult.put(cellKey, result);
			}
		}

		cellsPmfLength.clear();
	}

	public File buildBasedOnSketch(InMemoryDataStore<V> inMemoryDataStore) {
		//build cells
		//System.out.println(this.cells.size());
		if(this.cells.size()==0){
			buildCells(inMemoryDataStore.getObjectGroupedTuples());
		}

		int N=4;//n-th order cumulant\moment
		int arrayLenth=N+1;

		for(String cell:cells.keySet()){

			double sumEx=0.0;

			double totalCumulant[]=new double[arrayLenth];
			double totalRawMoment[]=new double[arrayLenth];

			this.sketchedQuarantineZone.put(cell, new HashMap<>());
			HashMap<Object,Complex[]> objectPmfs=cells.get(cell);
			for(Object objectId:objectPmfs.keySet()){

				Complex[] pmfs=objectPmfs.get(objectId);

				pmfs=zeroPadded(pmfs);

				double resultRawMoments[]=new double[arrayLenth];
				double resultCentralMoments[]=new double[arrayLenth];
				resultRawMoments=Moment.calculateRawMoments(pmfs, N);

				sumEx+=resultRawMoments[1];

				resultCentralMoments=Moment.calculateCentralMoments(pmfs,3,resultRawMoments[1]);
				this.sketchedQuarantineZone.get(cell).put(objectId, new ArrayList<double[]>());
				this.sketchedQuarantineZone.get(cell).get(objectId).add(resultRawMoments);
				this.sketchedQuarantineZone.get(cell).get(objectId).add(resultCentralMoments);

				double resultCumulants[]=new double[arrayLenth];
				resultCumulants=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);

				totalCumulant=combine(totalCumulant, resultCumulants);
				totalRawMoment=combine(totalRawMoment, resultRawMoments);
			}

			if(!this.countStatistic.containsKey(cell)){
				double[] statistics=new double[4];
				statistics[0]=sumEx;
				statistics[1]=statistics[2]=statistics[3]=0.0;

				this.countStatistic.put(cell, statistics);
			}else {
				double[] statistics=countStatistic.get(cell).clone();
				statistics[0]=sumEx;
				this.countStatistic.put(cell, statistics);
			}

			System.out.println("sumEx:"+sumEx);
			sketchedAggregationResult.put(cell, totalRawMoment);
		}

		return this.refreshFile();
	}

	public File buildBasedOnSketchReturnCountReciprocalExpection(InMemoryDataStore<V> inMemoryDataStore) {
		//build cells
		if(this.cells.size()==0){
			buildCells(inMemoryDataStore.getObjectGroupedTuples());
		}

		int N=4;//n-th order cumulant\moment
		int arrayLenth=N+1;

		for(String cell:cells.keySet()){
			//System.out.println("============"+cell+"================");
			double totalCumulant[]=new double[arrayLenth];
			double totalCompleteCumulant[]=new double[arrayLenth];

			//double totalRawMoment[]=new double[arrayLenth];

			this.sketchedQuarantineZone.put(cell, new HashMap<>());
			HashMap<Object,Complex[]> objectPmfs=cells.get(cell);

			double eX=0.0;
			double varX=0.0;

			double pCountEqualZeroPerCell=1.0;

			for(Object objectId:objectPmfs.keySet()){

				Complex[] pmfs=objectPmfs.get(objectId);

				pmfs=zeroPadded(pmfs);

				double zeroPadded=zeroPro(pmfs);

				if(zeroPadded<=0.000000001){
					pCountEqualZeroPerCell=0.0;
				}else {
					pCountEqualZeroPerCell*=zeroPadded;
				}

				double resultRawMoments[]=new double[arrayLenth];
				double resultCentralMoments[]=new double[arrayLenth];
				//FFT.show(pmfs, "pmfs");
				resultRawMoments=Moment.calculateRawMoments(pmfs, N);

				eX+=resultRawMoments[1];

				resultCentralMoments=Moment.calculateCentralMoments(pmfs, 3,resultRawMoments[1]);

				if(inMemoryDataStore.getObjectGroupedTuples().get(objectId).isTruncated()){
					this.sketchedQuarantineZone.get(cell).put(objectId, new ArrayList<double[]>());
					this.sketchedQuarantineZone.get(cell).get(objectId).add(resultRawMoments);
					this.sketchedQuarantineZone.get(cell).get(objectId).add(resultCentralMoments);
				}else {
					double resultCompleteCumulants[]=new double[arrayLenth];
					resultCompleteCumulants=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);

					totalCompleteCumulant=combine(resultCompleteCumulants, totalCompleteCumulant);
				}
				double resultCumulants[]=new double[arrayLenth];
				resultCumulants=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);

				totalCumulant=combine(totalCumulant, resultCumulants);
			}
			sketchedAggregationResult.put(cell, totalCumulant);
			sketchedCompleteAggregationResult.put(cell, totalCompleteCumulant);

			varX=totalCumulant[2];

			double eX2=varX+eX*eX;

			double reciprocalExpection=0.0;
			double reciprocalVariance=0.0;

			double pCountNonEqualZeroPerCell=1-pCountEqualZeroPerCell;

			if(eX!=0 && pCountEqualZeroPerCell!=0.0){
				reciprocalExpection=pCountNonEqualZeroPerCell/eX+(eX2/pCountNonEqualZeroPerCell-Math.pow(eX/pCountNonEqualZeroPerCell, 2))*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 3);
				reciprocalVariance=eX2*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 4)-Math.pow(pCountNonEqualZeroPerCell, 2)/Math.pow(eX, 2);

			}else if(pCountEqualZeroPerCell==0.0){
				reciprocalExpection=1/eX+(eX2-Math.pow(eX, 2))/Math.pow(eX, 3);
				reciprocalVariance=eX2*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 4)-Math.pow(pCountNonEqualZeroPerCell, 2)/Math.pow(eX, 2);
			}

			reciprocalExpection*=pCountNonEqualZeroPerCell;
			reciprocalVariance=(reciprocalVariance+Math.pow(reciprocalExpection/pCountNonEqualZeroPerCell, 2))*pCountNonEqualZeroPerCell-Math.pow(reciprocalExpection, 2);

			double[] statics=new double[4];
			statics[0]=eX;
			statics[1]=reciprocalExpection;
			statics[2]=reciprocalVariance;
			statics[3]=pCountEqualZeroPerCell;
			this.countStatistic.put(cell, statics);

		}
		return this.refreshFile();
	}


	//修改这个方法
	public File buildBasedOnCuboidByPWD(Cuboid<V> cuboid) {
		//build cells
		buildCellsBasedOnCuboid(cuboid);

		for(String cellKey:this.cellsToSubCells.keySet()){

			ArrayList<double[]> combineOfSubCell=new ArrayList<double[]>();
			int combineLength=combineOfSubCell.size();

			for(String subCellKey:cellsToSubCells.get(cellKey)){
				Complex[] pmf=cuboid.getConvolutionAggregationResult(subCellKey);
				if(combineOfSubCell.size()==0){
					double pCount0=0.0;
					for(int pmflength=0;pmflength<pmf.length;pmflength++){
						pCount0-=pmf[pmflength].getReal();
					}
					if(pCount0<0){
						pCount0=0.0;
					}


					double[] combination=new double[pmf.length];

					for(int pmflength=0;pmflength<pmf.length;pmflength++){
						combination[pmflength]=pmf[pmflength].getReal()*pCount0;
					}

					combineOfSubCell.add(combination);

					combineLength=combineOfSubCell.size();

				}else {
					System.out.println("combineLength:"+combineLength);
					System.out.println("combineOfSubCell.size()"+combineOfSubCell.size());


					System.out.println("combineLength:"+combineLength);
					System.out.println("combineOfSubCell.size()"+combineOfSubCell.size());

					double combination[]=new double[combineOfSubCell.get(0).length+pmf.length];

					for(int i=0;i<combineLength;i++){
						//combine the subcell
						for(int j=0;j<combineOfSubCell.get(0).length;j++){
							combination[j*combineLength+i]=pmf[i].getReal()*combineOfSubCell.get(0)[j];
						}


					}
					combineOfSubCell.remove(0);
					combineOfSubCell.add(combination);
					combineLength=combineOfSubCell.size();
				}
			}
			cellsPWD.put(cellKey, combineOfSubCell.get(0));
		}

		return this.refreshFile();
	}

	public File buildBasedOnCuboidBySketch(Cuboid<V> cuboid) {

		//build cells

		if(this.cells.isEmpty()){
			buildCellsBasedOnCuboid(cuboid);
		}

		int N=4;//n-th order cumulant\moment
		int arrayLenth=N+1;

		for(String cellKey:this.cellsToSubCells.keySet()){

			double sketchedCompleteResult[]=new double[5];
			sketchedCompleteResult[0]=0.0;
			sketchedCompleteResult[1]=0.0;
			sketchedCompleteResult[2]=0.0;
			sketchedCompleteResult[3]=0.0;
			sketchedCompleteResult[4]=0.0;

			this.sketchedQuarantineZone.put(cellKey, new HashMap<Object, ArrayList<double[]>>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){

				HashMap<Object, ArrayList<double[]>> objectMoments=cuboid.getSketchedQuarantineZone(subCellKey);
				if(objectMoments.size()!=0){
					for(Object objectId:objectMoments.keySet()){
						double rawMoments[]=objectMoments.get(objectId).get(0);

						if(!this.sketchedQuarantineZone.get(cellKey).containsKey(objectId)){
							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(rawMoments);
						}else {
							double combinedRawMoments[]=combineDouble(this.sketchedQuarantineZone.get(cellKey).get(objectId).get(0),rawMoments);
							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(combinedRawMoments);
						}
					}
				}

				sketchedCompleteResult=combineDouble(cuboid.getSketchedCompleteAggregationResult(subCellKey), sketchedCompleteResult);
			}
			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteResult);
		}

		double resultCumulants[]=new double[arrayLenth];

		for(String cellKey:this.sketchedQuarantineZone.keySet()){	
			double sketchedCompleteCumulants[]=this.sketchedCompleteAggregationResult.get(cellKey);

			HashMap<Object, ArrayList<double[]>> objectRawMoments=this.getSketchedQuarantineZone(cellKey);
			double totalCumulant[]=sketchedCompleteCumulants;

			if(objectRawMoments.size()!=0){
				for(Iterator<Object> objectIterator=objectRawMoments.keySet().iterator();objectIterator.hasNext();){
					Object objectId=objectIterator.next();

					double resultRawMoments[]=objectRawMoments.get(objectId).get(0);

					resultCumulants[0]=0.0;
					resultCumulants[1]=resultRawMoments[1];
					resultCumulants[2]=resultRawMoments[2]-resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[3]=resultRawMoments[3]-3*resultRawMoments[2]*resultRawMoments[1]+2*resultRawMoments[1]*resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[4]=resultRawMoments[4]-4*resultRawMoments[3]*resultRawMoments[1]-3*resultRawMoments[2]*resultRawMoments[2]+12*resultRawMoments[2]*resultRawMoments[1]*resultRawMoments[1]-6*Math.pow(resultRawMoments[1], 4);

					if(!isTruncated(objectId)){
						objectIterator.remove();			
						sketchedCompleteCumulants=combineDouble(resultCumulants, sketchedCompleteCumulants);
					}
					totalCumulant=combine(totalCumulant, resultCumulants);
				}
			}

			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteCumulants);
			this.sketchedAggregationResult.put(cellKey, totalCumulant);
		}

		return this.refreshFile();
	}

	public void buildBasedOnCuboidBySketchWithoutPersist(Cuboid<V> cuboid) {

		//build cells

		if(this.cells.isEmpty()){
			buildCellsBasedOnCuboid(cuboid);
		}

		int N=4;//n-th order cumulant\moment
		int arrayLenth=N+1;

		for(String cellKey:this.cellsToSubCells.keySet()){

			double sketchedCompleteResult[]=new double[5];
			sketchedCompleteResult[0]=0.0;
			sketchedCompleteResult[1]=0.0;
			sketchedCompleteResult[2]=0.0;
			sketchedCompleteResult[3]=0.0;
			sketchedCompleteResult[4]=0.0;

			this.sketchedQuarantineZone.put(cellKey, new HashMap<Object, ArrayList<double[]>>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){

				HashMap<Object, ArrayList<double[]>> objectMoments=cuboid.getSketchedQuarantineZone(subCellKey);
				if(objectMoments.size()!=0){
					for(Object objectId:objectMoments.keySet()){
						double rawMoments[]=objectMoments.get(objectId).get(0);

						if(!this.sketchedQuarantineZone.get(cellKey).containsKey(objectId)){
							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(rawMoments);
						}else {
							double combinedRawMoments[]=combineDouble(this.sketchedQuarantineZone.get(cellKey).get(objectId).get(0),rawMoments);
							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(combinedRawMoments);
						}
					}
				}

				sketchedCompleteResult=combineDouble(cuboid.getSketchedCompleteAggregationResult(subCellKey), sketchedCompleteResult);
			}
			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteResult);
		}

		double resultCumulants[]=new double[arrayLenth];

		for(String cellKey:this.sketchedQuarantineZone.keySet()){	
			double sketchedCompleteCumulants[]=this.sketchedCompleteAggregationResult.get(cellKey);

			HashMap<Object, ArrayList<double[]>> objectRawMoments=this.getSketchedQuarantineZone(cellKey);
			double totalCumulant[]=sketchedCompleteCumulants;

			if(objectRawMoments.size()!=0){
				for(Iterator<Object> objectIterator=objectRawMoments.keySet().iterator();objectIterator.hasNext();){
					Object objectId=objectIterator.next();

					double resultRawMoments[]=objectRawMoments.get(objectId).get(0);

					resultCumulants[0]=0.0;
					resultCumulants[1]=resultRawMoments[1];
					resultCumulants[2]=resultRawMoments[2]-resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[3]=resultRawMoments[3]-3*resultRawMoments[2]*resultRawMoments[1]+2*resultRawMoments[1]*resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[4]=resultRawMoments[4]-4*resultRawMoments[3]*resultRawMoments[1]-3*resultRawMoments[2]*resultRawMoments[2]+12*resultRawMoments[2]*resultRawMoments[1]*resultRawMoments[1]-6*Math.pow(resultRawMoments[1], 4);

					if(!isTruncated(objectId)){
						objectIterator.remove();			
						sketchedCompleteCumulants=combineDouble(resultCumulants, sketchedCompleteCumulants);
					}
					totalCumulant=combine(totalCumulant, resultCumulants);
				}
			}

			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteCumulants);
			this.sketchedAggregationResult.put(cellKey, totalCumulant);
		}
	}

	public File buildBasedOnCuboidBySketchReturnCountReciprocalExpection(Cuboid<V> cuboid) {

		//build cells

		if(this.cells.isEmpty()){
			buildCellsBasedOnCuboid(cuboid);
		}

		int N=4;//n-th order cumulant\moment
		int arrayLenth=N+1;

		for(String cellKey:this.cellsToSubCells.keySet()){
			if(cellKey.equals("Manufacturer#5|1992|AMERICA")){
				System.out.println("debug");
			}

			double sketchedCompleteResult[]=new double[5];
			sketchedCompleteResult[0]=0.0;
			sketchedCompleteResult[1]=0.0;
			sketchedCompleteResult[2]=0.0;
			sketchedCompleteResult[3]=0.0;
			sketchedCompleteResult[4]=0.0;

			this.sketchedQuarantineZone.put(cellKey, new HashMap<Object, ArrayList<double[]>>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){

				HashMap<Object, ArrayList<double[]>> objectMoments=cuboid.getSketchedQuarantineZone(subCellKey);
				if(objectMoments.size()!=0){
					for(Object objectId:objectMoments.keySet()){
						double rawMoments[]=objectMoments.get(objectId).get(0);

						if(!this.sketchedQuarantineZone.get(cellKey).containsKey(objectId)){
							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(rawMoments);
							//this.sketchedQuarantineZone.get(cellKey).get(objectId).add(centralMoments);
						}else {
							double combinedRawMoments[]=combineDouble(this.sketchedQuarantineZone.get(cellKey).get(objectId).get(0),rawMoments);
							//double combinedCentralMoments[]=combineDouble(this.sketchedQuarantineZone.get(cellKey).get(objectId).get(1),centralMoments);

							this.sketchedQuarantineZone.get(cellKey).put(objectId, new ArrayList<double[]>());
							this.sketchedQuarantineZone.get(cellKey).get(objectId).add(combinedRawMoments);
							//this.sketchedQuarantineZone.get(cellKey).get(objectId).add(combinedCentralMoments);
						}
					}
				}

				sketchedCompleteResult=combineDouble(cuboid.getSketchedCompleteAggregationResult(subCellKey), sketchedCompleteResult);
			}
			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteResult);
		}

		double resultCumulants[]=new double[arrayLenth];

		for(String cellKey:this.sketchedQuarantineZone.keySet()){
			if(cellKey.equals("Manufacturer#5|1992|AMERICA")){
				System.out.println("debug");
			}


			double sketchedCompleteCumulants[]=this.sketchedCompleteAggregationResult.get(cellKey);

			double eX=sketchedCompleteCumulants[1];
			double eXt=0.0;

			HashMap<Object, ArrayList<double[]>> objectRawMoments=this.getSketchedQuarantineZone(cellKey);
			double totalCumulant[]=sketchedCompleteCumulants;

			double pCountEqualZeroPerCell=1.0;

			if(objectRawMoments.size()!=0){
				for(Iterator<Object> objectIterator=objectRawMoments.keySet().iterator();objectIterator.hasNext();){
					Object objectId=objectIterator.next();

					double resultRawMoments[]=objectRawMoments.get(objectId).get(0);
					//double resultCentralMoments[]=Moment.calculateCentralMoments(a, 4, resultRawMoments[1]);
					//resultCumulants=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);		

					resultCumulants[0]=0.0;
					resultCumulants[1]=resultRawMoments[1];
					resultCumulants[2]=resultRawMoments[2]-resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[3]=resultRawMoments[3]-3*resultRawMoments[2]*resultRawMoments[1]+2*resultRawMoments[1]*resultRawMoments[1]*resultRawMoments[1];
					resultCumulants[4]=resultRawMoments[4]-4*resultRawMoments[3]*resultRawMoments[1]-3*resultRawMoments[2]*resultRawMoments[2]+12*resultRawMoments[2]*resultRawMoments[1]*resultRawMoments[1]-6*Math.pow(resultRawMoments[1], 4);

					if(!isTruncated(objectId)){
						objectIterator.remove();			
						sketchedCompleteCumulants=combineDouble(resultCumulants, sketchedCompleteCumulants);
						pCountEqualZeroPerCell=0.0;
					}else {
						Complex[] pmfs=this.cells.get(cellKey).get(objectId);
						pCountEqualZeroPerCell*=zeroPro(pmfs);
						eXt+=resultCumulants[1]*zeroPro(pmfs);
					}
					totalCumulant=combine(totalCumulant, resultCumulants);
					eX+=resultCumulants[1];
				}
			}

			this.sketchedCompleteAggregationResult.put(cellKey, sketchedCompleteCumulants);
			this.sketchedAggregationResult.put(cellKey, totalCumulant);

			double varX=totalCumulant[2];

			double eX2=varX+eX*eX;

			double reciprocalExpection=0.0;
			double reciprocalVariance=0.0;

			double pCountNonEqualZeroPerCell=1-pCountEqualZeroPerCell;

			if(eX!=0 && pCountEqualZeroPerCell!=0.0){
				reciprocalExpection=pCountNonEqualZeroPerCell/eX+(eX2/pCountNonEqualZeroPerCell-Math.pow(eX/pCountNonEqualZeroPerCell, 2))*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 3);
				reciprocalVariance=eX2*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 4)-Math.pow(pCountNonEqualZeroPerCell, 2)/Math.pow(eX, 2);

			}else if(pCountEqualZeroPerCell==0.0){
				reciprocalExpection=1/eX+(eX2-Math.pow(eX, 2))/Math.pow(eX, 3);
				reciprocalVariance=eX2*Math.pow(pCountNonEqualZeroPerCell, 3)/Math.pow(eX, 4)-Math.pow(pCountNonEqualZeroPerCell, 2)/Math.pow(eX, 2);
			}

			reciprocalExpection*=pCountNonEqualZeroPerCell;
			reciprocalVariance=(reciprocalVariance+Math.pow(reciprocalExpection/pCountNonEqualZeroPerCell, 2))*pCountNonEqualZeroPerCell-Math.pow(reciprocalExpection, 2);

			double[] statics=new double[4];
			statics[0]=eX;
			statics[1]=pCountEqualZeroPerCell;
			statics[2]=reciprocalVariance;
			statics[3]=eXt;
			this.countStatistic.put(cellKey, statics);
		}

		return null;
		//return this.refreshFile();
	}

	public File refreshFile(){

		String fileName="";

		for(int i=0;i<this.dimensions.length;i++){
			fileName+=this.dimensions[i].getDimensionName();
			fileName+=this.dimensions[i].getPath().length;
		}

		File file=new File("cube/"+fileName);

		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectOutputStream out;

		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
	}

	public File refreshCountFile(){

		String fileName="";

		for(int i=0;i<this.dimensions.length;i++){
			fileName+=this.dimensions[i].getDimensionName();
			fileName+=this.dimensions[i].getPath().length;
		}

		File file=new File("cubeCount/"+fileName);

		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectOutputStream out;

		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
	}

	public void convertToCumulants(){
		for(String key:convolutionAggregationResult.keySet()){
			int N=10;//n-th order cumulant\moment
			int arrayLenth=N+1;
			double resultRawMoments[]=new double[arrayLenth];
			double resultCentralMoments[]=new double[arrayLenth];
			resultRawMoments=Moment.calculateRawMoments(convolutionAggregationResult.get(key), N);
			resultCentralMoments=Moment.calculateCentralMoments(convolutionAggregationResult.get(key), 3, resultRawMoments[1]);
			double resultCumulants[]=new double[arrayLenth];
			resultCentralMoments=Cumulant.calucateCumulants(resultRawMoments, resultCentralMoments);
			sketchedAggregationResult.put(key, resultCentralMoments);
		}
		int i=0;
		for(String cellv1:sketchedAggregationResult.keySet()){
			for(String cellv2:sketchedAggregationResult.keySet()){
				if(cellv1.equals(cellv2)){
					i++;
				}
			}
		}
		System.out.println(i);
		System.out.println(this.sketchedAggregationResult.size());
		System.out.println("ff");

	}


	public void printSketchedAggregation(){
		int index=0;
		for(String key:sketchedAggregationResult.keySet()){
			System.out.print("Cell "+index+":");
			System.out.println(key);
			System.out.println();
			double cumulants[]=sketchedAggregationResult.get(key);
			for(int i=0;i<cumulants.length;i++){
				System.out.println(i+" : "+cumulants[i]);
			}

			double tv=0.5;
			double a=Math.abs(tv-cumulants[0]);

			double upper=cumulants[1]/(cumulants[1]+a*a);
			double low=a*a/(cumulants[1]+a*a);

			System.out.println("上界："+upper);
			System.out.println("下界："+low);
		}
	}

	public void buildCellBasedOnCuboid(Cuboid cuboid){
		for(String cellKey:this.cellsToSubCells.keySet()){

			Complex result[] = new Complex[2];
			result[0]=new Complex(1.0, 0.0);	result[1]=new Complex(0.0, 0.0);

			boolean flag=false;
			int max=0;
			this.fftQuarantineZone.put(cellKey, new HashMap<Object, Complex[]>());
			for(String subCellKey:cellsToSubCells.get(cellKey)){
				HashMap<Object, Complex[]> objectPmfs=cuboid.getFftQuarantineZone(subCellKey);

				Complex[] pmfSubCell=cuboid.getConvolutionAggregationResult(subCellKey);

				if(!flag){
					max=result.length+pmfSubCell.length-1;
					result=FFT.convolution(result, pmfSubCell);
					flag=true;
				}else {
					if(result.length<=(max+pmfSubCell.length-1)){
						result=FFT.ifft(result);
						if(result.length>max){
							result=FFT.removeExtra(result, max);
						}
						max=result.length+pmfSubCell.length-1;
						result=FFT.convolution(result, pmfSubCell);
					}else {
						max=max+pmfSubCell.length-1;
						result=FFT.product(result, pmfSubCell);
					}
				}
			}
			if(flag){
				result=FFT.ifft(result);
				if(result.length>max){
					result=FFT.removeExtra(result, max);
				}
			}
			this.completePMFAggregationResult.put(cellKey, result);
		}
	}

	public void printSketchedAggregation(double region){
		int index=0;

		int sum=(int) (sketchedAggregationResult.size()*region);
		int num=0;

		for(String key:sketchedAggregationResult.keySet()){			
			num++;
			if(num>sum){
				break;
			}

			System.out.print("Cell "+index+":");
			System.out.println(key);
			System.out.println();
			double cumulants[]=sketchedAggregationResult.get(key);
			for(int i=1;i<cumulants.length;i++){
				System.out.println(i+" : "+cumulants[i]);
			}
		}
	}

	public void printSketchedResults(){
		int index=0;

		for(String key:sketchedAggregationResult.keySet()){			
			System.out.print("Cell "+index+":");
			System.out.println(key);
			System.out.println();
			double cumulants[]=sketchedAggregationResult.get(key);
			for(int i=1;i<cumulants.length;i++){
				System.out.println(i+" : "+cumulants[i]);
			}
		}
	}


	public void buildCells(HashMap<Object,ObjectGroupedTuples<V>> objectGroupedTuples){
		int i=0;
		for(Iterator<ObjectGroupedTuples<V>> objectGroupedTuplesIter=objectGroupedTuples.values().iterator();objectGroupedTuplesIter.hasNext();){
			ObjectGroupedTuples<V> tuplesForSingleObject=objectGroupedTuplesIter.next();
			Object objectId=tuplesForSingleObject.getObjectid();

			//System.out.println("objectid:"+objectId);

			HashMap<String, Complex[]> pmfValues=tuplesForSingleObject.getPmfValues();


			for(String dimensionKey:pmfValues.keySet()){
				if(!this.cells.containsKey(dimensionKey)){
					cells.put(dimensionKey, new HashMap<Object, Complex[]>());
				}
				cells.get(dimensionKey).put(objectId, pmfValues.get(dimensionKey));
				//System.out.println("====="+dimensionKey);
				//FFT.show(pmfValues.get(dimensionKey), "pmfValues.get(dimensionKey)");

			}
			if(i==1){
				//break;
			}
			i++;
		}
	}



	public void buildCellsBasedOnCuboid(Cuboid cuboid){
		//根据cuboid的cells、dimension确定当前的cells

		//确定出来两者dimension的差值
		//1、如果是同一个dimension中path的上卷，例如说年月日变成了日了，
		//原本一个cell中对应的是 年、月、日 measures,现在要变成年 measures，
		//先确定cell的V[]，然后cuboid中的不同的cells放置到这个新的cell当中。		

		for(Iterator<String> iterator=cuboid.getCells().keySet().iterator();iterator.hasNext();){
			String keyString=iterator.next();

			V[] key=ConvertBetweenStringArray.convert(keyString);

			HashMap<V[], HashMap<Object, Complex[]>> cell=convertDimension(cuboid,key,(HashMap<Object, Complex[]>) cuboid.getCells().get(keyString));

			V[] cellsKey=cell.keySet().iterator().next();

			HashMap<Object, Complex[]> cellsMeasure= cell.get(cellsKey);

			//System.out.println("==========="+cellsMeasure.keySet().iterator().next());
			//FFT.show(cellsMeasure.values().iterator().next(), "cellsMeasure.values().iterator().next()");

			String cellsKeyString=ConvertBetweenStringArray.convert(cellsKey);

			if(!this.cells.containsKey(cellsKeyString)){
				this.cells.put(cellsKeyString, new HashMap<Object, Complex[]>());
				this.cellsToSubCells.put(cellsKeyString, new ArrayList<String>());
			}

			this.cellsToSubCells.get(cellsKeyString).add(keyString);

			for(Object object:cellsMeasure.keySet()){
				if(!this.cells.get(cellsKeyString).containsKey(object)){
					this.cells.get(cellsKeyString).put(object, cellsMeasure.get(object));
				}else {
					int length=FFT.maxLength(this.cells.get(cellsKeyString).get(object));
					int newlength=FFT.maxLength(cellsMeasure.get(object));
					if(newlength>length){
						this.cells.get(cellsKeyString).put(object, cellsMeasure.get(object));
					}
				}
			}
		}

		//System.out.println("this.cells.size:"+this.cells.size());

		for(String cellsKeyString:this.cells.keySet()){
			int sumLength=0;
			for(Object object:this.cells.get(cellsKeyString).keySet()){
				sumLength+=FFT.maxLength(this.cells.get(cellsKeyString).get(object));
			}
			this.cellsPmfLength.put(cellsKeyString, sumLength);
		}

		//this.cells.clear();

		this.isTruncated=new HashMap<>();

		for(String cellKey:this.getCellsToSubCells().keySet()){
			HashSet<Object> objectSet=new HashSet<>();
			for(String subCellKey:this.getCellsToSubCells().get(cellKey)){
				HashMap<String, Integer> tmp=(HashMap<String, Integer>) cuboid.getCells().get(subCellKey);
				for(Object object:tmp.keySet()){
					objectSet.add(object);
				}
			}
			for(Object object:objectSet){
				if(!isTruncated.containsKey(object)){
					isTruncated.put(object, false);
				}else if (!isTruncated.get(object)) {
					isTruncated.put(object, true);
				}
			}			
		}
	}



	private Complex[] combineComplex(Complex[] a, Complex[] b) {
		int length= a.length>=b.length ? a.length :b.length;
		int min_length=a.length>=b.length ? b.length :a.length;

		Complex[] c=new Complex[length];
		for(int i=0;i<min_length;i++){
			c[i]=a[i].add(b[i]);
		}
		if(a.length>b.length){
			System.arraycopy(a, min_length, c, min_length, length-min_length);
		}else {
			System.arraycopy(b, min_length, c, min_length, length-min_length);
		}
		return c;
	}

	private double[] combineDouble(double[] a, double[] b) {
		int length= a.length>=b.length ? a.length :b.length;
		int min_length=a.length>=b.length ? b.length :a.length;

		double[] c=new double[length];
		for(int i=0;i<min_length;i++){
			c[i]=a[i]+b[i];
		}
		if(a.length>b.length){
			System.arraycopy(a, min_length, c, min_length, length-min_length);
		}else {
			System.arraycopy(b, min_length, c, min_length, length-min_length);
		}
		return c;
	}


	private HashMap<V[], HashMap<Object, Complex[]>> convertDimension(Cuboid cuboids,V[] key, HashMap<Object, Complex[]> measure) {
		int sumlength=0;

		for(int i=0;i<this.getDimensions().length;i++){
			sumlength+=this.getDimensions(i).getPath().length;
		}

		HashMap<V[], HashMap<Object, Complex[]>> cell=new HashMap<V[], HashMap<Object,Complex[]>>();

		V[] cellsKey=(V[]) new Object[sumlength];

		int index=0;

		for(int i=0;i<this.getDimensions().length;i++){

			Dimensions d1=this.getDimensions(i);
			List<Integer> result=cuboids.containDimension(d1);
			if(result!=null && result.size()==2){
				int culumnStartIndex=result.get(0);
				int culumnEndIndex=result.get(1);

				for(int k=culumnStartIndex;k<culumnEndIndex;k++){
					cellsKey[index]=key[k];
					index++;
				}
			}
		}

		cell.put(cellsKey, measure);
		return cell;
	}


	private List<Integer> containDimension(Dimensions d1) {
		int culumnStartIndex=0;
		int culumnEndIndex=0;
		for(int i=0;i<this.getDimensions().length;i++){
			Dimensions d2=this.getDimensions(i);

			if(d2.getDimensionName().equals(d1.getDimensionName())){
				culumnEndIndex+=d1.getPath().length;
				List<Integer> result=new ArrayList<Integer>();
				result.add(culumnStartIndex);
				result.add(culumnEndIndex);
				return result;
			}else {
				culumnStartIndex+=d2.getPath().length;
				culumnEndIndex+=d2.getPath().length;
			}
		}
		return null;
	}

	public void printCountSumJointPro(){
		System.err.println("==========================start.printCountSumJointPro()");
		int index=0;

		int sum=0;

		for(Iterator<String> keyItertor=countSumJointPro.keySet().iterator();keyItertor.hasNext();){
			String key=keyItertor.next();
			System.out.print("Cell "+index+":");
			index++;
			System.out.print('\t');
			System.out.print(key);
			System.out.print('\t');
			System.out.println();

			for(Iterator<Integer> countItertor=countSumJointPro.get(key).keySet().iterator();countItertor.hasNext();){
				int countKey=countItertor.next();

				System.out.print('\t');
				System.out.print("count:"+countKey);
				System.out.print('\t');
				System.out.println();

				Complex resultComplex[]=countSumJointPro.get(key).get(countKey);
				Complex result=new Complex(0.0);

				sum+=resultComplex.length;

				//FFT.show(resultComplex, "resultComplex");
				System.err.println("resultComplex.length:"+resultComplex.length);
				for(int i=0;i<resultComplex.length;i++){
					//result.add(resultComplex[i]);
					System.out.println(resultComplex[i].getReal());
				}

				System.err.println("result:"+result.getReal());
			}
		}
		System.err.println("==========================end.printCountSumJointPro()");
	}

	public ArrayList<V[]> generateRandomQueryRegion(){
		int size=ConvertBetweenStringArray.convert(convolutionAggregationResult.keySet().iterator().next()).length;

		V[] minimal=(V[]) new Object[size];
		V[] maximal=(V[]) new Object[size];

		boolean firstKey=true;

		for(Iterator<String> keyItertor=convolutionAggregationResult.keySet().iterator();keyItertor.hasNext();){
			String key=keyItertor.next();
			V[] cellArray=ConvertBetweenStringArray.convert(key);

			for(int i=0;i<size;i++){
				if(firstKey){
					minimal[i]=cellArray[i];
					maximal[i]=cellArray[i];

					firstKey=false;
				}else {
					if(cellArray[i] instanceof Integer){
						if((int)minimal[i]>(int)cellArray[i]){
							minimal[i]=cellArray[i];
						}
						if((int)maximal[i]<(int)cellArray[i]){
							maximal[i]=cellArray[i];
						}
					}else if(cellArray[i] instanceof String){
						if(String.valueOf(minimal[i]).compareTo((String)cellArray[i])>0){
							minimal[i]=cellArray[i];
						}
						if(String.valueOf(maximal[i]).compareTo((String)cellArray[i])<0){
							maximal[i]=cellArray[i];
						}
					}
				}
			}
		}

		V[] lower=(V[]) new Object[size];
		V[] upper=(V[]) new Object[size];

		Random random=new Random();

		for(int i=0;i<size;i++){
			if(minimal[i] instanceof Integer){

				int lowerInt=random.nextInt((int)maximal[i]-(int)minimal[i])+(int)minimal[i];
				int upperInt=random.nextInt((int)maximal[i]-(int)minimal[i])+(int)minimal[i];
				if(lowerInt>upperInt){
					int swap=lowerInt;
					lowerInt=upperInt;
					upperInt=swap;
				}

				lower[i]=(V)Integer.valueOf(lowerInt);
				upper[i]=(V)Integer.valueOf(upperInt);

			}else if(minimal[i] instanceof String){
				String lowerString=String.valueOf((char)(random.nextInt(String.valueOf(maximal[i]).compareTo((String)minimal[i]))+Integer.valueOf(String.valueOf(minimal[i]).toCharArray()[0])));
				String upperString=String.valueOf((char)(random.nextInt(String.valueOf(maximal[i]).compareTo((String)minimal[i]))+Integer.valueOf(String.valueOf(minimal[i]).toCharArray()[0])));
				if(lowerString.compareTo(upperString)>0){
					String swap=lowerString;
					lowerString=upperString;
					upperString=swap;
				}

				lower[i]=(V)lowerString;
				upper[i]=(V)upperString;
			}
		}

		ArrayList<V[]> result=new ArrayList<>();
		result.add(lower);
		result.add(upper);

		return result;
	}

	public HashMap<String, Complex[]> psdBasedOnConvolution(V[] lowerBound,V[] upperBound){
		HashMap<String, Complex[]> pdqResultMap=new HashMap<>();

		for(Iterator<String> keyItertor=convolutionAggregationResult.keySet().iterator();keyItertor.hasNext();){

			String key=keyItertor.next();
			V[] cellArray=ConvertBetweenStringArray.convert(key);

			if(isInInterval(cellArray, lowerBound, upperBound)){
				pdqResultMap.put(key, convolutionAggregationResult.get(key));
			}
		}


		return pdqResultMap;
	}

	public HashMap<String, double[]> psdBasedOnSketch(V[] lowerBound,V[] upperBound){
		HashMap<String, double[]> pdqResultMap=new HashMap<>();

		for(Iterator<String> keyItertor=sketchedAggregationResult.keySet().iterator();keyItertor.hasNext();){

			String key=keyItertor.next();
			V[] cellArray=ConvertBetweenStringArray.convert(key);

			if(isInInterval(cellArray, lowerBound, upperBound)){
				pdqResultMap.put(key, sketchedAggregationResult.get(key));
			}
		}


		return pdqResultMap;
	}

	public ArrayList<String> pdqBasedOnConvolution(double tvdouble, double tp){

		ArrayList<String> pdqResult=new ArrayList<>();

		for(String key:convolutionAggregationResult.keySet()){
			int length=this.cellsPmfLength.get(key);
			double tv=tvdouble*length;

			Complex[] pmfs=this.getConvolutionAggregationResult(key);

			double pro=0.0;
			for(int i=(int)tp+1;i<pmfs.length;i++){
				pro+=pmfs[i].getReal();
			}
			if(pro>tv){
				pdqResult.add(key);
			}
		}
		return pdqResult;
	}

	public ArrayList<String> pdqBasedOnSketchwith1thMoment(Cuboid ancestorCuboid,double tvdouble, double tp) throws Exception{

		ArrayList<String> pdqResult=new ArrayList<>();

		for(String key:sketchedAggregationResult.keySet()){
			int length=this.cellsPmfLength.get(key);

			double cumulants[]=sketchedAggregationResult.get(key);


			double tv=tvdouble*length;
			System.out.println("tv:"+tv);
			double a=Math.abs(tv-cumulants[1]);

			double upper=cumulants[1]/tv;
			double low=cumulants[1]/(2*cumulants[1]-tv);

			if(cumulants[1]<=tv){
				//有上界
				if(upper<=tp){
					pdqResult.add(key);
				}else {
					Complex[] pmfs=this.buildSingleCellBasedOnCuboid(ancestorCuboid,key);

					double pro=0.0;
					for(int i=(int)tp+1;i<pmfs.length;i++){
						pro+=pmfs[i].getReal();
					}
					if(pro>tv){
						pdqResult.add(key);
					}
				}
			}else {
				if(low>=tp){
					pdqResult.add(key);
				}else {
					Complex[] pmfs=this.buildSingleCellBasedOnCuboid(ancestorCuboid,key);

					double pro=0.0;
					for(int i=(int)tp+1;i<pmfs.length;i++){
						pro+=pmfs[i].getReal();
					}
					if(pro>tv){
						pdqResult.add(key);
					}
				}
			}	
		}
		return pdqResult;
	}

	public ArrayList<String> pdqBasedOnSketchwith4thMoment(Cuboid ancestorCuboid,double tvdouble, double tp) throws Exception{

		ArrayList<String> pdqResult=new ArrayList<>();

		for(String key:sketchedAggregationResult.keySet()){
			int length=this.cellsPmfLength.get(key);

			double cumulants[]=sketchedAggregationResult.get(key);


			double tv=tvdouble*length;
			System.out.println("tv:"+tv);
			double a=Math.abs(tv-cumulants[1]);

			double u1=cumulants[1];
			double u2=cumulants[2]+Math.pow(u1, 2);
			double u3=cumulants[3]+3*u2*u1-2*Math.pow(u1, 3);
			double u4=cumulants[4]+4*u3*u1+3*Math.pow(u2, 2)-12*u2*Math.pow(u1, 2)+6*Math.pow(u1, 4);

			double M2=cumulants[2];
			double M4=u4-4*u3*u1+6*u2*Math.pow(u1, 2)-3*Math.pow(u1, 4);

			double L=M2/Math.pow(a, 2);
			double K=M4/Math.pow(M2, 2);

			double upper1=M2/(M2+a*a);
			double upper2=(M4-Math.pow(M2, 2))/(M4-2*M2*a*a+a*a);
			double upper3=0.5+Math.pow(0.25-1/(3+M4/Math.pow(M2, 2)), 0.5);

			double low1=1.0-upper1;
			double low2=1.0-upper2;	
			double low3=1.0-upper3;

			double limitK1=L+1.0/L-1.0;
			boolean flag2=false;

			if(Math.pow(1/L, 0.5)>=(Math.pow(K-1+Math.pow(K*K+2*K-3, 0.5), 0.5)-0.5*(Math.pow(K+3, 0.5)-Math.pow(K-1,0.5)))){
				flag2=true;
			}
			
			if(cumulants[1]<=tv){
				if(K>=limitK1){
					if(upper1<=tp){
						pdqResult.add(key);
					}
				}else if (K<=limitK1 && L<1) {
					if(upper2<=tp){
						pdqResult.add(key);
					}
				}else if (K<=limitK1 && L>=1 && flag2){
					if(upper3<=tp){
						pdqResult.add(key);
					}
				}
			}else {
				if(K>=limitK1){
					if(low1>=tp){
						pdqResult.add(key);
					}
				}else if (K<=limitK1 && L<1) {
					if(low2>=tp){
						pdqResult.add(key);
					}
				}else if (K<=limitK1 && L>=1 && flag2){
					if(low3>=tp){
						pdqResult.add(key);
					}
				}
			}
			
			if(!pdqResult.contains(key)){
				Complex[] pmfs=this.buildSingleCellBasedOnCuboid(ancestorCuboid,key);

				double pro=0.0;
				for(int i=(int)tp+1;i<pmfs.length;i++){
					pro+=pmfs[i].getReal();
				}
				if(pro>tv){
					pdqResult.add(key);
				}	
			}
		}
		return pdqResult;
	}

	public ArrayList<String> pdqBasedOnSketch(Cuboid ancestorCuboid,double tvdouble, double tp) throws Exception{

		ArrayList<String> pdqResult=new ArrayList<>();

		for(String key:sketchedAggregationResult.keySet()){
			int length=this.cellsPmfLength.get(key);

			double cumulants[]=sketchedAggregationResult.get(key);


			double tv=tvdouble*length;
			System.out.println("tv:"+tv);
			double a=Math.abs(tv-cumulants[1]);

			double upper=cumulants[2]/(cumulants[2]+a*a);
			double low=a*a/(cumulants[2]+a*a);

			if(cumulants[1]<=tv){
				//有上界
				if(upper<=tp){
					pdqResult.add(key);
				}else {

					Complex[] pmfs=this.buildSingleCellBasedOnCuboid(ancestorCuboid,key);

					double pro=0.0;
					for(int i=(int)tp+1;i<pmfs.length;i++){
						pro+=pmfs[i].getReal();
					}
					if(pro>tv){
						pdqResult.add(key);
					}	
				}
			}else {
				if(low>=tp){
					pdqResult.add(key);
				}else {
					Complex[] pmfs=this.buildSingleCellBasedOnCuboid(ancestorCuboid,key);

					double pro=0.0;
					for(int i=(int)tp+1;i<pmfs.length;i++){
						pro+=pmfs[i].getReal();
					}
					if(pro>tv){
						pdqResult.add(key);
					}	
				}
			}		
		}
		return pdqResult;
	}


	public boolean isInInterval(V[] cellArray,V[] lowerBound,V[] upperBound){
		if(cellArray.length!=lowerBound.length || cellArray.length!=lowerBound.length){
			try {
				throw new Exception("length of interval is not match to cuboid");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for(int i=0;i<cellArray.length;i++){
			if(cellArray[i] instanceof Integer){
				if((int)lowerBound[i]>(int)cellArray[i]||(int)upperBound[i]<(int)cellArray[i]){
					return false;
				}
			}else if(cellArray[i] instanceof String){
				if(String.valueOf(lowerBound[i]).compareTo((String)cellArray[i])>0||String.valueOf(upperBound[i]).compareTo((String)cellArray[i])<0){
					return false;
				}
			}
		}

		return true;
	}

	public int printAggregation(){
		System.err.println("==========================start.printAggregation()");
		int index=0;

		int sum=0;

		for(Iterator<String> keyItertor=convolutionAggregationResult.keySet().iterator();keyItertor.hasNext();){
			//for(String key:convolutionAggregationResult.keySet()){
			String key=keyItertor.next();
			System.out.print("Cell "+index+":");
			index++;
			System.out.print('\t');
			System.out.print(key);
			System.out.print('\t');
			System.out.println();
			Complex resultComplex[]=convolutionAggregationResult.get(key);
			Complex result=new Complex(0.0);

			for(int i=0;i<resultComplex.length;i++){
				System.out.println(resultComplex[i].getReal());
			}
		}
		System.err.println("==========================end.printAggregation()");
		return sum;
	}

	public void printCell(){
		System.err.println("==========================start.printAggregation()");
		int index=0;
		for(Iterator<String> keyItertor=convolutionAggregationResult.keySet().iterator();keyItertor.hasNext();){
			//for(String key:convolutionAggregationResult.keySet()){
			String key=keyItertor.next();
			System.out.print("Cell "+index+":");
			index++;
			System.out.print('\t');
			System.out.print(key);
			System.out.print('\t');
			System.out.println();
		}
		System.err.println("==========================end.printAggregation()");
	}

	private static boolean is2Pow(int n) {
		int result = ((n&(n-1))==0) ? (1) : (0);
		if(result==1){
			return true;
		}else {
			return false;
		}
	}

	private Complex[] zeroPadded(Complex[] complexs) {
		double zeroPadAmout=1.0;
		for(int i=0;i<complexs.length;i++){
			zeroPadAmout-=complexs[i].getReal();
		}

		Complex calComplex[]=complexs.clone();
		calComplex[0]=new Complex(zeroPadAmout, 0.0);
		return calComplex;
	}

	private double zeroPro(Complex[] complexs) {
		double zeroPadAmout=1.0;
		for(int i=0;i<complexs.length;i++){
			zeroPadAmout-=complexs[i].getReal();
		}

		return zeroPadAmout;
	}

	public boolean isEqualArray(V[] a,V[] b){
		if(a.length!=b.length){
			return false;
		}else {
			for(int i=0;i<a.length;i++){
				if(!a[i].equals(b[i])){
					return false;
				}
			}
		}
		return true;
	}

	public double[] combine(double a[],double b[]){
		double c[]=new double[a.length];
		for(int i=1;i<a.length;i++){
			c[i]=a[i]+b[i];
		}
		return c;
	}

	public boolean isGreaterThen(Cuboid cuboid){
		if(cuboid.dimensions.length==this.dimensions.length){
			for(int i=0;i<cuboid.dimensions.length;i++){
				if(this.dimensions[i].getPath().length<cuboid.dimensions[i].getPath().length){
					return false;
				}
			}
			return true;
		}else {
			return false;
		}
	}
}
