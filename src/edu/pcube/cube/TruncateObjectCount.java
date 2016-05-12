package edu.pcube.cube;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

public class TruncateObjectCount implements Serializable{

	private static final long serialVersionUID = -5809782578272943999L;
	
	private HashMap<File, Integer> truncateObjectCount=new HashMap<File, Integer>();
	private HashMap<File, Double> estimatedObjectCount=new HashMap<File, Double>();
	private HashMap<File, Long> timeOverhead=new HashMap<File, Long>();
	private HashMap<Integer, Long> completeTimeOverhead=new HashMap<Integer, Long>();
	
	public HashMap<File, Double> getEstimatedObjectCount() {
		if(this.estimatedObjectCount==null){
			this.estimatedObjectCount=new HashMap<>();
		}
		return estimatedObjectCount;
	}

	public Double getEstimatedObjectCount(File file) {
		return estimatedObjectCount.get(file);
	}

	public HashMap<File, Integer> getTruncateObjectCount() {
		return truncateObjectCount;
	}

	public Integer getTruncateObjectCount(File file) {
		return truncateObjectCount.get(file);
	}

	public HashMap<File, Long> getTimeOverhead() {
		if(this.timeOverhead==null){
			this.timeOverhead=new HashMap<>();
		}
		return timeOverhead;
	}
	
	public Long getTimeOverhead(File file){
		return timeOverhead.get(file);
	}

	public HashMap<Integer, Long> getCompleteTimeOverhead() {
		if(this.completeTimeOverhead==null){
			this.completeTimeOverhead=new HashMap<>();
		}
		return completeTimeOverhead;
	}
	
	public Long getCompleteTimeOverhead(int cellCountGap){
		return this.completeTimeOverhead.get(cellCountGap);
	}
}
