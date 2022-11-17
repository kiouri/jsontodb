package jsonparsing.usagesamples.sample3;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jsonparsing.dataloader.DataLoader;
import jsonparsing.ddl.DDLGenerator;
import jsonparsing.ddl.TagsDescListGenerator;
import jsonparsing.postgresql.DdlPostgresStatementGenerator;
import jsonparsing.postgresql.PostgresConnProducer;
import jsonparsing.postgresql.ToPostgresNameConvertor;
import jsonparsing.scriptexecutor.SQLScriptExecutor;
import jsonparsing.tagstodb.IDBConnProducer;
import jsonparsing.utils.Utils;

public class Step31 {
	
	public String DBCONFIGFILENAME = "DBConfig.json";
	public String CONFIGDIRNAME = "config/";
	public String DESCLISTFILENAME = "serializedDescList.json";
	
	public void processFile(String pathToSrcJson, String pathToDBConfign, String pathToDescList) throws Exception {
		
		TagsDescListGenerator tagsTreeGenerator = new TagsDescListGenerator(new ToPostgresNameConvertor());
		String tagsDescStr = tagsTreeGenerator.generateTagsDescStrFromJsonFile(pathToSrcJson, pathToDescList);
		Utils.writeStrToFile(tagsDescStr, pathToDescList);
		System.out.println("1. Generated tags descriptor");
		
		DdlPostgresStatementGenerator ddlStatementGenerator = new DdlPostgresStatementGenerator();
		DDLGenerator ddlGenerator = new DDLGenerator(ddlStatementGenerator);
		String generatedDDL =  ddlGenerator.generateDDLStr(pathToDescList);
		System.out.println("2. Generated ddl");

		IDBConnProducer postgresConnProducer = new PostgresConnProducer();
		SQLScriptExecutor sqlScriptExecutor = new SQLScriptExecutor(postgresConnProducer);
		sqlScriptExecutor.executeSQLScriptFromStr(generatedDDL, pathToDBConfign);
		System.out.println("3. Executed ddl");

		DataLoader dataLoader = new DataLoader();
		String cuirrentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
		String insertSQLSTR = dataLoader.getInsSQLStmtsAsString(pathToSrcJson, pathToDescList, cuirrentTime);
		System.out.println("4. Generated insert sql statements");

		sqlScriptExecutor.executeSQLScriptFromStr(insertSQLSTR, pathToDBConfign);
		System.out.println("5. Executed insert sql statements");
	}
	
	public void processFilesInDir(String pathToSrcDir) throws Exception {
		if (!pathToSrcDir.endsWith(File.separator)) {
			pathToSrcDir += File.separator;
		}
		String pathToDBConfig = pathToSrcDir +  CONFIGDIRNAME + DBCONFIGFILENAME ;
		String pathToDescList = pathToSrcDir +  CONFIGDIRNAME + DESCLISTFILENAME;
		List<String> files = Utils.getFullPathListForDir(pathToSrcDir, "*.json"); //only *.json files
		for (String filePath : files) {	
			processFile(filePath, pathToDBConfig, pathToDescList);			
			Utils.removeFile(filePath);	
		}
	}
		
	public static void main(String[] args) throws Exception {
		String pathToSrcDir = args[0];
		Step31 step31 = new Step31();
		step31.processFilesInDir(pathToSrcDir);
	}

}
