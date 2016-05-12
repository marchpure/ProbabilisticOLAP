package edu.pcube.cube;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class MaterializedSize  implements Serializable {
	private static final long serialVersionUID = -5809782578272943999L;
	
	private HashMap<File, Long> materializedSizeMap=new HashMap<>();

	public HashMap<File, Long> getMaterializedSizeMap() {
		return materializedSizeMap;
	}
	
	public Long getMaterializedSizeMap(File file) {
		return this.materializedSizeMap.get(file);
	}

	public void setMaterializedSizeMap(HashMap<File, Long> materializedSizeMap) {
		this.materializedSizeMap = materializedSizeMap;
	}
}
