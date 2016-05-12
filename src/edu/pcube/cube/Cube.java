package edu.pcube.cube;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.util.CuboidSerializable;

public class Cube {

	private HashMap<Integer,ArrayList<File>> cuboids;
	private Dimensions[] baseCellDimension;
	private File baseCuboid=null;
	private HashMap<File, ArrayList<File>> ancestor;

	private ArrayList<File> builtCuboids;
	private ArrayList<File> unbuiltCuboids;

	public Cube(InMemoryDataStore<?> inMemoryDataStore) {
		this.baseCellDimension=inMemoryDataStore.setBaseDimensions();
		this.cuboids= new HashMap<Integer, ArrayList<File>>();
		this.ancestor= new HashMap<File, ArrayList<File>>();
		this.builtCuboids=new ArrayList<File>();
		this.unbuiltCuboids=new ArrayList<File>();

		File file=new File("cube/");
		if(file.isDirectory()){
			file.delete();
		}
	}

	public Cube(InMemoryDataStore<?> inMemoryDataStore,boolean isCount) {
		this.baseCellDimension=inMemoryDataStore.setBaseDimensions();
		this.cuboids= new HashMap<Integer, ArrayList<File>>();
		this.ancestor= new HashMap<File, ArrayList<File>>();
		this.builtCuboids=new ArrayList<File>();
		this.unbuiltCuboids=new ArrayList<File>();

		File file=new File("cubeCount/");
		if(file.isDirectory()){
			//file.delete();
		}
	}

	public ArrayList<File> getBuiltCuboids() {
		return builtCuboids;
	}

	public ArrayList<File> getUnbuiltCuboids() {
		return unbuiltCuboids;
	}

	public HashMap<Integer, ArrayList<File>> getCuboids() {
		return this.cuboids;
	}

	public ArrayList<File> getCuboids(int dlevel) {
		return this.cuboids.get(dlevel);
	}

	public HashMap<File, ArrayList<File>> getAncestor() {
		return this.ancestor;
	}

	public ArrayList<File> getAncestor(File cuboid){
		return this.ancestor.get(cuboid);
	}

	public void setBaseCuboid(){
		int depth=this.baseCellDimension.length;
		System.out.println("depth:"+depth);

		for(int d:this.cuboids.keySet()){
			System.out.println(d+":"+this.cuboids.get(d).size());
		}

		if(this.cuboids.size()!=0){
			if(this.cuboids.containsKey(depth)){
				ArrayList<Cuboid> cuboidsList=CuboidSerializable.convertFileToCuboid(this.cuboids.get(depth));
				Iterator<Cuboid> iterator=cuboidsList.iterator();

				Cuboid<?> baseCuboid=iterator.next();

				while(iterator.hasNext()){
					Cuboid<?> tmpCuboid=iterator.next();
					if(tmpCuboid.isGreaterThen(baseCuboid)){
						baseCuboid=tmpCuboid;
					}
				}
				this.baseCuboid=CuboidSerializable.convertCuboidToFile(baseCuboid);
			}
		}
	}

	public File getBaseCuboid(){
		if(this.baseCuboid==null){
			this.setBaseCuboid();
		}
		return this.baseCuboid;
	}

	public Cuboid<?> getCuboid(Dimensions[] dimensions){

		String fileName="";

		for(int i=0;i<dimensions.length;i++){
			fileName+=dimensions[i].getDimensionName();
			fileName+=dimensions[i].getPath().length;
		}

		File file=new File("cubeCount/"+fileName);
		return CuboidSerializable.convertFileToCuboid(file);
	}

