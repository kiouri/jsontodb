package com.kiouri.jsontodb.postgresql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.JSONObject;

import com.kiouri.jsontodb.tagstodb.IDBConnProducer;
import com.kiouri.jsontodb.utils.Utils;

/**
 * 
 * Getting a database connection
 * DB connection parameters must be set in a json file with the following structure:
 * 
 * {
 *	"host" : "localhost",
 *	"port" : "5432",
 *	"database" : "nafta",
 *	"schema" : "public",
 *	"user" : "postgres",
 *	"password" : "XXXXXXXX"
 * }
 *
 */

public class PostgresConnProducer implements IDBConnProducer {
	
	private String host;
	private int port;
	private String database;
	private String schema;
	private String user;
	private String password;

	@Override
	public Connection getConnection(String host, int port, String dataBase, String schema, String userName,
			String password) throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:postgresql://" + 
		host + ":" + port + "/" + dataBase +"?currentSchema=" + schema, user, password);			
		return conn;
	}
	
	@Override
	public Connection getConnection(String pathToJsonConfig) throws IOException, ClassNotFoundException, SQLException {
		String jsonConfigStr = Utils.getStrFromFile(pathToJsonConfig);
		JSONObject obj = new JSONObject(jsonConfigStr);
		host = obj.getString("host");
		port = Integer.parseInt(obj.getString("port"));
		database = obj.getString("database");
		schema = obj.getString("schema");
		user = obj.getString("user");
		password = obj.getString("password");
		Connection conn =  getConnection(host, port, database, schema, user, password);		
		return conn;
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
		PostgresConnProducer dbConnProducer = new PostgresConnProducer();
		Connection conn = dbConnProducer.getConnection(
				"D:/myWork/workspace1/json_parsing/src/main/resources/json/postgreSQL_connection.json");
		System.out.println("Connection = " + conn);	
	}

}
