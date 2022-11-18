package com.kiouri.jsontodb.tagstodb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface IDBConnProducer {

	public Connection getConnection(String host, int port, String dataBase, String schema, String userName,
			String password) throws IOException, ClassNotFoundException, SQLException ;

	public Connection getConnection(String pathToJsonConfig) throws IOException, ClassNotFoundException, SQLException;

}
