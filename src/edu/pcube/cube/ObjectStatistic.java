package edu.pcube.cube;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.pcube.datastore.InMemoryDataStore;
import edu.pcube.datastore.ObjectGroupedTuples;
import edu.pcube.util.ConvertBetweenStringArray;
import edu.pcube.util.DBaddress;

public class ObjectStatistic implements Serializable{

	private static final long serialVersionUID = -5809782578272943999L;

	private int objectNum;
	private double density;
	private double[] domain;
	private double[] u;


	public int getObjectNum() {
		return objectNum;
	}

	public double[] getDomain() {
		return domain;
	}


	public double[] getU() {
		return u;
	}

	public ObjectStatistic(int dimensionCount) {
		super();
		this.domain=new double[dimensionCount];
		this.u=new double[dimensionCount];
	}



	public void statistic(InMemoryDataStore inMemoryDataStores) throws SQLException{
		this.objectNum=inMemoryDataStores.getObjectGroupedTuples().size();

		HashMap<String, ArrayList<Object>> objectHashMap=new HashMap<>();

		String[] dimensions=inMemoryDataStores.getDimensions();
		int dimensionCount=inMemoryDataStores.getDimensions().length;

		for(int i=0;i<dimensionCount;i++){
			String attributeName=inMemoryDataStores.getDimensions(i);
			objectHashMap.put(attributeName, setDomain(attributeName));
			this.domain[i]=objectHashMap.get(attributeName).size();
			//System.out.println(objectHashMap.get(attributeName).size());
		}

		double[] avg=new double[dimensionCount];
		for(int k=0;k<dimensionCount;k++){
			avg[k]=0.0;
		}

		for(Iterator<Object> iterator=inMemoryDataStores.getObjectGroupedTuples().keySet().iterator();iterator.hasNext();){
			Object key=iterator.next();
			ObjectGroupedTuples objectGroupedTuples=inMemoryDataStores.getObjectTuplesbyObjectId(key);
			//确定出来对于一个object来说的最小和最大界；
			int[] minarrayObject=new int[dimensionCount];
			int[] maxarrayObject=new int[dimensionCount];
			for(int k=0;k<dimensionCount;k++){
				minarrayObject[k]=Integer.MAX_VALUE;
				maxarrayObject[k]=Integer.MIN_VALUE;
			}

			for(Iterator<String> rowIterator=objectGroupedTuples.getRowValues().iterator();rowIterator.hasNext();){
				Object[] rowValue=ConvertBetweenStringArray.convert(rowIterator.next());
				for(int column=0;column<dimensionCount;column++){
					Object value=rowValue[column];
					//System.out.println(value);
					int index=objectHashMap.get(inMemoryDataStores.getColumnByIndex(column)).indexOf(value);
					//System.out.println(column+"--->"+inMemoryDataStores.getColumnByIndex(column));
					if(index==-1){
						for(Object v:objectHashMap.get(inMemoryDataStores.getColumnByIndex(column))){
							if(v.toString().equals(value)){
								index=objectHashMap.get(inMemoryDataStores.getColumnByIndex(column)).indexOf(v);
								break;
							}
						}
					}
					if(index<minarrayObject[column]){
						minarrayObject[column]=index;
					}
					if(index>maxarrayObject[column]){
						maxarrayObject[column]=index;
					}
				}
			}

			for(int i=0;i<dimensionCount;i++){
				if(maxarrayObject[i]==Integer.MIN_VALUE || minarrayObject[i]==Integer.MAX_VALUE){

				}else {
					//System.out.println("maxarrayObject[i]:"+maxarrayObject[i]);
					//System.out.println("minarrayObject[i]:"+minarrayObject[i]);
					//System.out.println("inMemoryDataStores.getColumnByIndex(i)).size():"+objectHashMap.get(inMemoryDataStores.getColumnByIndex(i)).size());

					int a=maxarrayObject[i]-minarrayObject[i];
					int b=objectHashMap.get(inMemoryDataStores.getColumnByIndex(i)).size()-maxarrayObject[i]+minarrayObject[i];

					avg[i]+=a<b?a:b;
				}
			}
		}

		double product=1;
		for(int i=0;i<dimensionCount;i++){
			avg[i]=avg[i]/(double)this.objectNum;
			product=product*domain[i];
		}

		this.u[0]=avg[1];
		this.u[1]=avg[0];
		this.u[2]=avg[4];
		this.u[3]=avg[3];
		this.u[4]=avg[2];
		this.u[5]=avg[6];
		this.u[6]=avg[5];
		this.u[7]=avg[8];
		this.u[8]=avg[7];

		//this.density=(double)this.objectNum/(double)product;
		//print();
	}


	private ArrayList<Object> setDomain(String attributeName) throws SQLException {
		// TODO Auto-generated method stub

		Connection conn = null;
		String sql;
		String url="jdbc:mysql://"+DBaddress.dbAddress+":3306/tpch?"
				+ "user=root&password=root&useUnicode=true&characterEncoding=UTF8";
		ArrayList<Object> columnValues=new ArrayList<>();
		try {

			Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
			Statement stmt = conn.createStatement();
			//sql="SELECT "+attributeName+" FROM tpchdata ORDER BY "+attributeName;
			sql="SELECT DISTINCT "+ attributeName+" FROM tpchdata ORDER BY "+attributeName;
			//sql="SELECT DISTINCT "+ attributeName+" FROM ocean ORDER BY "+attributeName;
			
			ResultSet rs = stmt.executeQuery(sql);// executeQuery会返回结果的集合，否则返回空值

			while (rs.next()) {
				columnValues.add(rs.getObject(1));
			}
			return columnValues;
		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return columnValues;
	}

	public void print(){
		System.out.println("objectNum:"+objectNum);
		System.out.println("density:"+density);
		for(int i=0;i<domain.length;i++){
			System.out.println("domain_"+i+":"+domain[i]);
			System.out.println("u_"+i+":"+u[i]);
		}
	}
}
