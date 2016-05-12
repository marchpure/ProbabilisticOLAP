package edu.pcube.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.pcube.cube.Cube;
import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.factory.InMemoryDataStoreFactory;

public class QueryProcessing implements Serializable {
	
	private static final long serialVersionUID = -5809782578272943999L;
	
	private ArrayList<File> traces=new ArrayList<>();

	private ArrayList<Double> tvList=new ArrayList<>();
	private ArrayList<Double> tpList=new ArrayList<>();
	
	public ArrayList<File> getTraces() {
		return traces;
	}
	
	public ArrayList<Double> getTvList() {
		return tvList;
	}

	public ArrayList<Double> getTpList() {
		return tpList;
	}

	public void testQuery(){
		try {
			//InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL("jdbc:mysql://192.168.1.113:3306/tpch", "root", "root", "SELECT * FROM tpchdata", 5000);
			InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM ocean", 5000);
			
			Cube cube=new Cube(inMemoryDataStore);
			cube.createLattice(inMemoryDataStore.setBaseDimensions());
			
			ArrayList<File> cuboids=new ArrayList<>();
			
			for(int level:cube.getCuboids().keySet()){
				for(File cuboid:cube.getCuboids(level)){
					cuboids.add(cuboid);
				}
			}
			
			int cuboidCount=cuboids.size();
			
			//generate query trace base on random
			Random random=new Random();
			for(int i=0;i<1000;i++){
				this.traces.add(cuboids.get(random.nextInt(cuboidCount)));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testQueryPDQ(){
		try {
			//InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL("jdbc:mysql://192.168.1.113:3306/tpch", "root", "root", "SELECT * FROM tpchdata", 5000);
			InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);
			
			Cube cube=new Cube(inMemoryDataStore);
			cube.createLattice(inMemoryDataStore.setBaseDimensions());
			
			ArrayList<File> cuboids=new ArrayList<>();

			
			for(int level:cube.getCuboids().keySet()){
				for(File cuboid:cube.getCuboids(level)){
					cuboids.add(cuboid);
				}
			}
			
			int cuboidCount=cuboids.size();
			
			//generate query trace base on random
			Random random=new Random();
			for(int i=0;i<1000;i++){
				//this.traces.add(cuboids.get(random.nextInt(cuboidCount)));
				this.traces.add(cuboids.get(i%cuboids.size()));
				this.tvList.add(random.nextDouble());
				this.tpList.add(random.nextDouble());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static <V> void main(String[] args) throws Exception {
		QueryProcessing queryProcessing=new QueryProcessing();
		queryProcessing.testQueryPDQ();

		File file=new File("query/queryProcessing2");
		if(file.exists()){
			file.delete();
		}
		file.createNewFile();

		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(queryProcessing);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
