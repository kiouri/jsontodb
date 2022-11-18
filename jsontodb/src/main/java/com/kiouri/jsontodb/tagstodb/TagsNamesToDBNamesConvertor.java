package com.kiouri.jsontodb.tagstodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kiouri.jsontodb.utils.Tag;

/**
 * This utility converts tag names to names of database artifacts (tables and 
 * columns). Table names must be unique among the set of all tables generated 
 * during structure processing. Column names must be unique within a table.
 * The naming rules must match the rules adopted for the selected database. 
 * The class that implements the interface IJsonToDBNameConvertor is responsible 
 * for the formation of names for a particular database. An instance of this 
 * class is passed as a parameter to constructor of class TagsToSQL. 
 *
 */
public class TagsNamesToDBNamesConvertor {

	IToDBNameConvertor toDBNameConvertor;

	// tag (table -> full path to json object as string)
	private Map<String, String> tablePathMap = new HashMap<String, String>();

	public TagsNamesToDBNamesConvertor(IToDBNameConvertor tagNameToDBNameConvertor) {
		this.toDBNameConvertor = tagNameToDBNameConvertor;
	}
	
	public void convertTagNamesToSQLNames(Tag tag) {
		if (!(tag.sqlName == null || tag.sqlName.trim().length() == 0)) {
			convertTagNameToColumnName(tag);
		}
		String tagFullPath = tag.getTagPathAsString();
		String tableName = toDBNameConvertor.nameToDBName(tag.tagName);
		tableName = createUnicName(tableName, tagFullPath, tablePathMap); 
		tag.sqlName = tableName;
		// transform attr names
		convertTagNameToColumnName(tag);
	}

	private String createUnicName(String artefactName, String fullPathToArtefact,
			Map<String, String> artefactToPathMap) {
		String resultName = artefactName;
		int i = 0;
		while (artefactToPathMap.get(resultName) != null) {
			resultName = toDBNameConvertor.nameToDBName(artefactName + i);
			i++;
			if (i > 99) {
				resultName = UUID.randomUUID().toString();
				break;
			}
		}
		artefactToPathMap.put(resultName, fullPathToArtefact);
		return resultName;
	}

	private void convertTagNameToColumnName(Tag tag) {
		// tag (column -> full path to tag)
		Map<String, String> columnPathMap = new HashMap<String, String>();
		for (String[] triplet : tag.attrs) {
			String attrSQLName = triplet[1];
			if (!(attrSQLName == null || attrSQLName.trim().length() == 0)) {
				continue;
			}
			String tagColumn = triplet[0];
			String tagFullPath = tag.getTagPathToAttrAsString(triplet);
			String columnName = toDBNameConvertor.nameToDBName(tagColumn);
			columnName = createUnicName(columnName, tagFullPath, columnPathMap);
			triplet[1] = columnName;
		}
	}

	public void convertTags(List<Tag> tags) {
		for (Tag tag : tags) {
			convertTagNamesToSQLNames(tag);
		}
	}

}