	public ArrayList<File> getNextBuiltCuboid(){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取cellCount、truncateObejct等信息；
		ObjectInputStream in;
		CellCount cellCount = null;
		ObjectStatistic objectStatistic = null;
		TruncateObjectCount truncateObjectCount = null;
		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double minOverhead=Double.MAX_VALUE;


		File choosedBuiltCuboidFile=null;
		File choosedUnBuiltCuboidFile=null;

		for(File builtCuboidFile:this.builtCuboids){
			for(File unbuiltCuboidFile:this.unbuiltCuboids){
				if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				

					//IO代价
					double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
					int avgPmflength=10*cellCount.getCuboidCellCount(this.getBaseCuboid())/cellCount.getCuboidCellCount(unbuiltCuboidFile);

					//sub-cell的代价
					double subCellCount=cellCount.getCuboidCellCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					double computeOverhead=avgPmflength*subCellCount*subCellCount*Math.log(avgPmflength*subCellCount);

					//quarantine 中的非truncateobject的数目

					double untruncatedObjectCount=truncateObjectCount.getTruncateObjectCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*untruncatedObjectCount*untruncatedObjectCount*Math.log(avgPmflength*untruncatedObjectCount);

					//quarantine 中的object数目

					double objectCount=truncateObjectCount.getTruncateObjectCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*objectCount*objectCount*Math.log(avgPmflength*objectCount);

					double A=0.032;
					double B=0.00000037;
					double totalOverhead=A*ioOverhead+B*computeOverhead;

					if(totalOverhead<minOverhead){
						minOverhead=totalOverhead;
						choosedUnBuiltCuboidFile=unbuiltCuboidFile;
						choosedBuiltCuboidFile=builtCuboidFile;
					}
				}
			}
		}

		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		ArrayList<File> result=new ArrayList<>();
		result.add(choosedUnBuiltCuboidFile);
		result.add(choosedBuiltCuboidFile);		

