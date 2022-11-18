package com.kiouri.jsontodb.usagesamples.sample1;

import java.io.IOException;
import java.sql.SQLException;

import com.kiouri.jsontodb.postgresql.PostgresConnProducer;
import com.kiouri.jsontodb.scriptexecutor.SQLScriptExecutor;
import com.kiouri.jsontodb.tagstodb.IDBConnProducer;

public class Step13 {

	public static String DBCONFIGFILENAME = "DBConfig.json";
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		String pathToSrcJson = args[0];
		String pathToGeneratedDDL = Step12.getFullPathToDDLFile(pathToSrcJson);
		String pathToDBConfig = getFullPathToDBConfigJson(pathToSrcJson);
		IDBConnProducer postgresConnProducer = new PostgresConnProducer();
		SQLScriptExecutor sqlScriptExecutor = new SQLScriptExecutor(postgresConnProducer);
		sqlScriptExecutor.executeSQLScriptFromFile(pathToGeneratedDDL, pathToDBConfig);
	}
	
	public static String getFullPathToDBConfigJson(String pathToSrcJson) {
		String path = Step11.getDirOfSrcJson(pathToSrcJson);
		String pathToDBconfig = path + DBCONFIGFILENAME;
		return pathToDBconfig;
	}

}
