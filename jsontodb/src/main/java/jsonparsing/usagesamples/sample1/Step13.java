package jsonparsing.usagesamples.sample1;

import java.io.IOException;
import java.sql.SQLException;

import jsonparsing.postgresql.PostgresConnProducer;
import jsonparsing.scriptexecutor.SQLScriptExecutor;
import jsonparsing.tagstodb.IDBConnProducer;

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
