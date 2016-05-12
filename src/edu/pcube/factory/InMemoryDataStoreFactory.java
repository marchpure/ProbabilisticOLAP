package edu.pcube.factory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import com.macrofocus.data.db.MySQLDatabaseDriver;
import com.macrofocus.data.source.ExcelDataSource;
import com.macrofocus.data.source.JDBCDataSource;

import edu.pcube.datastore.InMemoryDataStore;


public class InMemoryDataStoreFactory {
	private static final InMemoryDataStoreFactory ourInstance = new InMemoryDataStoreFactory();

	public static InMemoryDataStoreFactory getInstance() {
		return ourInstance;
	}

	protected InMemoryDataStoreFactory() {
	}

	/**
	 * Creates a data frame by loading an Excel (.xls, .xlsx) file.
	 */
	public static InMemoryDataStore<Object> fromExcel(URL url) throws IOException {
		try {
			return new InMemoryDataStore<Object>(new ExcelDataSource(url).load(null));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Creates a data frame by loading an RDBMS(mysql) (.xls, .xlsx) file.
	 */
	public static InMemoryDataStore<Object> fromMySQL(String url, String username, String password, String query) throws IOException,SQLException {
		return new InMemoryDataStore<Object>(new JDBCDataSource(new MySQLDatabaseDriver(), url, username, password, query).load(null));
	}
	
	public static InMemoryDataStore<Object> fromMySQL(String url, String username, String password, String query, int M) throws IOException,SQLException {
		return new InMemoryDataStore<Object>(new JDBCDataSource(new MySQLDatabaseDriver(), url, username, password, query).load(null),M);
	}
}