		return result;
	}

	public File getNextBuiltCuboid(File unbuiltCuboidFile){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取cellCount、truncateObejct等信息；
		ObjectInputStream in;
		CellCount cellCount = null;
		ObjectStatistic objectStatistic = null;
		TruncateObjectCount truncateObjectCount = null;
		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double minOverhead=Double.MAX_VALUE;


		File choosedBuiltCuboidFile=null;
		File choosedUnBuiltCuboidFile=null;

		for(File builtCuboidFile:this.builtCuboids){
			if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				

				//IO代价
				double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
				int avgPmflength=10*cellCount.getCuboidCellCount(this.getBaseCuboid())/cellCount.getCuboidCellCount(unbuiltCuboidFile);

				//sub-cell的代价
				double subCellCount=cellCount.getCuboidCellCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
				double computeOverhead=avgPmflength*subCellCount*subCellCount*Math.log(avgPmflength*subCellCount);

				//quarantine 中的非truncateobject的数目

				double untruncatedObjectCount=truncateObjectCount.getTruncateObjectCount(unbuiltCuboidFile);
				computeOverhead+=avgPmflength*untruncatedObjectCount*untruncatedObjectCount*Math.log(avgPmflength*untruncatedObjectCount);

				//quarantine 中的object数目

				double objectCount=truncateObjectCount.getTruncateObjectCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
				computeOverhead+=avgPmflength*objectCount*objectCount*Math.log(avgPmflength*objectCount);

				double A=0.032;
				double B=0.00000037;
				double totalOverhead=A*ioOverhead+B*computeOverhead;

				if(totalOverhead<minOverhead){
					minOverhead=totalOverhead;
					choosedUnBuiltCuboidFile=unbuiltCuboidFile;
					choosedBuiltCuboidFile=builtCuboidFile;
				}
			}
		}

		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		return choosedBuiltCuboidFile;
	}


	public double getNextBuiltCuboidReturnEstimatedTime(){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取cellCount、truncateObejct等信息；
		ObjectInputStream in;
		CellCount cellCount = null;
		ObjectStatistic objectStatistic = null;
		TruncateObjectCount truncateObjectCount = null;
		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double minOverhead=Double.MAX_VALUE;


		File choosedBuiltCuboidFile=null;
		File choosedUnBuiltCuboidFile=null;

		for(File builtCuboidFile:this.builtCuboids){
			for(File unbuiltCuboidFile:this.unbuiltCuboids){
				if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				

					//IO代价
					double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
					int avgPmflength=10*cellCount.getCuboidCellCount(this.getBaseCuboid())/cellCount.getCuboidCellCount(unbuiltCuboidFile);

					//sub-cell的代价
					double subCellCount=cellCount.getCuboidCellCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					double computeOverhead=avgPmflength*subCellCount*subCellCount*Math.log(avgPmflength*subCellCount);

					//quarantine 中的非truncateobject的数目

					double untruncatedObjectCount=truncateObjectCount.getTruncateObjectCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*untruncatedObjectCount*untruncatedObjectCount*Math.log(avgPmflength*untruncatedObjectCount);

					//quarantine 中的object数目

					double objectCount=truncateObjectCount.getTruncateObjectCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*objectCount*objectCount*Math.log(avgPmflength*objectCount);

					double A=0.032;
					double B=0.00000037;
					double totalOverhead=A*ioOverhead+B*computeOverhead;

					if(totalOverhead<minOverhead){
						minOverhead=totalOverhead;
						choosedUnBuiltCuboidFile=unbuiltCuboidFile;
						choosedBuiltCuboidFile=builtCuboidFile;
					}
				}
			}
		}

		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		ArrayList<File> result=new ArrayList<>();
		result.add(choosedUnBuiltCuboidFile);
		result.add(choosedBuiltCuboidFile);		

		return minOverhead;
	}

	public ArrayList<File> getNextBuiltCuboidbasedOnIO(){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取MaterializedSize；
		ObjectInputStream in;

		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double minOverhead=Double.MAX_VALUE;

		File choosedBuiltCuboidFile=null;
		File choosedUnBuiltCuboidFile=null;

		for(File builtCuboidFile:this.builtCuboids){
			for(File unbuiltCuboidFile:this.unbuiltCuboids){
				if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				

					//IO代价
					double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);

					double A=0.032;
					double totalOverhead=A*ioOverhead;

					if(totalOverhead<minOverhead){
						minOverhead=totalOverhead;
						choosedUnBuiltCuboidFile=unbuiltCuboidFile;
						choosedBuiltCuboidFile=builtCuboidFile;
					}
				}
			}
		}

		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		ArrayList<File> result=new ArrayList<>();
		result.add(choosedUnBuiltCuboidFile);
		result.add(choosedBuiltCuboidFile);		

		return result;
	}

	public ArrayList<File> getNextBuiltCuboidBasedOnRandom(){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取cellCount；
		ObjectInputStream in;
		CellCount cellCount = null;
		ObjectStatistic objectStatistic = null;
		TruncateObjectCount truncateObjectCount = null;
		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		ConcurrentSkipListSet<Double> costSet=new ConcurrentSkipListSet<>();

		for(File builtCuboidFile:this.builtCuboids){
			for(File unbuiltCuboidFile:this.unbuiltCuboids){

				if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				
					double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
					costSet.add(ioOverhead);
				}
			}
		}

		double overheadBound=Double.MAX_VALUE;

		int index=0;
		for(Iterator<Double> iterator=costSet.iterator();iterator.hasNext();){
			overheadBound=iterator.next();
			index++;
			if(index>5){
				break;
			}
		}



		File choosedBuiltCuboidFile=null;
		File choosedUnBuiltCuboidFile=null;


		HashMap<File, File> files=new HashMap<>();

		for(File builtCuboidFile:this.builtCuboids){
			for(File unbuiltCuboidFile:this.unbuiltCuboids){

				if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				
					//计算benefit；
					//IO代价
					double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
					int avgPmflength=10*cellCount.getCuboidCellCount(this.getBaseCuboid())/cellCount.getCuboidCellCount(unbuiltCuboidFile);

					//sub-cell的代价
					double subCellCount=cellCount.getCuboidCellCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					double computeOverhead=avgPmflength*subCellCount*subCellCount*Math.log(avgPmflength*subCellCount);

					//quarantine 中的非truncateobject的数目

					double untruncatedObjectCount=truncateObjectCount.getTruncateObjectCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*untruncatedObjectCount*untruncatedObjectCount*Math.log(avgPmflength*untruncatedObjectCount);

					//quarantine 中的object数目

					double objectCount=truncateObjectCount.getTruncateObjectCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
					computeOverhead+=avgPmflength*objectCount*objectCount*Math.log(avgPmflength*objectCount);

					double A=0.032;
					double B=0.00000037;
					double totalOverhead=A*ioOverhead+B*computeOverhead;

					if(totalOverhead<overheadBound){
						files.put(builtCuboidFile, unbuiltCuboidFile);
					}
				}
			}
		}

		Random random=new Random();
		index=random.nextInt(files.size());

		int num=0;
		for(File builtCuboidFile:files.keySet()){
			if(num==index){
				choosedBuiltCuboidFile=builtCuboidFile;
				choosedUnBuiltCuboidFile=files.get(choosedBuiltCuboidFile);
				break;
			}
			num++;
		}


		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		ArrayList<File> result=new ArrayList<>();
		result.add(choosedUnBuiltCuboidFile);
		result.add(choosedBuiltCuboidFile);

		return result;
	}

	public File getNextBuiltCuboidBasedOnRandom(File unbuiltCuboidFile){

		if(this.builtCuboids.size()==0){
			this.builtCuboids.add(this.getBaseCuboid());
			this.unbuiltCuboids.remove(this.getBaseCuboid());
		}

		//读取cellCount；
		ObjectInputStream in;
		CellCount cellCount = null;
		ObjectStatistic objectStatistic = null;
		TruncateObjectCount truncateObjectCount = null;
		MaterializedSize materializedSize = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/truncateObjectCount")));
			truncateObjectCount=(TruncateObjectCount) in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/materializedSize")));
			materializedSize=(MaterializedSize) in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ConcurrentSkipListSet<Double> costSet=new ConcurrentSkipListSet<>();

		for(File builtCuboidFile:this.builtCuboids){

			if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				
				double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
				costSet.add(ioOverhead);
			}
		}

		double overheadBound=Double.MAX_VALUE;

		int index=0;
		for(Iterator<Double> iterator=costSet.iterator();iterator.hasNext();){
			overheadBound=iterator.next();
			index++;
			if(index>5){
				break;
			}
		}

		File choosedUnBuiltCuboidFile=null;
		File choosedBuiltCuboidFile=null;

		HashMap<File, File> files=new HashMap<>();

		for(File builtCuboidFile:this.builtCuboids){

			if(this.getAncestor(builtCuboidFile).contains(unbuiltCuboidFile)){				

				//IO代价
				double ioOverhead=materializedSize.getMaterializedSizeMap(unbuiltCuboidFile)+materializedSize.getMaterializedSizeMap(builtCuboidFile);
				int avgPmflength=10*cellCount.getCuboidCellCount(this.getBaseCuboid())/cellCount.getCuboidCellCount(unbuiltCuboidFile);

				//sub-cell的代价
				double subCellCount=cellCount.getCuboidCellCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
				double computeOverhead=avgPmflength*subCellCount*subCellCount*Math.log(avgPmflength*subCellCount);

				//quarantine 中的非truncateobject的数目

				double untruncatedObjectCount=truncateObjectCount.getTruncateObjectCount(unbuiltCuboidFile);
				computeOverhead+=avgPmflength*untruncatedObjectCount*untruncatedObjectCount*Math.log(avgPmflength*untruncatedObjectCount);

				//quarantine 中的object数目

				double objectCount=truncateObjectCount.getTruncateObjectCount(builtCuboidFile)/cellCount.getCuboidCellCount(unbuiltCuboidFile);
				computeOverhead+=avgPmflength*objectCount*objectCount*Math.log(avgPmflength*objectCount);

				double A=0.032;
				double B=0.00000037;
				double totalOverhead=A*ioOverhead+B*computeOverhead;

				if(totalOverhead<overheadBound){
					files.put(builtCuboidFile, unbuiltCuboidFile);
				}
			}

		}

		Random random=new Random();
		index=random.nextInt(files.size());

		int num=0;
		for(File builtCuboidFile:files.keySet()){
			if(num==index){
				choosedBuiltCuboidFile=builtCuboidFile;
				choosedUnBuiltCuboidFile=files.get(choosedBuiltCuboidFile);
				break;
			}
			num++;
		}


		this.builtCuboids.add(choosedUnBuiltCuboidFile);
		this.unbuiltCuboids.remove(choosedUnBuiltCuboidFile);

		return choosedBuiltCuboidFile;
	}

	public TruncateObjectCount estimateTruncate(TruncateObjectCount truncateObjectCount){

		ObjectInputStream in = null;

		ObjectStatistic objectStatistic = null;
		CellCount cellCount = null;
		try {
			in = new ObjectInputStream(new FileInputStream(new File("cube/cellCount")));
			cellCount=(CellCount)in.readObject();
			in = new ObjectInputStream(new FileInputStream(new File("cube/objectCount")));
			objectStatistic=(ObjectStatistic)in.readObject();

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<File, Double> truncateBenefilt=new HashMap<>();
		for(File unbuiltCuboidFile:this.unbuiltCuboids){

			Cuboid<?> builtCuboid=CuboidSerializable.convertFileToCuboid(unbuiltCuboidFile);
			Dimensions[] dimensions=builtCuboid.getDimensions();
			int totallength=0;

			ArrayList<Integer> indexList=new ArrayList<>();
			for(int i=0;i<dimensions.length;i++){
				totallength+=dimensions[i].getPath().length;
				int index=0;
				if(dimensions[i].getDimensionName().equals("O")){
					index=2;
				}else if(dimensions[i].getDimensionName().equals("S")) {
					index=5;
				}else if (dimensions[i].getDimensionName().equals("C")) {
					index=7;
				}

				indexList.add(dimensions[i].getPath().length-1+index);
			}

			double a=1.0;
			double b=1.0;
			double c=1.0;
			double d=1.0;
			double e=1.0;
			for(int index:indexList){
				a*=(1.0+objectStatistic.getU()[index]);
				if(objectStatistic.getU()[index]<1.0){
					b*=(1.0-objectStatistic.getU()[index]);
				}else {
					b*=0.0;
				}
				c*=objectStatistic.getDomain()[index];
			}
			e=(double)objectStatistic.getObjectNum();
			d=e/c;

			double truncateObjectNum=d*cellCount.getCuboidCellCount(unbuiltCuboidFile)*(a-b);
			truncateBenefilt.put(unbuiltCuboidFile, truncateObjectNum);
			truncateObjectCount.getEstimatedObjectCount().put(unbuiltCuboidFile, truncateObjectNum);
		}

		File file=new File("cube/truncateObjectCount");
		if(file.exists()){
			file.delete();
		}

		ObjectOutputStream out;
		try {
			file.createNewFile();
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
		return truncateObjectCount;
	}

	public void createLattice(Dimensions[] dimensions) throws Exception {
		ArrayList<Cuboid> cuboids=new ArrayList<Cuboid>();

		System.out.println("+++++++++ base dimensions:+++++++++++");
		for(int i=0;i<dimensions.length;i++){
			for(int j=0;j<dimensions[i].getPath().length;j++){
				System.out.println(dimensions[i].getDimensionName()+":"+dimensions[i].getPath(j));
			}
		}

		int maxDimensionsCount=dimensions.length;

		int a[]=new int[dimensions.length];
		for(int i=0;i<dimensions.length;i++){
			a[i]=i;
		}

		this.cuboids.put(0, new ArrayList<File>());
		this.cuboids.get(0).add(CuboidSerializable.convertCuboidToFile(new Cuboid<Object>(this)));

		for(int d=1;d<=maxDimensionsCount;d++){
			this.cuboids.put(d, new ArrayList<File>());

			List<int[]> result=new ArrayList<int[]>();
			if(d!=a.length){
				result=combine(a, d);
			}else {
				result.add(a);
			}

			for(int i=0;i<result.size();i++){
				int[] b = (int[])result.get(i);

				ArrayList<Dimensions> d_Dimensions=new ArrayList<Dimensions>();
				for(int j=0;j<b.length;j++){
					d_Dimensions.add(dimensions[b[j]]);
				}

				ArrayList<Dimensions[]> paths=generatePermutations(d_Dimensions);
				for(Iterator<Dimensions[]> iterator=paths.iterator();iterator.hasNext();){
					Cuboid<?> cuboid=new Cuboid<Object>(this, iterator.next());
					File cuboidFile=CuboidSerializable.convertCuboidToFile(cuboid);
					this.cuboids.get(d).add(cuboidFile);
					this.unbuiltCuboids.add(cuboidFile);
				}
			}
		}

		for(int d:this.cuboids.keySet()){
			ArrayList<Cuboid> dCuboids=CuboidSerializable.convertFileToCuboid(this.cuboids.get(d));
			for(Iterator<Cuboid> cuboidIterator=dCuboids.iterator();cuboidIterator.hasNext();){
				Cuboid<?> cuboid=cuboidIterator.next();
				this.generateAncestors(cuboid, d);
			}
		}
	}


	private void generateAncestors(Cuboid<?> cuboid,int cur_d) throws Exception{

		File cuboidFile=CuboidSerializable.convertCuboidToFile(cuboid);
		this.ancestor.put(cuboidFile, new ArrayList<File>());

		for(int anc_d=1;anc_d<=cur_d;anc_d++){
			if(this.cuboids.get(anc_d).size()!=0){
				ArrayList<Cuboid> dCuboids=CuboidSerializable.convertFileToCuboid(this.cuboids.get(anc_d));
				for(Iterator<Cuboid> cuboidIterator=dCuboids.iterator();cuboidIterator.hasNext();){
					Cuboid<?> cuboidAncestor=cuboidIterator.next();

					if(!cuboid.equals(cuboidAncestor) && contains(cuboidAncestor.getDimensions(), cuboid.getDimensions())){
						this.ancestor.get(cuboidFile).add(CuboidSerializable.convertCuboidToFile(cuboidAncestor));
					}
				}
			}
		}
	}

	private static boolean contains(Dimensions[] a,Dimensions[] b){
		for(int i=0;i<a.length;i++){
			boolean flag=false;
			for(int j=0;j<b.length;j++){
				if(b[j].getDimensionName().equals(a[i].getDimensionName()) && b[j].getPath().length>=a[i].getPath().length){
					flag=true;
				}
			}
			if(!flag){
				return false;
			}
		}
		return true;
	}


	public Dimensions[] getBaseCellDimension() {
		return baseCellDimension;
	}

	private ArrayList<Dimensions[]> generatePermutations(ArrayList<Dimensions> d_Dimensions){
		if(d_Dimensions.size()==1){
			Dimensions dimensions=d_Dimensions.get(0);
			ArrayList<Dimensions[]> paths=new ArrayList<Dimensions[]>();
			String name=dimensions.getDimensionName();

			for(int i=0;i<dimensions.getPath().length;i++){
				paths.add(new Dimensions[1]);
			}

			Object[] srcPath=dimensions.getPath();

			for(int i=0;i<dimensions.getPath().length;i++){
				Object[] path=new Object[i+1];
				System.arraycopy(srcPath, 0, path, 0, i+1);

				paths.get(i)[0]=new Dimensions(name, path);
			}
			return paths;
		}else{
			//System.out.println("+++++"+d_Dimensions.size());
			int depth=d_Dimensions.size();
			ArrayList<Dimensions[]> paths=new ArrayList<Dimensions[]>();
			int sumlength=d_Dimensions.get(0).getPath().length*d_Dimensions.get(1).getPath().length;
			for(int i=0;i<sumlength;i++){
				paths.add(new Dimensions[2]);
			}

			String nameI=d_Dimensions.get(0).getDimensionName();
			String nameJ=d_Dimensions.get(1).getDimensionName();
			Object[] srcPathI=d_Dimensions.get(0).getPath();
			Object[] srcPathJ=d_Dimensions.get(1).getPath();
			for(int i=0;i<srcPathI.length;i++){
				for(int j=0;j<srcPathJ.length;j++){
					Object[] pathI=new Object[i+1];
					Object[] pathJ=new Object[j+1];
					System.arraycopy(srcPathI, 0, pathI, 0, i+1);
					System.arraycopy(srcPathJ, 0, pathJ, 0, j+1);

					int index=i*d_Dimensions.get(1).getPath().length+j;
					paths.get(index)[0]=new Dimensions(nameI, pathI);
					paths.get(index)[1]=new Dimensions(nameJ, pathJ);
				}
			}
			//System.out.println("+++++"+d_Dimensions.size());
			for(int i=2;i<d_Dimensions.size();i++){
				//System.out.println("*******"+d_Dimensions.size());
				Dimensions dimensions=d_Dimensions.get(i);
				//System.out.println("**********");
				paths=generatePermutations(paths, dimensions);
			}
			return paths;
		}
	}

	private ArrayList<Dimensions[]> generatePermutations(ArrayList<Dimensions[]> paths, Dimensions dimension){
		ArrayList<Dimensions[]> combinepaths=new ArrayList<Dimensions[]>();
		int depth=paths.get(0).length+1;
		Object[] srcPath=dimension.getPath();

		//System.out.println("paths.size():"+paths.size());
		//System.out.println("dimension.getPath().length:"+dimension.getPath().length);
		for(int i=0;i<paths.size();i++){
			for(int j=0;j<dimension.getPath().length;j++){
				combinepaths.add(new Dimensions[depth]);
				System.arraycopy(paths.get(i), 0, combinepaths.get(i*dimension.getPath().length+j), 0, paths.get(i).length);
			}
		}
		for(int i=0;i<paths.size();i++){
			for(int j=0;j<dimension.getPath().length;j++){
				Object[] path=new Object[j+1];
				System.arraycopy(srcPath, 0, path, 0, j+1);
				combinepaths.get(i*dimension.getPath().length+j)[depth-1]=new Dimensions(dimension.getDimensionName(), path);
			}
		}

		return combinepaths;
	}

	private List<int[]> combine(int[] a,int m){ 
		int n = a.length;

		List<int[]> result = new ArrayList<int[]>();

		int[] bs = new int[n];
		for(int i=0;i<n;i++){
			bs[i]=0;
		}
		//初始化
		for(int i=0;i<m;i++){
			bs[i]=1;
		}
		boolean flag = true;
		boolean tempFlag = false;
		int pos = 0;
		int sum = 0;
		//首先找到第一个10组合，然后变成01，同时将左边所有的1移动到数组的最左边
		do{
			sum = 0;
			pos = 0;
			tempFlag = true; 
			result.add(print(bs,a,m));

			for(int i=0;i<n-1;i++){
				if(bs[i]==1 && bs[i+1]==0 ){
					bs[i]=0;
					bs[i+1]=1;
					pos = i;
					break;
				}
			}
			//将左边的1全部移动到数组的最左边

			for(int i=0;i<pos;i++){
				if(bs[i]==1){
					sum++;
				}
			}
			for(int i=0;i<pos;i++){
				if(i<sum){
					bs[i]=1;
				}else{
					bs[i]=0;
				}
			}

			//检查是否所有的1都移动到了最右边
			for(int i= n-m;i<n;i++){
				if(bs[i]==0){
					tempFlag = false;
					break;
				}
			}
			if(tempFlag==false){
				flag = true;
			}else{
				flag = false;
			}

		}while(flag);
		result.add(print(bs,a,m));

		return result;
	}

	private int[] print(int[] bs,int[] a,int m){
		int[] result = new int[m];
		int pos= 0;
		for(int i=0;i<bs.length;i++){
			if(bs[i]==1){
				result[pos]=a[i];
				pos++;
			}
		}
		return result ;
	}

	private void print(List<?> l){
		for(int i=0;i<l.size();i++){
			int[] a = (int[])l.get(i);
			for(int j=0;j<a.length;j++){
				System.out.print(a[j]);
				System.out.print('\t');
			}
			System.out.println();
		}
	}

	public void printCuboid(Cuboid<?> cuboid){
		for(int i=0;i<cuboid.getDimensions().length;i++){
			System.out.print("//");
			cuboid.getDimensions(i).print();
		}
		System.out.println();
	}

	public void printAncestor(){
		System.out.println("++++++++++++++Ancestor+++++++++++");
		for(File cuboidFile:this.ancestor.keySet()){

			Cuboid<?> cuboid=CuboidSerializable.convertFileToCuboid(cuboidFile);

			if(cuboid.getDimensions()!=null){

				for(int i=0;i<cuboid.getDimensions().length;i++){
					System.out.print("//");
					cuboid.getDimensions(i).print();
				}
				ArrayList<Cuboid> ancestorCuboids=CuboidSerializable.convertFileToCuboid(this.ancestor.get(cuboidFile));
				for(Iterator<Cuboid> iterator=ancestorCuboids.iterator();iterator.hasNext();){
					Cuboid<?> ancestorCuboid=iterator.next();
					if(ancestorCuboid.getDimensions()!=null){
						System.out.print("+++++++++++++");
						for(int i=0;i<ancestorCuboid.getDimensions().length;i++){
							System.out.print("//");
							ancestorCuboid.getDimensions(i).print();
						}
					}
				}
				System.out.println();
				System.out.println();
			}
		}
	}
}
