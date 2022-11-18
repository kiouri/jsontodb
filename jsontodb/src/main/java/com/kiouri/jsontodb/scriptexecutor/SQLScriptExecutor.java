package com.kiouri.jsontodb.scriptexecutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.jdbc.ScriptRunner;

import com.kiouri.jsontodb.tagstodb.IDBConnProducer;

public class SQLScriptExecutor {
	
	private IDBConnProducer dbConnProducer;
	
	public SQLScriptExecutor(IDBConnProducer dbConnProducer) {
		this.dbConnProducer = dbConnProducer;
	}
		
	public void executeSQLScriptFromFile(String pathToSQLStatementsFile, String pathToDBConfigJson)
			throws ClassNotFoundException, SQLException, IOException {
		
		Connection conn = dbConnProducer.getConnection(pathToDBConfigJson);
		// Initialize the script runner
		ScriptRunner sr = new ScriptRunner(conn);
		sr.setStopOnError(true);
		// Creating a reader object
		Reader reader = new BufferedReader(new FileReader(pathToSQLStatementsFile));
		// Running the script
		sr.runScript(reader);
		conn.close();
	}
	
	public void executeSQLScriptFromStr(String sqlStatementsStr, String pathToDBConfigJson)
			throws ClassNotFoundException, SQLException, IOException {
		
		Connection conn = dbConnProducer.getConnection(pathToDBConfigJson);
		// Initialize the script runner
		ScriptRunner sr = new ScriptRunner(conn);
		sr.setStopOnError(true);
		// Creating a reader object
		Reader reader = new BufferedReader(new StringReader(sqlStatementsStr));
		// Running the script
		sr.runScript(reader);
		conn.close();
	}
}
