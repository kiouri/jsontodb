package com.kiouri.jsontodb.dataloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kiouri.jsontodb.utils.SerDeTagsToJSON;
import com.kiouri.jsontodb.utils.Tag;
import com.kiouri.jsontodb.utils.Utils;

/**
 * Generating insert SQL scripts.
 * When generating insert SQL statements, the database structure previously
 * prepared for this json structure is used. DB model (tables and columns) are
 * presented as a json structure. The description of the database contains not
 * only a description of tables and columns, but also their relationship with
 * internal json objects of analyzed json file. Thus, knowing which internal
 * tags the data in the analyzed json file belongs to, we can link them with database
 * tables and columns. Insert SQL statements are generated based on this
 * information.
 */
public class DataLoader {
	
	private static Map<Integer, String> lineIDMap = new HashMap<Integer, String>();

	// result list of Tags (descriptors of json objects in the parsed json structure)
	private List<Tag> tags = new ArrayList<Tag>();
	// Technology stack containing the path to the currently processed json object
	private List<String> path = new ArrayList<String>();
	
	// list of descriptors of DB artifacts
	private List<Tag> ddlTags;


	public List<String> getInsSQLStmtsAsList(String myjson, List<Tag> inDDLTags,  String jobID) throws Exception {
		JSONObject obj = new JSONObject(myjson);
		this.ddlTags = inDDLTags;
		//Preparing tag list without db  specific
		recursiveLoader(Utils.ROOTTAGNAME , obj, "", jobID);		
		List<String> insertStmts = new ArrayList<String>();		
		for (Tag tag : tags) {
			Tag sutableDDLTag = getSutableDLLTags(tag);
			tag.mergeTagWith(sutableDDLTag);
			String insertStmt = createInsertStmt(tag);
			insertStmts.add(insertStmt);
		}	
		return insertStmts;
	}
	
	public String getInsSQLStmtsAsString(String myjson, List<Tag> inDDLTags, String jobID) throws Exception {
		List<String> stmts = getInsSQLStmtsAsList(myjson, inDDLTags, jobID);
		StringBuffer sb = new StringBuffer();
		sb.append(Utils.disableAllTriggersForAllTables(inDDLTags));
		for (String stmt : stmts) {
			sb.append(stmt);
			sb.append('\n');
		}
		sb.append(Utils.enableAllTriggersForAllTables(inDDLTags));
		String insertStmtsStr = sb.toString();
		return insertStmtsStr;
	}

	public String getInsSQLStmtsAsString(String pathToSrcJson, String pathToDescList, String jobID) throws Exception {
		String srcJsonStr = Utils.getStrFromFile(pathToSrcJson);
		SerDeTagsToJSON serdetagstoJSON = new SerDeTagsToJSON();
		String serializedJSON = Utils.getStrFromFile(pathToDescList);
		List<Tag> tagsDDL = serdetagstoJSON.deserFromJSON(serializedJSON);
		String insertStmtsStr = getInsSQLStmtsAsString(srcJsonStr, tagsDDL, jobID);
		return insertStmtsStr;
	}
	
