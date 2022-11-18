package com.kiouri.jsontodb.postgresql;

import java.util.Set;

import com.kiouri.jsontodb.tagstodb.IToDBNameConvertor;
import com.kiouri.jsontodb.utils.Utils;

public class ToPostgresNameConvertor implements IToDBNameConvertor{
	
	public static Set<String> RESERVED = Utils.newHashSet(
			"All","ANALYSE","ANALYZE","AND","ANY","ARRAY","AS","ASC","ASYMMETRIC",
			"AUTHORIZATION","BOTH","CASE","CAST","CHECK","COLLATE","COLUMN",
			"CONSTRAINT","CREATE","CURRENT_CATALOG","CURRENT_DATE","CURRENT_ROLE",
			"CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER","DEFERRABLE","DESC",
			"DISTINCT","DO","ELSE","END","EXCEPT","FALSE","FOREIGN","FREEZE","FROM",
			"FULL","GRANT","GROUP","HAVING","ILIKE","IN","INITIALLY","INNER",
			"INTERSECT","INTO","IS","ISNULL","JOIN","LATERAL","LEADING","LEFT",
			"LOCALTIME","LOCALTIMESTAMP","NOT","NULL","OFFSET","ON","ONLY","OR",
			"ORDER","OUTER","OVERLAPS","PLACING","PRIMARY","REFERENCES","RETURNING",
			"RIGHT","SELECT","SESSION_USER","SIMILAR","SOME","SYMMETRIC","TABLE",
			"TABLESAMPLE","THEN","TO","TRAILING","TRUE","UNIQUE","USER","USING",
			"VARIADIC","VERBOSE","WHEN","WHERE","WINDOW","WITH");
    
	@Override
	public String nameToDBName(String inString) {
		return firstDigitsToRiomanReduceToNoReserved(inString, 55, RESERVED);
	}

	private static String firstDigitsToRiomanReduceToNoReserved(String inString, int outlength, Set<String> RESERVED) {
		String result = Utils.correctReserved(Utils.reduceString(Utils.firstDigitsToRioman(inString), outlength), outlength, RESERVED).replaceAll("-", "");
		return result;
	}
	
}
