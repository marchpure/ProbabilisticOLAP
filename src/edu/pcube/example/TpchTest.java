package edu.pcube.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import edu.pcube.cube.CellCount;
import edu.pcube.cube.Cube;
import edu.pcube.cube.Cuboid;
import edu.pcube.cube.ObjectStatistic;
import edu.pcube.cube.TruncateObjectCount;
import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.factory.InMemoryDataStoreFactory;
import edu.pcube.util.CuboidSerializable;
import edu.pcube.util.DBaddress;
import edu.pcube.util.Data;
import edu.pcube.util.QueryProcessing;


public class TpchTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fig7_convolution(int[] objectCountArray){

		HashMap<Long, Long> fig7_convolution=new HashMap<Long, Long>();

		try {

			for(int M:objectCountArray){		
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				baseCuboid.buildBasedOnFFT(inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cube.getCuboids(3).get(0));
				File cuboidFile=cuboid.buildBasedOnCuboid(baseCuboid);

				long endTime=System.currentTimeMillis();

				long fileSize=getFileSize(cuboidFile);

				fig7_convolution.put((long) M, endTime-startTime);
				fig7_convolution.put((long) M+1, fileSize);
			}

			Data.writeExcel("Fig7-convolution", fig7_convolution);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fig7_sktech(int[] objectCountArray){

		HashMap<Long, Long> fig7_sktech=new HashMap<Long, Long>();

		try {

			for(int M:objectCountArray){		
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				baseCuboid.buildBasedOnSketch(inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cube.getCuboids(3).get(0));
				File cuboidFile=cuboid.buildBasedOnCuboidBySketch(baseCuboid);

				long endTime=System.currentTimeMillis();

				long fileSize=getFileSize(cuboidFile);

				fig7_sktech.put((long) M, endTime-startTime);
				fig7_sktech.put((long) M+1, fileSize);
			}

			Data.writeExcel("Fig7-sktech", fig7_sktech);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void fig7_pwd(int[] objectCountArray){

		HashMap<Long, Long> fig7_pwd=new HashMap<Long, Long>();

		try {

			for(int M:objectCountArray){		
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				baseCuboid.buildBasedOnFFT(inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cube.getCuboids(3).get(0));
				File cuboidFile=cuboid.buildBasedOnCuboidByPWD(cuboid);

				long endTime=System.currentTimeMillis();

				long fileSize=getFileSize(cuboidFile);

				fig7_pwd.put((long) M, endTime-startTime);
				fig7_pwd.put((long) M+1, fileSize);
			}

			Data.writeExcel("Fig7-pwd", fig7_pwd);
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

	public static void fig8_fullM_convolution(int[] objectCountArray){

		HashMap<Long, Long> fig8fullMconvolution=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				
				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
				}
				long endTime=System.currentTimeMillis();

				fig8fullMconvolution.put((long) M, endTime-startTime);
			}

			Data.writeExcel("fig8_fullM_convolution", fig8fullMconvolution);
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

	public static void fig8_fullM_sketch(int[] objectCountArray){

		HashMap<Long, Long> fig8fullMsketch=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);
				}
				long endTime=System.currentTimeMillis();

				fig8fullMsketch.put((long) M, endTime-startTime);
			}

			Data.writeExcel("fig8_fullM_sketch", fig8fullMsketch);
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

	public static void fig8_fullM_semidistributive(int[] objectCountArray){

		HashMap<Long, Long> fig8fullMsemidistributive=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				
				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboidByPWD(ancestorCuboid);
				}
				long endTime=System.currentTimeMillis();

				fig8fullMsemidistributive.put((long) M, endTime-startTime);
			}

			Data.writeExcel("fig8_fullM_semidistributive", fig8fullMsemidistributive);
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

	public static void fig8_fullM_costmodel_IO(int[] objectCountArray){

		HashMap<Long, Long> fig8fullMCostModelIO=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidbasedOnIO();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
				}
				long endTime=System.currentTimeMillis();

				fig8fullMCostModelIO.put((long) M, endTime-startTime);
			}

