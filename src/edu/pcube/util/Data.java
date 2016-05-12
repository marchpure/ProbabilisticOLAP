package edu.pcube.util;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.example.TpchTest;
import edu.pcube.factory.InMemoryDataStoreFactory;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class Data {


	public InMemoryDataStore inMemoryDataStore;

	public static void writeExcel(double a[][],int M) throws IOException, RowsExceededException, WriteException{   
		System.out.println(a[1][1]);

		File file = new File("./test2.xls");
		if(!file.exists()){
			file.createNewFile();
		}
		WritableWorkbook wwb= Workbook.createWorkbook(file);
		WritableSheet ws = wwb.createSheet("Test Sheet 1",0); 
		Number labelN = new Number(1, 1, 2);
		ws.addCell(labelN); 
		int index = 0;
		//写入数据
		
		System.out.println("M:"+M);
		System.out.println("a[1].length:"+a[1].length);
		for(int i=1;i<M;i++){
			for(int j=0;j<a[i].length;j++){
				Number label = new Number(0, index, a[i][j]);
				ws.addCell(label); 
				index++;
				System.out.println("i:"+i+"j:"+j+"a[i][j]:"+a[i][j]);
			}
		}
		wwb.write();   
		wwb.close();
	} 
	
	public static void writeExcel(String fileName,HashMap<Long, Long> data) throws IOException, RowsExceededException, WriteException{   

		File file = new File("./result/"+fileName+".xls");
		if(file.exists()){
			file.delete();
		}
		file.createNewFile();
		WritableWorkbook wwb= Workbook.createWorkbook(file);
		WritableSheet ws = wwb.createSheet(fileName,0); 
		int index = 0;
		
		//写入数据
		for(Long key:data.keySet()){
			Number labelKey = new Number(0, index, key);
			Number labelValue = new Number(1, index, data.get(key));
			ws.addCell(labelKey); 
			ws.addCell(labelValue); 
			index++;
		}
		
		wwb.write();   
		wwb.close();
	} 
	
	public static void writeExcel(String fileName,HashMap<String, Double> data,String a) throws IOException, RowsExceededException, WriteException{   

		File file = new File("./result/"+fileName+".xls");
		if(file.exists()){
			file.delete();
		}
		file.createNewFile();
		WritableWorkbook wwb= Workbook.createWorkbook(file);
		WritableSheet ws = wwb.createSheet(fileName,0); 
		int index = 0;
		
		//写入数据
		for(String key:data.keySet()){
			
			Label labelKeyA=new Label(0, index, key);
			
			Number labelValue = new Number(1, index, data.get(key));
			ws.addCell(labelKeyA); 
			ws.addCell(labelValue); 
			index++;
		}
		
		wwb.write();   
		wwb.close();
	} 
	
	public static void writeExcel(String fileName,HashMap<Double, Double> data,int a) throws IOException, RowsExceededException, WriteException{   

		File file = new File("./result/"+fileName+".xls");
		if(file.exists()){
			file.delete();
		}
		file.createNewFile();
		WritableWorkbook wwb= Workbook.createWorkbook(file);
		WritableSheet ws = wwb.createSheet(fileName,0); 
		int index = 0;
		
		//写入数据
		for(Double key:data.keySet()){
			Number labelKey = new Number(0, index, key);
			Number labelValue = new Number(1, index, data.get(key));
			ws.addCell(labelKey); 
			ws.addCell(labelValue); 
			index++;
		}
		
		wwb.write();   
		wwb.close();
	} 
}
