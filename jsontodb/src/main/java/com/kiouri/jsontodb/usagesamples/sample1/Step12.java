package com.kiouri.jsontodb.usagesamples.sample1;

import java.io.IOException;

import com.kiouri.jsontodb.ddl.DDLGenerator;
import com.kiouri.jsontodb.postgresql.DdlPostgresStatementGenerator;

public class Step12 {

	public static String DDLFILENAME = "postgres.ddl"; 
	
	public static void main(String[] args) throws IOException {
		String pathToSrcJson = args[0];
		String pathToDescList = Step11.getFullPathToDescListFile(pathToSrcJson);
		String pathToGeneratedDDL = getFullPathToDDLFile(pathToSrcJson);
		DdlPostgresStatementGenerator ddlStatementGenerator = new DdlPostgresStatementGenerator();
		DDLGenerator ddlGenerator = new DDLGenerator(ddlStatementGenerator);
		ddlGenerator.generateDDLFile(pathToDescList, pathToGeneratedDDL);
		System.out.println("Generated file : " + pathToGeneratedDDL);
	}
	
	public static String getFullPathToDDLFile(String pathToSrcJson) {
		String path = Step11.getDirOfSrcJson(pathToSrcJson);
		String pathToDescList = path + DDLFILENAME;
		return pathToDescList;
	}


}
