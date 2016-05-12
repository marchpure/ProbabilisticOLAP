package edu.pcube.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;


public class TPCHProcessing {

	public static ArrayList<Object> pNAMEArrayList=new ArrayList<Object>();
	public static ArrayList<Object> oRDERTIMEArrayList=new ArrayList<Object>();
	public static ArrayList<Object> sNAMEArrayList=new ArrayList<Object>();
	public static ArrayList<Object> cNAMEArrayList=new ArrayList<Object>();

	public static HashMap<Object, ArrayList<Object>> pNAMEHashMap=new HashMap<Object, ArrayList<Object>>();
	public static HashMap<Object, ArrayList<Object>> oRDERHashMap=new HashMap<Object, ArrayList<Object>>();
	public static HashMap<Object, ArrayList<Object>> sNAMEHashMap=new HashMap<Object, ArrayList<Object>>();
	public static HashMap<Object, ArrayList<Object>> cNAMEHashMap=new HashMap<Object, ArrayList<Object>>();
	
	public static ArrayList<Object> p=new ArrayList<>();
	public static ArrayList<Object> o=new ArrayList<>();
	public static ArrayList<Object> s=new ArrayList<>();
	public static ArrayList<Object> c=new ArrayList<>();

	public TPCHProcessing() {
		super();
	}

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
				//samplesIndex[i][k]=(int) error[k];
				
				if(k==variableIndex){
					samplesIndex[i][k]=(int) error[k];
				}else {
					samplesIndex[i][k]=row[k];
				}
				
