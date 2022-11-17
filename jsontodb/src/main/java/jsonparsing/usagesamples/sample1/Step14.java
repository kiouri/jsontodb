package jsonparsing.usagesamples.sample1;

import java.text.SimpleDateFormat;
import java.util.Date;

import jsonparsing.dataloader.DataLoader;

public class Step14 {
	
	public static String SQLFILENAME = "postgres.sql"; 

	public static void main(String[] args) throws Exception {
		String pathToSrcJson = args[0];
		String pathToDescList = Step11.getFullPathToDescListFile(pathToSrcJson);
		String pathToGeneratedSQL = getFullPathToSQLFile(pathToSrcJson);
		DataLoader dataLoader = new DataLoader();
		String cuirrentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
		dataLoader.getInsSQLStmtsFromFilesToFiles(pathToSrcJson, pathToDescList, pathToGeneratedSQL,  cuirrentTime);
		System.out.println("Generated file : " + pathToGeneratedSQL);
	}

	public static String getFullPathToSQLFile(String pathToSrcJson) {
		String path = Step11.getDirOfSrcJson(pathToSrcJson);
		String pathToDescList = path + SQLFILENAME;
		return pathToDescList;
	}


}
