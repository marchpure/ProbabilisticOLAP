package edu.pcube.cube;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class CellCount implements Serializable{

	private static final long serialVersionUID = -5809782578272943999L;
	
	private HashMap<File, Integer> cuboidCellCount=new HashMap<>();

	public HashMap<File, Integer> getCuboidCellCount() {
		return cuboidCellCount;
	}
	
	public int getCuboidCellCount(File file){
		return cuboidCellCount.get(file);
	}

	public void setCuboidCellCount(HashMap<File, Integer> cuboidCellCount) {
		this.cuboidCellCount = cuboidCellCount;
	}
}
