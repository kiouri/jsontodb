package com.kiouri.jsontodb.postgresql;

import java.util.List;

import com.kiouri.jsontodb.tagstodb.IDdlDBStatementGenerator;
import com.kiouri.jsontodb.utils.Utils;

public class DdlPostgresStatementGenerator implements IDdlDBStatementGenerator{

	@Override
	public  String generateCreateTableIfNotExistsDDL(String tableName, List<String> columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(tableName);
		sb.append("(\n");
		sb.append(Utils.ID);
		sb.append(" VARCHAR PRIMARY KEY);\n");
    	for (int i = 1; i < columns.size(); i++) {
    		sb.append("ALTER TABLE ");
    		sb.append(tableName);
    		sb.append('\n');
    		sb.append("ADD COLUMN IF NOT EXISTS ");
    		sb.append(columns.get(i));
    		sb.append(" VARCHAR;\n");
    	}
    	sb.append('\n');
    	return sb.toString();
	}

	@Override
	public String createForeignkey(String childTable, String parentTable) {
		if (parentTable.trim().length() == 0) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ");
		sb.append(childTable);
		sb.append(" DROP CONSTRAINT IF EXISTS fk_");
		sb.append(childTable);
		sb.append('_');
		sb.append(parentTable);
		sb.append(";\n");
		sb.append("ALTER TABLE ");
		sb.append(childTable);
		sb.append(" ADD CONSTRAINT fk_");
		sb.append(childTable);
		sb.append('_');
		sb.append(parentTable);
		sb.append('\n');
		sb.append("FOREIGN KEY (");
		sb.append(Utils.PARENT_ID);
		sb.append(") REFERENCES ");
		sb.append(parentTable);
		sb.append('(');
		sb.append(Utils.ID);
		sb.append(");\n");
		return sb.toString();
	}

}
