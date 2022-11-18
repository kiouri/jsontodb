package com.kiouri.jsontodb.ddl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kiouri.jsontodb.tagstodb.IDdlDBStatementGenerator;
import com.kiouri.jsontodb.utils.SerDeTagsToJSON;
import com.kiouri.jsontodb.utils.Tag;
import com.kiouri.jsontodb.utils.Utils;

/**
 * Generating DDL scripts taking into account the specifics of a particular type
 * of database
 *
 */
public class DDLGenerator {
	
	private IDdlDBStatementGenerator ddlStatementGenerator;
	
	public DDLGenerator(IDdlDBStatementGenerator ddlStatementGenerator) {
		this.ddlStatementGenerator = ddlStatementGenerator;
	}
	
	public String generateDDLStr(List<Tag> tags) {       
		StringBuilder sb = new StringBuilder();
		for (Tag tag : tags) {
			List<String> columnsNames = new ArrayList<String>();
			for (String[] columns : tag.attrs) {
				columnsNames.add(columns[1]);
			}
			String createTableStmt = ddlStatementGenerator.generateCreateTableIfNotExistsDDL(tag.sqlName, columnsNames);
			sb.append(createTableStmt);
			sb.append('\n');
			
			String createFKStmt =  ddlStatementGenerator.createForeignkey(tag.sqlName, tag.parentTAGSQLName);
			sb.append(createFKStmt);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public String generateDDLStr(String pathToSerializedDescListFile) throws IOException {
		SerDeTagsToJSON serdetagstoJSON = new SerDeTagsToJSON();
		String serializedJSON = Utils.getStrFromFile(pathToSerializedDescListFile);
		List<Tag> tags = serdetagstoJSON.deserFromJSON(serializedJSON);
		String result = generateDDLStr(tags);
		return result;
	}
	
	public void generateDDLFile(List<Tag> tags, String pathToDDLFile) throws FileNotFoundException {
		String ddlStr = generateDDLStr(tags);
		Utils.writeStrToFile(ddlStr, pathToDDLFile);
	}
	
	public void generateDDLFile(String pathToSerializedDescListFile, String pathToDDLFile) throws IOException {
		String ddlStr = generateDDLStr(pathToSerializedDescListFile);
		Utils.writeStrToFile(ddlStr, pathToDDLFile);
	}

}
