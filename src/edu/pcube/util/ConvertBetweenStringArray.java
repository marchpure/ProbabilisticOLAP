package edu.pcube.util;

import java.util.StringTokenizer;

public class ConvertBetweenStringArray {
	
	public static <V> V[] convert(String row){		
		StringTokenizer tokenizer=new StringTokenizer(row, "|");
		V[] rowVs=(V[]) new Object[tokenizer.countTokens()];
		int index=0;
		while(tokenizer.hasMoreTokens()){
			Object token= tokenizer.nextToken();
			rowVs[index++]=(V) token;
		}
		return rowVs;
	}
	
	public static <V> String convert(V[] row){
		String rowString=new String();
		
		for(int i=0;i<row.length-1;i++){
			rowString+=row[i];
			rowString+="|";
		}
		rowString+=row[row.length-1];
		
		return rowString;
	}
	
}
