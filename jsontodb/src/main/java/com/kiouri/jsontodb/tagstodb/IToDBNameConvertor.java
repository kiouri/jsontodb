package com.kiouri.jsontodb.tagstodb;

/*
 * Converter of tag names contained in json structures 
 * into names of tables and columns of database tables
 */
public interface IToDBNameConvertor {

	/**
	 * In current implementation, there are no separate rules for forming the names
	 * of tables and columns. When implementing the method, return result must be
	 * suitable for both table names and column names.
	 */
	public String nameToDBName(String inString);

}
