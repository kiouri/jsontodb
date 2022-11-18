package com.kiouri.jsontodb.usagesamples.sample1;

import java.io.IOException;
import java.sql.SQLException;

import com.kiouri.jsontodb.postgresql.PostgresConnProducer;
import com.kiouri.jsontodb.scriptexecutor.SQLScriptExecutor;
import com.kiouri.jsontodb.tagstodb.IDBConnProducer;

public class Step15 {
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		String pathToSrcJson = args[0];
		String pathToGeneratedSQL = Step14.getFullPathToSQLFile(pathToSrcJson);
		String pathToDBConfig = getFullPathToDBConfigJson(pathToSrcJson);
		IDBConnProducer postgresConnProducer = new PostgresConnProducer();
		SQLScriptExecutor sqlScriptExecutor = new SQLScriptExecutor(postgresConnProducer);
		sqlScriptExecutor.executeSQLScriptFromFile(pathToGeneratedSQL, pathToDBConfig);
	}
	
	public static String getFullPathToDBConfigJson(String pathToSrcJson) {
		String path = Step11.getDirOfSrcJson(pathToSrcJson);
		String pathToDBconfig = path + Step13.DBCONFIGFILENAME;
		return pathToDBconfig;
	}
}
