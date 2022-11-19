package com.kiouri.jsontodb.ddl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kiouri.jsontodb.tagstodb.IToDBNameConvertor;
import com.kiouri.jsontodb.tagstodb.TagsNamesToDBNamesConvertor;
import com.kiouri.jsontodb.utils.SerDeTagsToJSON;
import com.kiouri.jsontodb.utils.Tag;
import com.kiouri.jsontodb.utils.Utils;

/**
 * Automatically analyze the json file and generate a relational model to store
 * the data it contains. Each internal json object of the analyzed file is
 * mapped to a database table. This mapping is represented as a json array, each
 * element of which describes a internal json object of the source file.
 */
public class TagsDescListGenerator {

	// result list of Tags (descriptors of json objects in the parsed json structure)
	private List<Tag> tags = new ArrayList<Tag>();
	// Technology stack containing the path to the currently processed json object
	private List<String> path = new ArrayList<String>();

	private IToDBNameConvertor toDBNameConvertor;

	public TagsDescListGenerator(IToDBNameConvertor toDBNameConvertor) {
		this.toDBNameConvertor = toDBNameConvertor;
	}

	/**
	 * Entry point
	 * 
	 * @param jsonStr          Json as string
	 * @param prevTagsDescList The result of processing a previous json file of
	 *                         similar structure. It can be null if this is the
	 *                         first parsing of a json file of this type, or if it
	 *                         is not necessary to maintain a consistent database
	 *                         structure for a set of similar json files, which may
	 *                         have some differences in structure (e.g., one-time
	 *                         parsing of files).
	 * @return List of descriptors for the child json objects of the parent json
	 *         object, represented as a string.
	 * @throws IOException
	 */
	public List<Tag> generateTagsDescListFromJSONStr(String jsonStr, List<Tag> prevTagsDescList)
			throws IOException {
		JSONObject obj = new JSONObject(jsonStr);
		recursiveParser(Utils.ROOTTAGNAME, obj, null);
		tags = removeDubleTags(tags);

		combineNewOldTags(tags, prevTagsDescList);

		TagsNamesToDBNamesConvertor tagsToSQL = new TagsNamesToDBNamesConvertor(toDBNameConvertor);
		tagsToSQL.convertTags(tags);

		setParentTagName(tags);
		return tags;
	}
	
	/**
	 * Entry point
	 * 
	 * @param pathToJson             Path to Json file
	 * @param pathToPrevTagsDescList Path to the file with results of processing a
	 *                               previous json file of similar structure. It can
	 *                               be null if this is the first parsing of a json
	 *                               file of this type, or if it is not necessary to
	 *                               maintain a consistent database structure for a
	 *                               set of similar json files, which may have some
	 *                               differences in structure (e.g., one-time
	 *                               parsing of files).
	 * @return Serialized as string a list of descriptors for the child json objects
	 *         of the parent json object.
	 * @throws IOException
	 */
	public String generateTagsDescStrFromJsonFile(String pathToJson, String pathToPrevTagsDescList) throws IOException {
		String myjson = Utils.getStrFromFile(pathToJson);
		List<Tag> oldTags = null;
		if (!(pathToPrevTagsDescList == null || pathToPrevTagsDescList.length() == 0)) {
			try {
				String oldTagsStr = Utils.getStrFromFile(pathToPrevTagsDescList);
				SerDeTagsToJSON serdetagstojson = new SerDeTagsToJSON();
				oldTags = serdetagstojson.deserFromJSON(oldTagsStr);
			} catch (Exception e) {

			}
		}
		List<Tag> tags = generateTagsDescListFromJSONStr(myjson, oldTags);
		String tagsStructureSerializedToJSONStr = serTagsJSON(tags);		
		return tagsStructureSerializedToJSONStr;
	}

	/**
	 * Entry point
	 * 
	 * @param pathToJson				Path to json file 
	 * @param pathToPrevTagsDescList 	Path to file with list of descriptors resulting from previous parsing
	 * @param pathToOutputTagsDescList	Path to file with results of the current parsing
	 * @throws IOException
	 */
	public void generateTagsDescFileFromJsonFiles(String pathToJson, String pathToOutputTagsDescList, String pathToPrevTagsDescList) throws IOException {
		String tagsStructureSerializedToJSONStr = generateTagsDescStrFromJsonFile(pathToJson, pathToPrevTagsDescList);
		Utils.writeStrToFile(tagsStructureSerializedToJSONStr, pathToOutputTagsDescList);
		return;
	}	

