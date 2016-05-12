package edu.pcube.datastore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.table.TableModel;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import edu.pcube.cube.Dimensions;
import edu.pcube.util.ConvertBetweenStringArray;

public class InMemoryDataStore<V> {
	private HashMap<Object,ObjectGroupedTuples<V>> objectGroupedTuples;
	private ArrayList<String> columnHeaders;
	private Dimensions[] dimensions;
	private String[] dimensionsStrings;
	private HashMap<String, Integer> columnIndex;

	public InMemoryDataStore() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InMemoryDataStore(TableModel tableModel) {

		setBaseDimensions();
		this.columnHeaders=new ArrayList<String>();
		this.objectGroupedTuples=new HashMap<Object, ObjectGroupedTuples<V>>();
		this.columnIndex=new HashMap<String, Integer>();

		for(int r = 0; r<tableModel.getRowCount();r++){
			V[] values = (V[]) new Object[tableModel.getColumnCount()-1];
			final Object objectId=tableModel.getValueAt(r, 0);			
			for(int c = 1; c < tableModel.getColumnCount(); c++) {
				final V valueAt = (V)tableModel.getValueAt(r, c);
				values[c-1]=valueAt;
			}

			if(!this.objectGroupedTuples.containsKey(objectId)){
				this.objectGroupedTuples.put(objectId, new ObjectGroupedTuples<V>(objectId,this));
			}
			objectGroupedTuples.get(objectId).getRowValues().add(ConvertBetweenStringArray.convert(values));
		}

		HashMap<String, Integer> columnIndex=new HashMap<String, Integer>();

		for(int c = 0; c < tableModel.getColumnCount(); c++) {
			System.out.println(tableModel.getColumnName(c));
			this.columnHeaders.add(tableModel.getColumnName(c));
			if(c!=0){
				columnIndex.put(tableModel.getColumnName(c), c-1);
			}
		}

		this.setColumnIndex(columnIndex);

		for(Object objectId:this.getObjectGroupedTuples().keySet()){
			this.getObjectTuplesbyObjectId(objectId).generatePMF();
		}
	}
	
	public InMemoryDataStore(TableModel tableModel, int M) {

		setBaseDimensions();
		this.columnHeaders=new ArrayList<String>();
		this.objectGroupedTuples=new HashMap<Object, ObjectGroupedTuples<V>>();
		this.columnIndex=new HashMap<String, Integer>();

		for(int r = 0; r<tableModel.getRowCount();r++){

			V[] values = (V[]) new Object[tableModel.getColumnCount()-1];
			final Object objectId=tableModel.getValueAt(r, 0);
			
			if(this.objectGroupedTuples.size()>M && !this.objectGroupedTuples.containsKey(objectId)){
				break;
			}
			
			for(int c = 1; c < tableModel.getColumnCount(); c++) {
				final V valueAt = (V)tableModel.getValueAt(r, c);
				values[c-1]=valueAt;
			}

			if(!this.objectGroupedTuples.containsKey(objectId)){
				this.objectGroupedTuples.put(objectId, new ObjectGroupedTuples<V>(objectId,this));
			}
			objectGroupedTuples.get(objectId).getRowValues().add(ConvertBetweenStringArray.convert(values));
		}

		HashMap<String, Integer> columnIndex=new HashMap<String, Integer>();

		for(int c = 0; c < tableModel.getColumnCount(); c++) {
			System.out.println(tableModel.getColumnName(c));
			this.columnHeaders.add(tableModel.getColumnName(c));
			if(c!=0){
				columnIndex.put(tableModel.getColumnName(c), c-1);
			}
		}

		this.setColumnIndex(columnIndex);

		for(Object objectId:this.getObjectGroupedTuples().keySet()){
			this.getObjectTuplesbyObjectId(objectId).generatePMF();
		}


	}

	public String[] getDimensions() {
		return dimensionsStrings;
	}

	public String getDimensions(int index) {
		return dimensionsStrings[index];
	}

	public void setDimensions(Dimensions[] dimensions) {
		int length=0;
		for(int i=0;i<dimensions.length;i++){
			length+=dimensions[i].getPath().length;
		}
		String[] ds=new String[length];
		int index=0;
		for(int i=0;i<dimensions.length;i++){
			for(int j=0;j<dimensions[i].getPath().length;j++){
				ds[index++]=(String) dimensions[i].getPath(j);
			}
		}
		this.dimensionsStrings= ds;
	}

