package edu.pcube.cube;

import java.io.Serializable;


public class Dimensions implements Serializable{

	private static final long serialVersionUID = -5809782578272943999L;

	public Object[] path;

	public String dimensionName;


	public Dimensions(String dimensionName,Object[] path) {
		super();
		this.path = path;
		this.dimensionName = dimensionName;
	}


	public Object[] getPath() {
		return path;
	}

	public Object getPath(int index){
		return path[index];
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public void print(){
		for(int i=0;i<path.length;i++){
			System.out.print(path[i]);
			System.out.print('\t');
		}
		System.out.println();
	}
}