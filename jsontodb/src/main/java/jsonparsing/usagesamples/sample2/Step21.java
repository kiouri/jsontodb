package jsonparsing.usagesamples.sample2;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import jsonparsing.dataloader.DataLoader;
import jsonparsing.ddl.DDLGenerator;
import jsonparsing.ddl.TagsDescListGenerator;
import jsonparsing.postgresql.DdlPostgresStatementGenerator;
import jsonparsing.postgresql.PostgresConnProducer;
import jsonparsing.postgresql.ToPostgresNameConvertor;
import jsonparsing.scriptexecutor.SQLScriptExecutor;
import jsonparsing.tagstodb.IDBConnProducer;
import jsonparsing.utils.Utils;

public class Step21 {

	public static String DESKLISTFILENAME = "serializedDescList.json";
	public static String DBCONFIGFILENAME = "DBConfig.json";
	public static String CONFIGDIRNAME = "config/";
	
	public static void main(String[] args) throws Exception {
		String pathToSrcJson = args[0];
		String pathToDescList = getFullPathToDescListFile(pathToSrcJson);
		TagsDescListGenerator tagsTreeGenerator = new TagsDescListGenerator(new ToPostgresNameConvertor());
		String tagsDescStr = tagsTreeGenerator.generateTagsDescStrFromJsonFile(pathToSrcJson, pathToDescList);
		Utils.writeStrToFile(tagsDescStr, pathToDescList);
		System.out.println("1. Generated tags descriptor");
		
		DdlPostgresStatementGenerator ddlStatementGenerator = new DdlPostgresStatementGenerator();
		DDLGenerator ddlGenerator = new DDLGenerator(ddlStatementGenerator);
		String generatedDDL =  ddlGenerator.generateDDLStr(pathToDescList);
		System.out.println("2. Generated ddl");

		String pathToDBConfig = getFullPathToDBConfigJson(pathToSrcJson);
		IDBConnProducer postgresConnProducer = new PostgresConnProducer();
		SQLScriptExecutor sqlScriptExecutor = new SQLScriptExecutor(postgresConnProducer);
		sqlScriptExecutor.executeSQLScriptFromStr(generatedDDL, pathToDBConfig);
		System.out.println("3. Executed ddl");
		
		DataLoader dataLoader = new DataLoader();
		String cuirrentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
		String insertSQLSTR = dataLoader.getInsSQLStmtsAsString(pathToSrcJson, pathToDescList, cuirrentTime);
		System.out.println("4. Generated insert ddl statements");
		sqlScriptExecutor.executeSQLScriptFromStr(insertSQLSTR, pathToDBConfig);
		System.out.println("5. Executed insert sql statements");
	}
	
	public static String getFullPathToDescListFile(String pathToSrcJson) {
		String path = getDirOfSrcJson(pathToSrcJson);
		String pathToDescList = path + CONFIGDIRNAME + DESKLISTFILENAME;
		return pathToDescList;
	}
	
	public static String getFullPathToDBConfigJson(String pathToSrcJson) {
		String path = getDirOfSrcJson(pathToSrcJson);
		String pathToDBconfig = path + CONFIGDIRNAME + DBCONFIGFILENAME;
		return pathToDBconfig;
	}
	
	public static String getDirOfSrcJson(String pathToSrcJson) {
		String path = FilenameUtils.getFullPath(pathToSrcJson);
		return path;
	}
}
