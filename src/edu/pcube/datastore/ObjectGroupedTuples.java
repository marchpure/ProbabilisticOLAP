package edu.pcube.datastore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.complex.Complex;

import edu.pcube.util.ConvertBetweenStringArray;


public class ObjectGroupedTuples<V> {
	private final Object objectid;
	//private String[] dimensions;

	private HashMap<String, Complex[]> pmfValues;
	private ArrayList<String> rowValues;

	private InMemoryDataStore inMemoryDataStore;

	public ObjectGroupedTuples(Object objectid,InMemoryDataStore inMemoryDataStore) {
		this.objectid=objectid;

		this.rowValues=new ArrayList<String>();
		this.pmfValues=new HashMap<String, Complex[]>();
		this.inMemoryDataStore=inMemoryDataStore;
	}	

	public Object getObjectid() {
		return objectid;
	}

	

	public HashMap<String, Complex[]> getPmfValues() {
		return pmfValues;
	}

	public ArrayList<String> getRowValues() {
		return rowValues;
	}

	public int getMeasuresMax(){
		int max=0;
		for(Iterator<String> iter=this.rowValues.iterator();iter.hasNext();){
			String row=iter.next();
			BigDecimal bd=new BigDecimal((String)ConvertBetweenStringArray.convert(row)[inMemoryDataStore.getColumnIndex("X")]);
			int measure=bd.intValue();
			//int measure=(Integer) row[this.getColumnIndex().get("X")];
			if(max<measure){
				max=measure;
			}
		}

		return max;
	}

	public V[] getDimensionData(V[] tuple){
		int N=tuple.length;
		V[] dimensionData=(V[]) new Object[N-2];

		int index=0;
		for(int i=0;i<inMemoryDataStore.getDimensions().length;i++){
			String columnHeader=inMemoryDataStore.getDimensions()[i];
			dimensionData[index]=tuple[inMemoryDataStore.getColumnIndex(columnHeader)];
			index++;
		}
		return dimensionData;
	}

	public V getMeasureData(V[] tuple){
		return tuple[inMemoryDataStore.getColumnIndex("X")];
	}

	public Complex getPData(V[] tuple){
		return new Complex(Double.valueOf((String)tuple[inMemoryDataStore.getColumnIndex("P")]), 0.0);
	}

	public boolean isTruncated(){
		if(this.getPmfValues().size()>1){
			return true;
		}else {
			return false;
		}
	}

	public void generatePMF(){
		int N=this.getMeasuresMax()-this.getMeasuresMin()+2;
		//int N=this.getMeasuresMax()-this.getMeasuresMin()+50;
		int MIN=this.getMeasuresMin();

		for(Iterator<String> iter=this.rowValues.iterator();iter.hasNext();){
			V[] tuple=ConvertBetweenStringArray.convert(iter.next());
			V[] dimension=this.getDimensionData(tuple);
			V measure=this.getMeasureData(tuple);

			Complex p=this.getPData(tuple);

			//System.out.println("---------------");
			String dimensionKey=ConvertBetweenStringArray.convert(dimension);
			if(!this.pmfValues.containsKey(dimensionKey)){
				pmfValues.put(dimensionKey, new Complex[N]);
				for(int k=0;k<N;k++){
					pmfValues.get(dimensionKey)[k]=new Complex(0.0, 0.0);
				}
				//System.out.println("**"+(((BigDecimal.valueOf(Double.valueOf((String)measure)))).intValue()-MIN+1));
				//System.out.println("**"+pmfValues.get(dimensionKey).length);
				pmfValues.get(dimensionKey)[((BigDecimal.valueOf(Double.valueOf((String)measure)))).intValue()-MIN+1]=pmfValues.get(dimensionKey)[((BigDecimal.valueOf(Double.valueOf((String)measure)))).intValue()-MIN+1].add(p);		
			}else {
				pmfValues.get(dimensionKey)[((BigDecimal.valueOf(Double.valueOf((String)measure)))).intValue()-MIN+1]=pmfValues.get(dimensionKey)[((BigDecimal.valueOf(Double.valueOf((String)measure)))).intValue()-MIN+1].add(p);
			}
	
		}
		//printPmf();
	}

	private int getMeasuresMin() {
		// TODO Auto-generated method stub
		int min=Integer.MAX_VALUE;
		for(Iterator<String> iter=this.rowValues.iterator();iter.hasNext();){
			String row=iter.next();
			BigDecimal bd=new BigDecimal((String)ConvertBetweenStringArray.convert(row)[inMemoryDataStore.getColumnIndex("X")]);
			int measure=bd.intValue();
			//int measure=(Integer) row[this.getColumnIndex().get("X")];
			if(min>measure){
				min=measure;
			}
		}

		return min;
	}

	public void printPmf(){
		for(String key:this.pmfValues.keySet()){
			System.out.println("pmfValues.get(key).length:"+pmfValues.get(key).length);
			for(int i=0;i<pmfValues.get(key).length;i++){
				System.out.println(pmfValues.get(key)[i].getReal());
			}
		}
	}
}