	public void getInsSQLStmtsFromFilesToFiles(String pathToSrcJson, String pathToDescList, String pathToGeneratedSQL, String jobID) throws Exception {
		String insertStmtsStr = getInsSQLStmtsAsString(pathToSrcJson, pathToDescList, jobID);
		Utils.writeStrToFile(insertStmtsStr, pathToGeneratedSQL);		
	}
	
	
	/**
	 * Create and  fill tags list without db specific
	 */
	private void recursiveLoader(String jsonObjname, JSONObject jsonObj, String parentID, String jobID) {
		List <String[]> columns = new ArrayList<String[]>();
		String currentID = UUID.randomUUID().toString();
		columns.add(new String[]{Utils.ID, null, currentID});
		columns.add(new String[]{Utils.PARENT_ID, null,  parentID});
		columns.add(new String[]{Utils.JOB_ID, null, jobID});			
		path.add(jsonObjname);
		String[] names = JSONObject.getNames(jsonObj);
		List<Pair<String, Object>> internalObj = new ArrayList<Pair<String, Object>>();	
		for (String name : names) {
			Object intobj = jsonObj.get(name);
			if ((intobj instanceof org.json.JSONArray) || intobj instanceof org.json.JSONObject) {
				internalObj.add(Pair.with(name, intobj));
			} else {
				columns.add(new String[]{name, null, intobj.toString()});
			}
		}		
		Tag tag = new Tag(jsonObjname, Utils.deepCloneList(path), columns, null);
		tags.add(tag);
		for (Pair<String, Object> complexobj : internalObj) {
			if (complexobj.getValue1() instanceof org.json.JSONObject) {
				recursiveLoader(complexobj.getValue0(), (JSONObject) complexobj.getValue1(), currentID, jobID);
			}
			if (complexobj.getValue1() instanceof org.json.JSONArray) {
				JSONArray arr = (JSONArray) complexobj.getValue1();
				for (int i = 0; i < arr.length(); i++) {
					Object arrobj = arr.get(i);					
					if (arrobj instanceof org.json.JSONObject) {
						JSONObject arrJsonObj = (JSONObject) arrobj;
						String lineID = getLineID(i);
						arrJsonObj.put(Utils.LINENUMBER, lineID);
						recursiveLoader(complexobj.getValue0(), arrJsonObj, currentID, jobID);
					} else {
						JSONObject newobj = new JSONObject();
						newobj.put(complexobj.getValue0(), arrobj);
						recursiveLoader(complexobj.getValue0(), newobj, currentID, jobID);
					}
				}
			}
		}
		path.remove(path.size() -1);
	}

	private Tag getSutableDLLTags(Tag tag) {		
		for (Tag ddlTag : ddlTags) {
			String tagFullPath = tag.getTagPathAsString();
			String currentDDLFullPath = ddlTag.getTagPathAsString();
			if (tagFullPath.equals(currentDDLFullPath)) {
				if (ddlTag.attrs.size() < tag.attrs.size()) {
					continue;
				}
				boolean isSutableDDLTag = true;
				for (int i = 0; i < tag.attrs.size(); i++) {
					if (!isDdlAttrsContainsAttr(ddlTag.attrs, tag.attrs.get(i))) {
						isSutableDDLTag = false;
						break; 
					}
				}
				if(isSutableDDLTag) {
				   return ddlTag;
				}
			}
		}
		return null;
	}
	
	private String getLineID(Integer key) {
		String lineID = lineIDMap.get(key);
		if (lineID == null) {
			lineID = UUID.randomUUID().toString();
			lineIDMap.put(key, lineID);
		}
		return lineID;
	}
	
	// is exists tag attribute in DDL tag attributes 
	boolean isDdlAttrsContainsAttr(List<String[]> ddlAttrs, String[] attr) {
		for (String[] ddlAttr : ddlAttrs) {
			if (ddlAttr[0].equals(attr[0])) {
				return true;
			}
		}
		return false;
	}
	
    public static String createInsertStmt(Tag tag) {
    	StringBuffer result = new StringBuffer();
    	result.append("INSERT INTO ");
    	result.append(tag.sqlName);
    	StringBuffer resultcol = new StringBuffer();
    	StringBuffer resultval = new StringBuffer();
    	resultcol.append(" (\n");
    	resultval.append(" (\n");
    	for (int i = 0; i < tag.attrs.size(); i++) {
    		resultcol.append(tag.attrs.get(i)[1]);
    		resultval.append('\'');
    		resultval.append(tag.attrs.get(i)[2]);
    		resultval.append('\'');
    		if (i < (tag.attrs.size() - 1)) {
    			resultcol.append(",\n");
    			resultval.append(",\n");
    		} else {
    			resultcol.append(")\n");
    			resultval.append(");\n");
    		}    		
    	}
    	result.append(resultcol);    	
    	result.append(" VALUES ");
    	result.append(resultval);
    	return result.toString();    	
    }	
}