				if(k==0){
					samples[i][k]=pNAMEArrayList.get(samplesIndex[i][k]);
				}else if (k==1) {
					samples[i][k]=oRDERTIMEArrayList.get(samplesIndex[i][k]);
				}else if (k==2) {
					samples[i][k]=sNAMEArrayList.get(samplesIndex[i][k]);
				}else {
					samples[i][k]=cNAMEArrayList.get(samplesIndex[i][k]);
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

	public static void tpcH(int M) throws SQLException{
		Connection conn = null;
		String sql;
		String url=DBaddress.dbconnectString
				+ "?user=root&password=root&useUnicode=true&characterEncoding=UTF8";

		try {

			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();
			sql="SELECT P_NAME,P_BRAND,P_MFGR,O_ORDERDATE AS O_ORDERDAY,MONTH(O_ORDERDATE) AS O_ORDERMONTH,YEAR(O_ORDERDATE) AS O_ORDERYEAR,S_NAME,S_N.N_NAME AS S_NATIONNAME,S_R.R_NAME AS S_REGIONNAME,C_NAME,C_N.N_NAME AS C_NATIONNAME,C_R.R_NAME AS C_REGIONNAME,O_TOTALPRICE AS P "
					+ "FROM lineitem "
					+ "LEFT JOIN part ON lineitem.L_PARTKEY=part.P_PARTKEY "
					+ "LEFT JOIN orders ON lineitem.L_ORDERKEY=orders.O_ORDERKEY "
					+ "LEFT JOIN supplier ON lineitem.L_SUPPKEY=supplier.S_SUPPKEY "
					+ "LEFT JOIN nation AS S_N ON supplier.S_NATIONKEY=S_N.N_NATIONKEY "
					+ "LEFT JOIN region AS S_R ON S_N.N_REGIONKEY=S_R.R_REGIONKEY "
					+ "LEFT JOIN customer ON orders.O_CUSTKEY=customer.C_CUSTKEY "
					+ "LEFT JOIN nation AS C_N ON customer.C_NATIONKEY=C_N.N_NATIONKEY "
					+ "LEFT JOIN region AS C_R ON C_N.N_REGIONKEY=C_R.R_REGIONKEY "
					+ "ORDER BY O_ORDERDATE "
					+ "LIMIT 1,"+M;

			System.out.println("start to execute");
			ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
			int N=13; //column length

			ArrayList<Object[]> rows=new ArrayList<Object[]>();
			int rowNum=0;
			while (rs.next()) {
				System.out.println(rowNum);
				Object[] row=new Object[16];
				row[1]=rowNum;
				for(int i=2;i<14;i++){
					row[i]=rs.getObject(i-1);
				}
				row[14]=rs.getBigDecimal(13).intValue()/1000;

				//将row[] 放入到一个区间，然后服从某一种分布。
				//要把row[i]，就是同一列的转换为一个区间。

				rows.add(row);
				rowNum++;
			}

			sql="TRUNCATE TABLE facttable";
			stmt.execute(sql);

			sql="insert into facttable values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement cmd=conn.prepareStatement(sql);

			Object[] row=new Object[15];
			int num=0;
			for(Iterator<Object[]> rowIterator=rows.iterator();rowIterator.hasNext();){
				row=rowIterator.next();
				for(int i=1;i<15;i++){
					cmd.setObject(i, row[i]);
				}
				cmd.setObject(15, 1.0);
				rowNum++;
				cmd.addBatch();
				if(rowNum%1000==0){
					num++;
					System.out.println("row:"+num);
					cmd.executeBatch();
				}
			}
			cmd.executeBatch();
			cmd.close();

		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	public static void dataProcessing() throws SQLException{
		Connection conn = null;
		String sql;
		String url=DBaddress.dbconnectString
				+ "?user=root&password=root&useUnicode=true&characterEncoding=UTF8";

		try {

			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();

			sql="SELECT P_BRAND,P_MFGR,O_ORDERDAY,O_ORDERMONTH,O_ORDERYEAR,S_NATIONNAME,S_REGIONNAME,C_NATIONNAME,C_REGIONNAME "
					+ "FROM facttable";

			ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值

			while (rs.next()) {
				pNAMEHashMap.put(rs.getObject(1), new ArrayList<Object>());
				pNAMEHashMap.get(rs.getObject(1)).add(rs.getObject(2));
				if(!p.contains(rs.getObject(2))){
					p.add(rs.getObject(2));
				}

				oRDERHashMap.put(rs.getObject(3), new ArrayList<Object>());
				oRDERHashMap.get(rs.getObject(3)).add(rs.getObject(4));
				oRDERHashMap.get(rs.getObject(3)).add(rs.getObject(5));
				if(!o.contains(rs.getObject(3))){
					o.add(rs.getObject(3));
				}

				sNAMEHashMap.put(rs.getObject(6), new ArrayList<Object>());
				sNAMEHashMap.get(rs.getObject(6)).add(rs.getObject(7));
				if(!o.contains(rs.getObject(7))){
					s.add(rs.getObject(7));
				}

				cNAMEHashMap.put(rs.getObject(8), new ArrayList<Object>());
				cNAMEHashMap.get(rs.getObject(8)).add(rs.getObject(9));
				if(!o.contains(rs.getObject(9))){
					c.add(rs.getObject(9));
				}				
			}
		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	public static ArrayList<Object> generateAttributes(String attributeName) throws SQLException{
		Connection conn = null;
		String sql;
		String url=DBaddress.dbconnectString
				+ "?user=root&password=root&useUnicode=true&characterEncoding=UTF8";
		ArrayList<Object> attributeValues=new ArrayList<Object>();

		try {
			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();
			sql="SELECT DISTINCT "+ attributeName+" FROM facttable ORDER BY "+attributeName;

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


	public static void generateProbabistic(int instense) throws SQLException{
		Connection conn = null;
		String sql;
		String url=DBaddress.dbconnectString
				+ "?user=root&password=root&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true";

		try {

			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();

			System.out.println("++++++++++++++");
			pNAMEArrayList=generateAttributes("P_BRAND");
			oRDERTIMEArrayList=generateAttributes("O_ORDERDAY");
			sNAMEArrayList=generateAttributes("S_NATIONNAME");
			cNAMEArrayList=generateAttributes("C_NATIONNAME");

			System.out.println("++++++++++++++");
			double[] avg=new double[4];
			avg[0]=pNAMEArrayList.size();
			avg[1]=oRDERTIMEArrayList.size();
			avg[2]=sNAMEArrayList.size();
			avg[3]=cNAMEArrayList.size();

			System.out.println(avg[0]);
			System.out.println(avg[1]);
			System.out.println(avg[2]);
			System.out.println(avg[3]);
			System.out.println("+++++++++++++");


			double[] dev1D=new double[4];
			dev1D[0]=indefiniteIntegral((int) avg[0]);
			dev1D[1]=indefiniteIntegral((int) avg[1]);
			dev1D[2]=indefiniteIntegral((int) avg[2]);
			dev1D[3]=indefiniteIntegral((int) avg[3]);

			System.out.println(dev1D[0]);
			System.out.println(dev1D[1]);
			System.out.println(dev1D[2]);
			System.out.println(dev1D[3]);
			System.out.println("+++++++++++++");

			double[][] dev=new double[4][4];

			for(int i=0;i<4;i++){
				for(int j=0;j<4;j++){
					if(i==j){
						dev[i][i]=dev1D[i];
					}else{
						dev[i][j]=0.0;
					}
				}
			}


			sql="SELECT L_ORDERID,P_BRAND,O_ORDERDAY,S_NATIONNAME,C_NATIONNAME,X FROM facttable";
			ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值
			ArrayList<Object[]> rows=new ArrayList<Object[]>();

			while (rs.next()) {
				int[] row=new int[4];
				row[0]=pNAMEArrayList.indexOf(rs.getObject(2));
				row[1]=oRDERTIMEArrayList.indexOf(rs.getObject(3));
				row[2]=sNAMEArrayList.indexOf(rs.getObject(4));
				row[3]=cNAMEArrayList.indexOf(rs.getObject(5));

				HashMap<Object[], Double> rowP=new HashMap<Object[], Double>();
				rowP=gaussian(row, instense, avg, dev);

				for(Object[] rowAttributs:rowP.keySet()){
					Object[] singleRow=new Object[7];
					singleRow[0]=rs.getObject(1);
					singleRow[1]=rowAttributs[0];
					singleRow[2]=rowAttributs[1];
					singleRow[3]=rowAttributs[2];
					singleRow[4]=rowAttributs[3];
					singleRow[5]=rs.getObject(6);
					singleRow[6]=rowP.get(rowAttributs);
					rows.add(singleRow);
				}
			}
			rs.close();

			Random random=new Random();
			ArrayList<Object[]> tpcData=new ArrayList<Object[]>();
			for(Object[] row:rows){
				//System.out.println("------");
				Object[] singleRow=new Object[15];
				singleRow[0]=row[0];
				int objectid=(int) singleRow[0];
				//singleRow[1]=row[1];
				Object pBRAND=row[1];

				singleRow[1]=pBRAND;
				
				singleRow[2]=pNAMEHashMap.get(pBRAND).get(0);
				//singleRow[2]=p.get(objectid % p.size());
				
				//singleRow[3]=row[2];
				singleRow[3]=o.get(objectid % o.size());

				singleRow[4]=oRDERHashMap.get(singleRow[3]).get(0);
				singleRow[5]=oRDERHashMap.get(singleRow[3]).get(1);

				//singleRow[7]=row[3];
				Object sNATION=row[3];

				singleRow[6]=sNATION;
				singleRow[7]=sNAMEHashMap.get(sNATION).get(0);
				//singleRow[7]=s.get(objectid % s.size());

				//singleRow[10]=row[4];
				Object cNATION=row[4];

				singleRow[8]=cNATION;
				singleRow[9]=cNAMEHashMap.get(cNATION).get(0);
				//singleRow[9]=c.get(objectid % c.size());

				singleRow[10]=row[5];

				int bound=((BigDecimal)singleRow[10]).intValue()/20;
				if(bound==0){
					bound++;
				}
				BigDecimal row10=new BigDecimal(((BigDecimal)singleRow[10]).intValue()+random.nextInt(bound));
				singleRow[10]=row10;

				singleRow[11]=row[6];

				singleRow[12]=1;
				
				tpcData.add(singleRow);
			}

			sql="TRUNCATE TABLE tpchdata";
			stmt.execute(sql);

			sql="insert into tpchdata values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement cmd=conn.prepareStatement(sql);

			Object[] row=new Object[13];
			int rowNum=0;
			for(Iterator<Object[]> rowIterator=tpcData.iterator();rowIterator.hasNext();){
				row=rowIterator.next();

				for(int i=1;i<14;i++){
					cmd.setObject(i, row[i-1]);
				}
 
				rowNum++;
				cmd.addBatch();
				if(rowNum%1000==0){
					rowNum++;
					System.out.println("rowNum:"+rowNum);
					cmd.executeBatch();
				}
			}
			cmd.executeBatch();
			cmd.close();
		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	public static <V> void main(String[] args) throws IOException, SQLException{
		
		int objectCount=0;     //用户自定义
		int tupleCountPerObject=0;     //用户自定义
		
		tpcH(objectCount);
		dataProcessing();
		generateProbabistic(tupleCountPerObject);
	}
}