	/**
	 * Recursive parsing of json structure
	 * The elementary attributes of an object form the structure of a database table. 
	 * Complex objects are transferred for further processing (recursively).
	 * Parsing results are stored in the instance variable List<Tag> tags
	 * 
	 * @param jsonObjname 	Name of the currently processed json object
	 * @param jsonObj		Currently processed json object
	 * @param parentTag		parent object descriptor
	 */
	private void recursiveParser(String jsonObjname, JSONObject jsonObj, Tag parentTag) {
		List<String[]> attrs = new ArrayList<String[]>();
		attrs.add(new String[] { Utils.ID, null, null });
		attrs.add(new String[] { Utils.PARENT_ID, null, null });
		attrs.add(new String[] { Utils.JOB_ID, null, null });

		List<Pair<String, Object>> nestedObjs = new ArrayList<Pair<String, Object>>();

		path.add(jsonObjname);

		String[] NestedJsonObjNames = JSONObject.getNames(jsonObj);
		for (String name : NestedJsonObjNames) {
			Object NestedJsonObj = jsonObj.get(name);
			if ((NestedJsonObj instanceof org.json.JSONArray) || NestedJsonObj instanceof org.json.JSONObject) {
				nestedObjs.add(Pair.with(name, NestedJsonObj));
			} else {
				attrs.add(new String[] { name, null, null });
			}
		}

		Tag tag = new Tag(jsonObjname, Utils.deepCloneList(path), attrs, parentTag);
		tags.add(tag);

		for (Pair<String, Object> nestedObj : nestedObjs) {
			if (nestedObj.getValue1() instanceof org.json.JSONObject) {
				recursiveParser(nestedObj.getValue0(), (JSONObject) nestedObj.getValue1(), tag);
			}
			if (nestedObj.getValue1() instanceof org.json.JSONArray) {
				JSONArray arr = (JSONArray) nestedObj.getValue1();
				for (int i = 0; i < arr.length(); i++) {
					Object arrobj = arr.get(i);
					if (arrobj instanceof org.json.JSONObject) {
						JSONObject arrJsonObj = (JSONObject) arrobj;
						arrJsonObj.put(Utils.LINENUMBER, "lineID");
						recursiveParser(nestedObj.getValue0(), arrJsonObj, tag);
					} else {
						JSONObject newobj = new JSONObject();
						newobj.put(nestedObj.getValue0(), arrobj);
						recursiveParser(nestedObj.getValue0(), newobj, tag);
					}
				}
			}
		}
		path.remove(path.size() - 1);
	}


	/**
	 * Combining the list of descriptors for the current and previous iterations of
	 * the analysis of json files of similar structure, taking into account the
	 * possibility of some differences in these structures
	 * 
	 * @param newTags List of "new" descriptors
	 * @param oldTags List of "old" descriptors
	 * @return a combined list of new and old descriptors, preserving continuity in
	 *         the names of tables and columns in the database
	 */
	private List<Tag> combineNewOldTags(List<Tag> newTags, List<Tag> oldTags) {
		if (oldTags == null || oldTags.size() == 0) {
			return newTags;
		}
		List<Tag> usedOldTags = new ArrayList<Tag>();
		for (Tag newTag : newTags) {
			String currentPathToTag = newTag.getTagPathAsString();
			List<Tag> sameLevelOldTags = getTagsByPath(oldTags, currentPathToTag);
			for (Tag currentOldTag : sameLevelOldTags) {
				// The new tag contains all the attributes of the old one and maybe a little more
				if(newTag.isContainsTag(currentOldTag)) {
					newTag.sqlName = currentOldTag.sqlName;
					for (String[] newAttr : newTag.attrs) {
						String[] correspondOldAttr = getAttrByName(currentOldTag.attrs, newAttr[0]);
						if (correspondOldAttr != null) {
							newAttr[1] = correspondOldAttr[1];
						}
					}
					usedOldTags.add(currentOldTag);
					//The old tag contains all the attributes of the new one and maybe a little more
				} else if (currentOldTag.isContainsTag(newTag)) {
					newTag.attrs = currentOldTag.attrs;
					usedOldTags.add(currentOldTag);
				}
			}
		}
		oldTags.removeAll(usedOldTags);
		newTags.addAll(oldTags);
		return newTags;
	}

	private String[] getAttrByName(List<String[]> attrs, String attrName) {
		for (String[] attr : attrs) {
			if (attr[0].equals(attrName)) {
				return attr;
			}
		}
		return null;
	}

	private List<Tag> getTagsByPath(List<Tag> tags, String path) {
		List<Tag> result = new ArrayList<Tag>();
		for (Tag tag : tags) {
			String currentPath = tag.getTagPathAsString();
			if (path.equals(currentPath)) {
				result.add(tag);
			}
		}
		return result;
	}

	private void setParentTagName(List<Tag> tags) {
		for (Tag tag : tags) {
			if (tag.parentTag == null || tag.parentTag.sqlName == null) {
				tag.parentTAGSQLName = "";
			} else {
				tag.parentTAGSQLName = tag.parentTag.sqlName;
			}
		}
	}

	private String serTagsJSON(List<Tag> tags) {
		SerDeTagsToJSON serdetagstoJSON = new SerDeTagsToJSON();
		String result = serdetagstoJSON.serTags(tags);
		return result;
	}

	private List<Tag> removeDubleTags(List<Tag> tags) {
		List<Tag> result = new ArrayList<Tag>();
		for (Tag tag : tags) {
			boolean exists = false;
			Tag replasedTag = null;
			for (Tag resultTag : result) {
				replasedTag = resultTag;
				String tagPath = tag.getTagPathAsString();
				String resultTagPath = resultTag.getTagPathAsString();
				if (tagPath.equals(resultTagPath) && resultTag.isContainsTag(tag)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				result.add(tag);
			} else {
				replaceReferences(tags, tag, replasedTag);
			}
		}
		return result;
	}

	// replace all references to the removed tag with references to the tag that remains
	private void replaceReferences(List<Tag> tags, Tag oldTag, Tag newTag) {
		for (Tag tag : tags) {
			if (tag.parentTag != null && tag.parentTag == oldTag) {
				tag.parentTag = newTag;
			}
		}
	}

}
