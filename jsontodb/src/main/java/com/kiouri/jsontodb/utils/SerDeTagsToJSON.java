package com.kiouri.jsontodb.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SerDeTagsToJSON {
	
//////////////////// deserialization ////////////////////////////

	public List<Tag> deserFromJSON(String myjson){
		JSONObject jsonObj = new JSONObject(myjson);
//		String[] names = JSONObject.getNames(jsonObj);
		JSONArray rootArray = jsonObj.getJSONArray("tags");
		List<Tag> tags = new ArrayList<Tag>(); 
		for (int i = 0; i < rootArray.length(); i++) {
			JSONObject arrayJsonObj = rootArray.getJSONObject(i);
			JSONObject tagJsonObj = arrayJsonObj.getJSONObject("tag");
			String tagName = tagJsonObj.getString("tagName");
			String sqlName = tagJsonObj.getString("sqlName");
//System.out.println(tagName);			
			String parentTAGSQLName = tagJsonObj.getString("parentTAGSQLName");
			ArrayList<String> tagPathList = new ArrayList<String>();     
			JSONArray tagPaths = tagJsonObj.getJSONArray("tagPath"); 
			if (tagPaths != null) { 
			   int len = tagPaths.length();
			   for (int k=0 ; k < len; k++){ 
				   tagPathList.add(tagPaths.getString(k));
			   } 
			} 	
			JSONArray arrs = tagJsonObj.getJSONArray("attrs");
			List<String[]> arrsList = new ArrayList<String[]>();
			for (int j = 0; j < arrs.length(); j++) {
				JSONObject attr = arrs.getJSONObject(j);				
				String attrName = attr.getString("attrName");
				String attrSqlName = attr.getString("attrSqlName");
				String[] attrArr = new String[] {attrName,attrSqlName,""}; 
				arrsList.add(attrArr);
			}	
				
			Tag tag = new Tag();
			tag.tagName = tagName;
			tag.sqlName = sqlName;
			tag.jsonPathList = tagPathList;
			tag.attrs = arrsList;
			tag.parentTAGSQLName = parentTAGSQLName; 
			tags.add(tag);
			
		}		
		return tags;
	}
		
///////////////////// serialization ////////////////////////////
	public String serTags(List<Tag> tags){
		JSONObject rootJSON = new JSONObject();
		JSONArray attrArray = new JSONArray();
		rootJSON.put("tags", attrArray);
		for (Tag tag : tags) {
			JSONObject tagObject = serTag(tag);
			attrArray.put(tagObject);
		}
		return rootJSON.toString(4);
	}
	
	public JSONObject serTag(Tag tag) {
		JSONObject rootJSON = new JSONObject();	
		JSONObject tagObject = new JSONObject();
		rootJSON.put("tag", tagObject);
		
		tagObject.put("tagName", tag.tagName);
		tagObject.put("sqlName", tag.sqlName);
		tagObject.put("parentTAGSQLName", tag.parentTAGSQLName);
		
		
		JSONArray pathArray = new JSONArray(); 
		List<String> pathList = tag.jsonPathList;
		for (String pathElement : pathList) {
		     pathArray.put(pathElement);
		}
		tagObject.put("tagPath", pathArray);	
		
		JSONArray attrArray = new JSONArray(); 
		tagObject.put("attrs", attrArray);
		
	    List <String[]> attrs = tag.attrs;
	    for (String[] attr : attrs) {
	    	JSONObject attrObject = new JSONObject();
			attrObject.put("attrName", attr[0]);
			attrObject.put("attrSqlName", attr[1]);
			attrArray.put(attrObject);
	    }		
		return rootJSON;		
	}
	
	public static void main(String[] args) throws IOException {
//		SerDeTagsToJSON serdeTagsToJSON = new SerDeTagsToJSON();
//		String tagsJSON = serdeTagsToJSON.serTags(null);
//		System.out.println(tagsJSON);
		
		String myjson = Utils.getStrFromFile("D:\\myWork\\workspace1\\json_parsing\\src\\main\\resources\\json\\serialized.json");		
		SerDeTagsToJSON serdeTagsToJSON = new SerDeTagsToJSON();
		List<Tag> tags = serdeTagsToJSON.deserFromJSON(myjson);
		String deserTags = Utils.tagsToString(tags);
		System.out.println(deserTags);

	}
}
