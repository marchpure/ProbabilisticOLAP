package edu.pcube.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import edu.pcube.cube.Cuboid;

public class CuboidSerializable {

	public static ArrayList<Cuboid> convertFileToCuboid(ArrayList<File> fileList){

		ArrayList<Cuboid> cuboids=new ArrayList<Cuboid>();
		for(File file:fileList){
			cuboids.add(convertFileToCuboid(file));
		}
		return cuboids;
	}

	public static Cuboid convertFileToCuboid(File file){
		ObjectInputStream in;
		Cuboid cuboid = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			cuboid=(Cuboid)in.readObject();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cuboid;
	}

	public static File convertCuboidToFile(Cuboid cuboid){
		ObjectOutputStream out;
		File cuboidFile=createFile(cuboid);

		try {
			out = new ObjectOutputStream(new FileOutputStream(cuboidFile));
			out.writeObject(cuboid);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cuboidFile;
	}

	public static File createFile(Cuboid cuboid){

		String fileName="";

		if(cuboid.getDimensions()!=null){
			for(int i=0;i<cuboid.getDimensions().length;i++){
				fileName+=cuboid.getDimensions(i).getDimensionName();
				fileName+=cuboid.getDimensions(i).getPath().length;
			}
		}else {
			fileName="null";
		}

		File file=new File("cube/"+fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return file;
	}
}
