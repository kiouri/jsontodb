package com.kiouri.jsontodb.tagstodb;

import java.util.List;

public interface IDdlDBStatementGenerator {
	public String generateCreateTableIfNotExistsDDL(String tableName, List<String> columns);
	public String createForeignkey (String childTable, String parentTable);
}