	public HashMap<Object, ObjectGroupedTuples<V>> getObjectGroupedTuples() {
		return objectGroupedTuples;
	}

	public ObjectGroupedTuples<V> getObjectTuplesbyObjectId(Object objectId) {
		return objectGroupedTuples.get(objectId);
	}

	public ArrayList<String> getColumnHeaders() {
		return columnHeaders;
	}

	public Dimensions[] setBaseDimensionsOcean(){
		//int dimensionNum=darrays.length;
		this.dimensions=new Dimensions[4];


		for(int i=0;i<4;i++){
			if(i==0){
				Object path[]=new Object[3];

				path[0]="YEAR";
				path[1]="MONTH";
				path[2]="DAY";

				dimensions[0]=new Dimensions("Y", path);
			}else if(i==1){
				Object path[]=new Object[2];
				path[0]="DIR";
				path[1]="DIRLEVEL";

				dimensions[1]=new Dimensions("D", path);
			}else if(i==2){
				Object path[]=new Object[2];
				path[0]="SPD";
				path[1]="SPDLEVEL";

				dimensions[2]=new Dimensions("S", path);
			}else if(i==3){
				Object path[]=new Object[2];
				path[0]="TEMP";
				path[1]="TEMPLEVEL";

				dimensions[3]=new Dimensions("T", path);
			}
		}


		setDimensions(dimensions);

		return dimensions;
	}
	
	public Dimensions[] setBaseDimensions(){
		//int dimensionNum=darrays.length;
		this.dimensions=new Dimensions[4];
		for(int i=0;i<4;i++){
			if(i==0){
				Object path[]=new Object[2];
				path[0]="P_MFGR";
				path[1]="P_BRAND";

				dimensions[0]=new Dimensions("P", path);
			}else if(i==1){
				Object path[]=new Object[3];
				path[0]="O_ORDERYEAR";
				path[1]="O_ORDERMONTH";
				path[2]="O_ORDERDAY";

				dimensions[1]=new Dimensions("O", path);
			}else if(i==2){
				Object path[]=new Object[2];
				path[0]="S_REGIONNAME";
				path[1]="S_NATIONNAME";

				dimensions[2]=new Dimensions("S", path);
			}else if(i==3){
				Object path[]=new Object[2];
				path[0]="C_REGIONNAME";
				path[1]="C_NATIONNAME";

				dimensions[3]=new Dimensions("C", path);
			}
		}


		setDimensions(dimensions);

		return dimensions;
	}

	public HashMap<String, Integer> getColumnIndex() {
		return columnIndex;
	}

	public int getColumnIndex(String key){
		return columnIndex.get(key);
	}

	public String getColumnByIndex(int index){
		for(String key:this.columnIndex.keySet()){
			if(this.columnIndex.get(key)==index){
				return key;
			}
		}
		return "";
	}

	public void setColumnIndex(HashMap<String, Integer> columnIndex) {
		this.columnIndex = columnIndex;
	}


	private static String abbreviate(String str, int maxWidth) {
		if(str == null) {
			return null;
		}
		if (str.length() <= maxWidth) {
			return str;
		}
		return str.substring(0, maxWidth - 3) + "...";
	}


	public void print(){
		for(Iterator<ObjectGroupedTuples<V>> seriesIter=this.getObjectGroupedTuples().values().iterator();seriesIter.hasNext();){
			ObjectGroupedTuples<V> objectGroupedTuples=seriesIter.next();
			Object objectId=objectGroupedTuples.getObjectid();
			if(objectId instanceof Number) {
				System.out.printf("%20s", abbreviate(DecimalFormat.getNumberInstance().format(objectId), 19));
			} else {
				System.out.printf("%20s", abbreviate(objectId.toString(), 19));
			}

			for(Iterator<String> iter=objectGroupedTuples.getRowValues().iterator();iter.hasNext();){
				String row=iter.next();
				System.out.print(row);
				System.out.print('\t');
			}
			System.out.println();
		}
	}
}
