package com.kiouri.jsontodb.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tag {

	public String tagName;
	public String sqlName;
	public List<String> jsonPathList;
	public List<String[]> attrs; 	
	
	public Tag parentTag;	
	public String parentTAGSQLName;	
	public Tag(){}
	
	public Tag(String tagName, List<String> jsonPathList, List<String[]> attrs, Tag parentTag){
		this.tagName = tagName;
		this.jsonPathList = jsonPathList;
		this.attrs = attrs;		
		this.parentTag = parentTag;
	}
		
		
	public boolean isContainsTag(Tag tag2) {
		
		Set<String> tag1AttrNamesSet = tgAttrNamesToSet(this);
		Set<String> tag2AttrNamesSet = tgAttrNamesToSet(tag2);
		if (tag1AttrNamesSet.containsAll(tag2AttrNamesSet)) {
			return true;
		}
		return false;
	}
	
	//set SQLnames for current tag from tag2 
	public void mergeTagWith(Tag tag2) {
		this.sqlName = tag2.sqlName;
		for (String[] attr : attrs) {
			 String[] tag2attr = tag2.getAttrByName(attr[0]);
			 attr[1] = tag2attr[1]; 
		}
	}
	
	public String[] getAttrByName(String attrName) {
		for (String[] attr : attrs) {
			if (attr[0].equals(attrName)) {
				return attr;
			}
		}
		return null;
	}
	
	private Set<String> tgAttrNamesToSet(Tag tag) {
		Set<String> tagAttrNamesSet = new HashSet<String>();
		for (String[] attr : tag.attrs) {
			tagAttrNamesSet.add(attr[0]);
		}
		return tagAttrNamesSet;
	}
	
	public String tagToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("----- Tag: ");
		sb.append(tagName);
		sb.append(" Table: ");
		sb.append(sqlName);
		sb.append(" ----- \n");
		sb.append("path : ");
		sb.append(getTagPathAsString());
		sb.append('\n');
		for (int i = 0; i< attrs.size(); i++) {
			sb.append('\t');
			sb.append(attrs.get(i)[0]);
			sb.append(" : ");
			sb.append(attrs.get(i)[1]);
			sb.append(" : "); 
			sb.append(attrs.get(i)[2]);
			sb.append('\n');
		}
		return sb.toString();
	}		

	public String getTagPathAsString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < jsonPathList.size(); i++) {
			sb.append(jsonPathList.get(i));
			if (i < (jsonPathList.size() - 1)) {
				sb.append("->");
			}
		}
		return sb.toString();
	}
	
	public String getTagPathToAttrAsString(String[] attr) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTagPathAsString());
		sb.append("->");
		sb.append(attr[0]);
		return sb.toString(); 
	}	

}
