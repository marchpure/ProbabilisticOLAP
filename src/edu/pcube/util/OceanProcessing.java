package edu.pcube.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;


public class OceanProcessing {

	public static ArrayList<Object> dateArrayList=new ArrayList<Object>();
	public static ArrayList<Object> dirArrayList=new ArrayList<Object>();
	public static ArrayList<Object> spdArrayList=new ArrayList<Object>();
	public static ArrayList<Object> tempArrayList=new ArrayList<Object>();

	//public static MultivariateNormalDistribution mnd=null;
	public static double[] avg=new double[4];
	public static double[] dev1D=new double[4];
	public static double[][] dev=new double[4][4];

	//决定正太分布的参数
	public static double indefiniteIntegral(int u){
		double min=1;
		double miny=0;
		for(double y=1.0;y<2;y+=0.01){
			double sum=0.0;

			for(int x=u;x<=u+1;x++){
				sum+=1/(Math.sqrt(2*Math.PI)*y)*Math.pow(Math.E, -Math.pow(x-u, 2)/(2*Math.pow(y, 2)));
			}
			if(sum>0.46){
				if(sum-0.46<min){
					min=sum-0.46;
					miny=y;
				}
			}
		}
		return miny;
	} 

	public static ArrayList<Object> generateAttributes(String attributeName) throws SQLException{
		Connection conn = null;
		String sql;
		String url="jdbc:mysql://192.168.1.112:3306/tpch?"
				+ "user=root&password=root&useUnicode=true&characterEncoding=UTF8";
		ArrayList<Object> attributeValues=new ArrayList<Object>();

		try {
			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();
			sql="SELECT DISTINCT "+ attributeName+" FROM oceandata ORDER BY "+attributeName;

			ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空

			while (rs.next()) {
				Object PNAME=rs.getObject(1);
				attributeValues.add(PNAME);
			}

		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return attributeValues;
	}

	public static HashMap<Object[],Double> gaussian(int[] row,Integer N,double[] avg,double[][] dev){
		//mean是各个维度的中心
		int[][] samplesIndex=new int[N][4];
		Object[][] samples=new Object[N][4];
		double[] error=new double[4];

		double[] rowDouble=new double[4];
		for(int k=0;k<4;k++){
			rowDouble[k]=row[k];
		}
		MultivariateNormalDistribution mnd=new MultivariateNormalDistribution(rowDouble, dev);

		//在这里面选定一个纬度，其他纬度保持不变。

		Random random=new Random();
		int variableIndex=random.nextInt(4);

		for(int i=0;i<N;i++){
			error=mnd.sample();

			for(int k=0;k<error.length;k++){
				if(error[k]<0 || error[k]>avg[k]){
					error[k]=row[k];
				}
			}

			for(int k=0;k<4;k++){
				samplesIndex[i][k]=(int) error[k];

				if(k==0){
					samples[i][k]=dateArrayList.get(samplesIndex[i][k]);
				}else if (k==1) {
					samples[i][k]=dirArrayList.get(samplesIndex[i][k]);
				}else if (k==2) {
					samples[i][k]=spdArrayList.get(samplesIndex[i][k]);
				}else {
					samples[i][k]=tempArrayList.get(samplesIndex[i][k]);
				}
			}
		}
		HashMap<Object[],Double> sampleUnique=new HashMap<Object[], Double>();
		for(int i=0;i<N;i++){
			Object[] sampleKey=isContainKey(samples[i],sampleUnique);
			if(sampleKey==null){
				//System.out.println("*************");
				sampleUnique.put(samples[i], 1.0/(double)N);
			}else {
				double value=sampleUnique.get(sampleKey);
				sampleUnique.put(sampleKey, 1.0/(double)N+value);
			}

		}
		return sampleUnique;
	}


	public static boolean isEqualArray(Object[] a,Object[] b){
		if(a.length!=b.length){
			return false;
		}else {
			for(int i=0;i<a.length;i++){
				if(!a[i].equals(b[i])){
					return false;
				}
			}
		}
		return true;
	}

	private static Object[] isContainKey(Object[] objects,
			HashMap<Object[], Double> sampleUnique) {
		for(Object[] key:sampleUnique.keySet()){
			if(isEqualArray(objects,key)){
				return key;
			}
		}
		return null;
	}

	private static void changeTxtToMysql(int M) {
		// TODO Auto-generated method stub
		File file=new File("/home/hxj/workspace/allData.txt");

		Connection conn = null;
		String sql;
		String url="jdbc:mysql://192.168.1.112:3306/tpch?"
				+ "user=root&password=root&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true";

		try {
			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();
			sql="TRUNCATE TABLE oceandata";
			stmt.execute(sql);


			sql="insert into oceandata values(?,?,?,?,?,?)";
			PreparedStatement cmd=conn.prepareStatement(sql);

			BufferedReader br=new BufferedReader(new FileReader(file));
			String line="";
			int lineNum=0;
			br.readLine();

			int lineCount=0;

			Random random=new Random();

			while((line=br.readLine())!=null){
				lineCount++;
				if(lineCount==M){
					break;
				}
				StringTokenizer tokenizer=new StringTokenizer(line, " ");

				int tokenNum=0;
				cmd.setObject(1, lineNum);
				while(tokenizer.hasMoreTokens()){
					if(tokenNum==2){
						SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
						String time=tokenizer.nextToken();
						Date date=sdf.parse(time.substring(0, 8));
						//tokenizer.nextToken();
						//Date date=randomDate("2014-01-01", "2014-04-01");

						cmd.setObject(2, date);
					}else if(tokenNum==3){
						String dirString=tokenizer.nextToken();
						int DIR=0;
						if(!dirString.equals("***")){
							DIR=Integer.valueOf(dirString);
						}

						cmd.setObject(3, DIR);
					}else if(tokenNum==4){
						String spdString=tokenizer.nextToken();
						int SPD=0;
						if(!spdString.equals("***")){
							SPD=Integer.valueOf(spdString);
							//SPD=random.nextInt(100);
						}
						cmd.setObject(4, SPD);
					}else if(tokenNum==13){

						String tempString=tokenizer.nextToken();
						int TEMP=0;
						if(!tempString.equals("****")){
							TEMP=Integer.valueOf(tempString);
						}

						cmd.setObject(5, TEMP);
					}else if(tokenNum==14){
						String dwepString=tokenizer.nextToken();
						int DEWP=0;
						if(!dwepString.equals("****")){
							DEWP=Integer.valueOf(dwepString);
						}

						cmd.setObject(6, DEWP);
					}else {
						tokenizer.nextToken();
					}

					tokenNum++;

				}

				cmd.addBatch();
				lineNum++;

				if(lineNum%100000==0){
					System.out.println("lineNum:"+lineNum);
					cmd.executeBatch();
				}
			}

			cmd.executeBatch();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void generateProbabistic() throws SQLException{
		Connection conn = null;
		String sql = null;
		String url="jdbc:mysql://192.168.1.112:3306/tpch?"
				+ "user=root&password=root&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true";

		try {

			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();

			System.out.println("++++++++++++++");
			dateArrayList=generateAttributes("DATE");
			dirArrayList=generateAttributes("DIR");
			spdArrayList=generateAttributes("SPD");
			tempArrayList=generateAttributes("TEMP");

			System.out.println("++++++++++++++");
			avg[0]=dateArrayList.size();
			avg[1]=dirArrayList.size();
			avg[2]=spdArrayList.size();
			avg[3]=tempArrayList.size();

			System.out.println(avg[0]);
			System.out.println(avg[1]);
			System.out.println(avg[2]);
			System.out.println(avg[3]);
			System.out.println("+++++++++++++");



			dev1D[0]=indefiniteIntegral((int) avg[0]);
			dev1D[1]=indefiniteIntegral((int) avg[1]);
			dev1D[2]=indefiniteIntegral((int) avg[2]);
			dev1D[3]=indefiniteIntegral((int) avg[3]);

			for(int i=0;i<4;i++){
				for(int j=0;j<4;j++){
					if(i==j){
						dev[i][i]=dev1D[i];
					}else{
						dev[i][j]=0.0;
					}
				}
			}
			generateData(conn,sql,stmt,0,100000);
		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	public static void generateData(Connection conn,	String sql,Statement stmt,int startLimit,int endLimit){
		System.out.println("startLimit:"+startLimit);
		System.out.println("endLimit:"+endLimit);
		sql="SELECT OBJECTID,DATE,DIR,SPD,TEMP,X FROM oceandata WHERE OBJECTID>="+startLimit+" LIMIT "+endLimit;
		System.out.println(sql);
		ResultSet rs;
		try {
			rs = stmt.executeQuery(sql);
			ArrayList<Object[]> rows=new ArrayList<Object[]>();

			Random random=new Random();
			while (rs.next()) {
				int[] row=new int[4];
				row[0]=dateArrayList.indexOf(rs.getObject(2));
				row[1]=dirArrayList.indexOf(rs.getObject(3));
				row[2]=spdArrayList.indexOf(rs.getObject(4));
				row[3]=tempArrayList.indexOf(rs.getObject(5));

				HashMap<Object[], Double> rowP=new HashMap<Object[], Double>();
				rowP=gaussian(row, 20, avg, dev);

				for(Object[] rowAttributs:rowP.keySet()){
					Object[] singleRow=new Object[12];
					singleRow[0]=rs.getObject(1);

					singleRow[3]=rowAttributs[0];		
					String date=((Date) singleRow[3]).toString();
					singleRow[1]=Integer.valueOf(date.substring(0, 4));
					singleRow[2]=Integer.valueOf(date.substring(5, 7));

					singleRow[5]=rowAttributs[1];
					singleRow[4]=((int)singleRow[5])/40;

					singleRow[7]=rowAttributs[2];
					singleRow[6]=((int)singleRow[7])/10;

					singleRow[9]=rowAttributs[3];
					singleRow[8]=((int)singleRow[9])/10;

					singleRow[10]=new BigDecimal((int)rs.getObject(6));
					BigDecimal row10=null;

					if(random.nextDouble()<0.05){
						row10=new BigDecimal(((BigDecimal)singleRow[10]).intValue()+1);
					}else {
						row10=(BigDecimal) singleRow[10];
					}

					singleRow[10]=row10;

					singleRow[11]=rowP.get(rowAttributs);

					rows.add(singleRow);
				}
				//break;
			}
			rs.close();
			if(startLimit==0){
				sql="TRUNCATE TABLE oceanfacttable";
				stmt.execute(sql);
			}


			sql="insert into oceanfacttable values(?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement cmd=conn.prepareStatement(sql);

			Object[] row=new Object[13];
			int rowNum=0;
			for(Iterator<Object[]> rowIterator=rows.iterator();rowIterator.hasNext();){
				row=rowIterator.next();

				for(int i=1;i<13;i++){
					cmd.setObject(i, row[i-1]);
				}

				rowNum++;
				cmd.addBatch();
				if(rowNum%10000==0){
					rowNum++;
					//System.out.println("rowNum:"+rowNum);
					cmd.executeBatch();
				}
			}
			System.out.println(rowNum);
			cmd.executeBatch();
			cmd.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// executeQuery会返回结果的集合，否则返回空值		
	}


	public static <V> void main(String[] args) throws IOException, SQLException{
		changeTxtToMysql(100000);
		generateProbabistic();
	}
}