			Data.writeExcel("fig8fullMCostModelIO", fig8fullMCostModelIO);
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

	public static void fig8_fullM_costmodel_Random(int[] objectCountArray){

		HashMap<Long, Long> fig8fullMCostModelRandom=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidBasedOnRandom();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
				}
				long endTime=System.currentTimeMillis();

				fig8fullMCostModelRandom.put((long) M, endTime-startTime);
			}

			Data.writeExcel("fig8fullMCostModelRandom", fig8fullMCostModelRandom);
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

	public static void fig9_partialM_convolution(int[] objectCountArray){

		HashMap<Long, Long> fig9partialMconvolution=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				long mSize=0;

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				mSize+=getFileSize(baseCuboidFile);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidBasedOnRandom();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);

					long endTime=System.currentTimeMillis();
					mSize+=getFileSize(cuboidFile);

					fig9partialMconvolution.put(mSize, endTime-startTime);
				}
			}

			Data.writeExcel("fig9partialMconvolution", fig9partialMconvolution);
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

	public static void fig9_partialM_sketch(int[] objectCountArray){

		HashMap<Long, Long> fig9partialMsketch=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				long mSize=0;

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);
				mSize+=getFileSize(baseCuboidFile);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidBasedOnRandom();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);

					long endTime=System.currentTimeMillis();
					mSize+=getFileSize(cuboidFile);

					fig9partialMsketch.put(mSize, endTime-startTime);
				}
			}

			Data.writeExcel("fig9partialMsketch", fig9partialMsketch);
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

	public static void fig9_partialM_costmodel_Random(int[] objectCountArray){

		HashMap<Long, Long> fig9partialMCostModelRandom=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				long mSize=0;

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				mSize+=getFileSize(baseCuboidFile);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidBasedOnRandom();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);

					long endTime=System.currentTimeMillis();
					mSize+=getFileSize(cuboidFile);

					fig9partialMCostModelRandom.put(mSize, endTime-startTime);
				}
			}

			Data.writeExcel("fig9partialMCostModelRandom", fig9partialMCostModelRandom);
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

	public static void fig9_partialM_costmodel_IO(int[] objectCountArray){

		HashMap<Long, Long> fig9partialMCostModelRandom=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				long mSize=0;

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				mSize+=getFileSize(baseCuboidFile);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				long startTime=System.currentTimeMillis();

				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboidbasedOnIO();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);

					long endTime=System.currentTimeMillis();
					mSize+=getFileSize(cuboidFile);

					fig9partialMCostModelRandom.put(mSize, endTime-startTime);
				}
			}

			Data.writeExcel("fig9partialMCostModelIO", fig9partialMCostModelRandom);
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

	public static void fig10_costmodel_accuracy(int[] objectCountArray){

		HashMap<Long, Long> fig10CostAccuracy=new HashMap<Long, Long>();

		try {
			for(int M:objectCountArray){	
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata_v2",M);
				//System.out.println("-----------------");
				//inMemoryDataStore.print();

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				objectStatistics(inMemoryDataStore);
				cellCountStatistics(cube,inMemoryDataStore);

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());
				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);

				truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);

				int num=0;				
				while(cube.getUnbuiltCuboids().size()!=0){

					double time=cube.getNextBuiltCuboidReturnEstimatedTime();
					num++;

					fig10CostAccuracy.put((long)num, (long) time);
				}
			}

			Data.writeExcel("fig9CostAccuracy", fig10CostAccuracy);
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


	public static <V> void fig11_psq_costmodel(int startThreshold,int endThreshold,int totalMSize){

		HashMap<Long, Long> fig11PsqCostmodel=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}


				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){


					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//待利用
				double totalConstructTime=0.0;
				double totalQueryTime=0.0;
				long startTime=System.currentTimeMillis();

				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){
					File cuboidFile=cuboidIterator.next();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){
							cuboid.buildBasedOnCuboid(CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboid(cuboidFile)));
						}
					}
					ArrayList<V[]> bounds=cuboid.generateRandomQueryRegion();
					cuboid.psdBasedOnConvolution(bounds.get(0), bounds.get(1));
				}
				long endTime=System.currentTimeMillis();

				fig11PsqCostmodel.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("fig11PsqCostmodel", fig11PsqCostmodel);

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

	public static <V> void fig11_psq_costmodel_random(int startThreshold,int endThreshold,int totalMSize){

		HashMap<Long, Long> fig11PsqCostmodelRandom=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}


				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){


					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//待利用
				double totalConstructTime=0.0;
				double totalQueryTime=0.0;
				long startTime=System.currentTimeMillis();

				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){
					File cuboidFile=cuboidIterator.next();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){
							cuboid.buildBasedOnCuboidWithoutPersist(CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboidBasedOnRandom(cuboidFile)));
						}
					}
					ArrayList<V[]> bounds=cuboid.generateRandomQueryRegion();
					cuboid.psdBasedOnConvolution(bounds.get(0), bounds.get(1));
				}
				long endTime=System.currentTimeMillis();

				fig11PsqCostmodelRandom.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("fig11PsqCostmodelRandom", fig11PsqCostmodelRandom);

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

	public static <V> void fig11_psq_sketch(int startThreshold,int endThreshold,int totalMSize){

		HashMap<Long, Long> fig11PsqSketch=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}


				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){


					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//待利用
				double totalConstructTime=0.0;
				double totalQueryTime=0.0;
				long startTime=System.currentTimeMillis();

				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){
					File cuboidFile=cuboidIterator.next();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){

							cuboid.buildBasedOnCuboidBySketchWithoutPersist(CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboid(cuboidFile)));
						}
					}

					ArrayList<V[]> bounds=cuboid.generateRandomQueryRegion();
					cuboid.psdBasedOnSketch(bounds.get(0), bounds.get(1));
				}
				long endTime=System.currentTimeMillis();

				fig11PsqSketch.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("fig11PsqSketch", fig11PsqSketch);

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

	public static <V> void fig12_pdq_convolution(int startThreshold,int endThreshold,int totalMSize){

		HashMap<Long, Long> fig12PdqConvolution=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				File baseCuboidFile=baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){


					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Random random=new Random();
				double totalConstructTime=0.0;

				long startTime=System.currentTimeMillis();

				Iterator<Double> tvIterator=queryProcessing.getTvList().iterator();
				Iterator<Double> tpIterator=queryProcessing.getTpList().iterator();
				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){

					double tv=tvIterator.next();
					double tp=tpIterator.next();
					File cuboidFile=cuboidIterator.next();

					
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){
							
							Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
							File ancestorCuboidFile=cube.getNextBuiltCuboid(cuboidFile);
							Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboid(cuboidFile));

							cuboid.buildBasedOnCuboid(ancestorCuboid);
							cuboid.pdqBasedOnConvolution(tv,tp);
						}else {
							Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
							cuboid.pdqBasedOnConvolution(tv,tp);
						}
					}			
				}
				
				long endTime=System.currentTimeMillis();
				
				fig12PdqConvolution.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("fig12PdqConvolution", fig12PdqConvolution);

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
	
	public static <V> void fig12_pdq_sketch(int startThreshold,int endThreshold,int totalMSize,int orderOfMoment){

		HashMap<Long, Long> fig12PdqSketch=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					File cuboidFile=cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				double totalConstructTime=0.0;

				long startTime=System.currentTimeMillis();

				Iterator<Double> tvIterator=queryProcessing.getTvList().iterator();
				Iterator<Double> tpIterator=queryProcessing.getTpList().iterator();
				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){

					double tv=tvIterator.next();
					double tp=tpIterator.next();
					File cuboidFile=cuboidIterator.next();

					
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){
							File ancestorCuboidFile=cube.getNextBuiltCuboid(cuboidFile);
							Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboid(cuboidFile));
							cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);					
						}
						
						if(orderOfMoment==2){
							cuboid.pdqBasedOnSketch(baseCuboid,tv,tp);
						}else if(orderOfMoment==1){
							cuboid.pdqBasedOnSketchwith1thMoment(baseCuboid,tv,tp);
						}else if(orderOfMoment==4){
							cuboid.pdqBasedOnSketchwith4thMoment(baseCuboid,tv,tp);
						}
					}			
				}	
				
				long endTime=System.currentTimeMillis();
				
				fig12PdqSketch.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("fig12PdqSketch", fig12PdqSketch);

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
	
	public static <V> void fig12_pdq_combined(int startThreshold,int endThreshold,int totalMSize){

		HashMap<Long, Long> fig12PdqCombined=new HashMap<Long, Long>();

		try {
			for(int Threshold=startThreshold;Threshold<=endThreshold;Threshold+=5){
				InMemoryDataStore<Object> inMemoryDataStore = InMemoryDataStoreFactory.fromMySQL(DBaddress.dbconnectString, "root", "root", "SELECT * FROM tpchdata", 5000);

				Cube cube=new Cube(inMemoryDataStore);
				cube.createLattice(inMemoryDataStore.setBaseDimensions());

				if(Threshold==startThreshold){
					objectStatistics(inMemoryDataStore);
					cellCountStatistics(cube,inMemoryDataStore);
				}

				Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid());

				baseCuboid.buildBasedOnFFT(inMemoryDataStore);
				File baseCuboidFile=baseCuboid.buildBasedOnSketch(inMemoryDataStore);
				ArrayList<Cuboid> cache=new ArrayList<>();
				cache.add(baseCuboid);

				if(Threshold==startThreshold){
					truncateObjectStatistics(baseCuboid,baseCuboidFile,baseCuboid.getTruncateObjectCount(),cube,inMemoryDataStore);
				}

				long mSize=getFileSize(baseCuboidFile);
				while(cube.getUnbuiltCuboids().size()!=0){
					ArrayList<File> list=cube.getNextBuiltCuboid();
					Cuboid cuboid=CuboidSerializable.convertFileToCuboid(list.get(0));
					Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(list.get(1));
					System.out.println("cuboid:"+list.get(0).getName());
					System.out.println("ancestorCuboid:"+list.get(1).getName());

					cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);
					File cuboidFile=cuboid.buildBasedOnCuboid(ancestorCuboid);
					cache.add(cuboid);
					mSize+=getFileSize(cuboidFile);

					if(mSize/totalMSize>=Threshold/100.0){
						break;
					}
				}

				ObjectInputStream in;
				QueryProcessing queryProcessing = null;

				try {
					in = new ObjectInputStream(new FileInputStream(new File("query/queryProcessing")));
					queryProcessing=(QueryProcessing)in.readObject();

					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Random random=new Random();
				double totalConstructTime=0.0;

				long startTime=System.currentTimeMillis();

				Iterator<Double> tvIterator=queryProcessing.getTvList().iterator();
				Iterator<Double> tpIterator=queryProcessing.getTpList().iterator();
				for(Iterator<File> cuboidIterator=queryProcessing.getTraces().iterator();cuboidIterator.hasNext();){

					double tv=tvIterator.next();
					double tp=tpIterator.next();
					File cuboidFile=cuboidIterator.next();

					
					if(!cuboidFile.getName().equals("null")&&!cuboidFile.getName().equals("P2O3S3C2")){
						if(cube.getUnbuiltCuboids().contains(cuboidFile)){
							
							Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
							File ancestorCuboidFile=cube.getNextBuiltCuboid(cuboidFile);
							Cuboid ancestorCuboid=CuboidSerializable.convertFileToCuboid(cube.getNextBuiltCuboid(cuboidFile));

							cuboid.buildBasedOnCuboidBySketch(ancestorCuboid);
							
							cuboid.pdqBasedOnSketch(ancestorCuboid, tv, tp);
						}else {
							Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
							cuboid.pdqBasedOnConvolution(tv,tp);
						}
					}			
				}			
				
				long endTime=System.currentTimeMillis();
				
				fig12PdqCombined.put((long) Threshold, endTime-startTime);
			}

			Data.writeExcel("pic2-1-103", fig12PdqCombined);

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

	@SuppressWarnings("resource")
	public static int getFileSize(File file){
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			return in.available();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return true;
	}

	public static void objectStatistics(InMemoryDataStore<Object> inMemoryDataStore){
		try {
			ObjectStatistic objectStatistic=new ObjectStatistic(inMemoryDataStore.getDimensions().length);
			objectStatistic.statistic(inMemoryDataStore);

			File file=new File("cube/objectCount");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();

			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(objectStatistic);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public static void truncateObjectStatistics(Cuboid baseCuboid,File baseCuboidFile,Integer toCount,Cube cube,InMemoryDataStore<Object> inMemoryDataStore){

		TruncateObjectCount truncateObjectCount=new TruncateObjectCount();

		try {
			truncateObjectCount.getTruncateObjectCount().put(baseCuboidFile, toCount);
			int num=0;
			for(File cuboidFile:cube.getAncestor(cube.getBaseCuboid())){
				Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
				if(num==106){
					System.out.println("###################");
					for(int i=0;i<cuboid.getDimensions().length;i++){
						System.out.println(cuboid.getDimensions(i).getPath().length);
					}
					System.out.println("###################");
				}else {
					truncateObjectCount.getTruncateObjectCount().put(cuboidFile, cuboid.getTruncateObjectCount(baseCuboid));		
				}
				//break;
				num++;
			}

			truncateObjectCount=cube.estimateTruncate(truncateObjectCount);

			File file=new File("cube/truncateObjectCount");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();

			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(truncateObjectCount);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static void decideParameterofTruncate(){

		ObjectInputStream in;
		TruncateObjectCount truncateObjectCount = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double sum=0.0;
		double fileNum=0.0;
		for(File fileKey:truncateObjectCount.getTimeOverhead().keySet()){
			long timeOverhead=truncateObjectCount.getTimeOverhead(fileKey);
			double estimatedTruncateObjectCount=truncateObjectCount.getEstimatedObjectCount(fileKey);
			if(estimatedTruncateObjectCount==0){

			}else {
				sum+=(double)timeOverhead/estimatedTruncateObjectCount;
			}

			System.out.println(timeOverhead);
			System.out.println(estimatedTruncateObjectCount);
			System.out.println((double)timeOverhead/estimatedTruncateObjectCount);
			fileNum+=1.0;
		}
		System.out.println(sum);
		System.out.println(fileNum);

		double avg=sum/fileNum;

		for(File fileKey:truncateObjectCount.getTimeOverhead().keySet()){
			long timeOverhead=truncateObjectCount.getTimeOverhead(fileKey);
			double estimatedTruncateObjectCount=truncateObjectCount.getEstimatedObjectCount(fileKey);
			System.out.println(estimatedTruncateObjectCount*avg+"-----------"+timeOverhead);
		}

		System.out.println(avg);
	}

	public static void cellCountStatistics(Cube cube,InMemoryDataStore inMemoryDataStore){

		CellCount cellCount=new CellCount();

		try {
			File baseCuboidFile=CuboidSerializable.convertFileToCuboid(cube.getBaseCuboid()).buildBasedOnFFT(inMemoryDataStore);

			Cuboid baseCuboid=CuboidSerializable.convertFileToCuboid(baseCuboidFile);
			int cc=baseCuboid.getCells().size();

			cellCount.getCuboidCellCount().put(baseCuboidFile, cc);

			int num=0;

			for(File cuboidFile:cube.getAncestor(cube.getBaseCuboid())){
				Cuboid cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);
				if(num==106){
					System.out.println("###################");
					for(int i=0;i<cuboid.getDimensions().length;i++){
						System.out.println(cuboid.getDimensions(i).getPath().length);
					}
					System.out.println("###################");
				}else {
					cellCount.getCuboidCellCount().put(cuboidFile, cuboid.getCellCount(baseCuboid));		
				}
				num++;
			}

			File file=new File("cube/cellCount");
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();

			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(cellCount);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
