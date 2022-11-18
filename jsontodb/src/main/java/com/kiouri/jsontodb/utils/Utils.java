package com.kiouri.jsontodb.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import ru.homyakin.iuliia.Schemas;
import ru.homyakin.iuliia.Translator;

public class Utils {
	
	public static String ID = "iiiddd";
	public static String PARENT_ID = "parent_iiiddd";	
	public static String JOB_ID = "job_iiiddd";
	public static String LINENUMBER = "myLineNumberrr";
	public static String ROOTTAGNAME = "root";
	
	@SafeVarargs
	public static final <T> Set<T> newHashSet(T... objs) {
	    Set<T> set = new HashSet<T>();
	    Collections.addAll(set, objs);
	    return set;
	}
	
	public static String reduceString(String inString, int outlength) {
		if (inString == null || inString.length() < outlength) {
			return inString;
		}
		String result = 
				inString.substring(0, outlength/2) + 
				inString.substring(inString.length() - (outlength - outlength/2));
		return result;
	}

	
	public static String firstDigitsToRioman(String inString) {
		StringBuffer fuirstDigits = new StringBuffer();
		for (int i = 0; i < inString.length(); i++) {
			if (!Character.isDigit(inString.charAt(i))) {
				break;
			} else {
				fuirstDigits.append(inString.charAt(i));
			}
		}
		if (fuirstDigits.length() == 0) {
			return inString;
		}		
		return ArabicToRioman.arabicToRoman(Integer.parseInt(fuirstDigits.toString())) + 
				inString.substring(fuirstDigits.length());
	}
	
	/**
	 * 
     * Correct usage reserved words of DB
	 */
	public static String correctReserved(String inString, int outlength, Set<String> RESERVED) {
		if (RESERVED.contains(inString.toUpperCase())) {
			if (inString.length() < outlength) {
				inString  += "1";
			} else {
				inString = inString.substring(0, inString.length() - 1) + "1";
			}
		}
		return inString;
	}

	public static String getStringFromFileInResources(String fname, Class<?> clazz) throws IOException {
		ClassLoader classLoader = clazz.getClassLoader();
		File file = new File(classLoader.getResource(fname).getFile());
		InputStream inputStream = new FileInputStream(file);
		String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		inputStream.close();
		return text;
	}
	
	public static String getStrFromFile(String pathToFile) throws IOException {
		String content = Files.readString(Path.of(pathToFile), StandardCharsets.UTF_8);
		return content;
	}
			
	public static String tagsToString(List<Tag> tags) {
		String result = "";
		for (Tag tag : tags) {
			result += tag.tagToString();
		}
		return result;
	}

	public static <T> List<T> deepCloneList(List<T> inarr){
		List<T> result = new ArrayList<T>();
		for (T t : inarr) {
			result.add(t);
		}
		return result;
	}
	    
    //Возвращаем Tag, который заменит удаляемый (или null, если замены нет)
    public static Tag isWiderTagWithSamePathConteinsInList(Tag tag, List<Tag> tags) {
    	if (tags == null) {
    		return null;
    	}
    	for (Tag tagFromList : tags) {
    		//не сравниваем tag'и с разными путями
        	if(!tag.getTagPathAsString().equals(tagFromList.getTagPathAsString())) {
        		continue;
        	}    		
            //не сравниваем tag'и с разными именами
    		if(!tag.tagName.equals(tagFromList.tagName)) {
    			continue;
    		}
    		//если у tag'а из списка меньше атрибутов, чем у рассматриваемого - он тоже не подходит
    		if(tag.attrs.size() > tagFromList.attrs.size()) {
    			continue;
    		}
    		//проверяем, что для каждого атрибута обрабатываемого tag'а есть аналогичный у tag'а из списка
    		boolean cirrentTagFromLisrtIsValid = true;
    		for (String[] attrEl : tag.attrs) {
    			boolean attrExists = false;
    			for(String[] attrElFromList : tagFromList.attrs) {
    				if(attrEl[0].equals(attrElFromList[0])) {
    					attrExists = true;
    					break;
    				}
    			}
    			if(!attrExists) { //если какой-то атрибут не существует в tag'е расширенного списка, то надо переходить к след. элемениу расширенного списка
    				cirrentTagFromLisrtIsValid = false;
    				break;
    			}
    		}
    		if (cirrentTagFromLisrtIsValid) {
    			return tagFromList;
    		}
    	}
    	return null;
    }
    
    /**
     * Удаление tag'а, если есть другой tag с такимже путем, но 
     * с идентичным или более широким набором атрибутов !!!  
     */
    public static List<Tag> removeNarrowTagsWithSamePath(List<Tag> tags){
    	List<Tag> resultTags = new ArrayList<Tag>();
    	for (Tag tag : tags) {
    		Tag tagForUse = isWiderTagWithSamePathConteinsInList(tag, resultTags);
    		if (tagForUse == null) {
    			resultTags.add(tag);
    		} else {
    			//заменить tag (удаленный) на тот, который остался к использованию во всех ссыдках parentTag   			
    			for (Tag tag2 : tags) {
    				if (tag2.parentTag == tag) {
    					tag2.parentTag = tagForUse; 
    				}
    			}
    			
    		}
    	}
    	return resultTags;
    }
    
    public static void writeStrToFile(String str, String pathToFile) throws FileNotFoundException {
    	PrintWriter out = new PrintWriter(pathToFile);
    	out.print(str);
    	out.close();
    }
    
    public static String disableAllTriggersForTable(String tableName) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("ALTER TABLE ");
    	sb.append(tableName);
    	sb.append("  DISABLE TRIGGER ALL;");
    	return sb.toString();
    }
    
    public static String enableAllTriggersForTable(String tableName) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("ALTER TABLE ");
    	sb.append(tableName);
    	sb.append("  ENABLE TRIGGER ALL;");
    	return sb.toString();
    }
    
    public static String disableAllTriggersForAllTables(List<Tag> ddlTags) {
    	StringBuilder sb = new StringBuilder();
    	for (Tag tag : ddlTags) {
    		sb.append(disableAllTriggersForTable(tag.sqlName));
    		sb.append('\n');   		
    	}
    	String result = sb.toString();
    	return result;
    }
    
    public static String enableAllTriggersForAllTables(List<Tag> ddlTags) {
    	StringBuilder sb = new StringBuilder();
    	for (Tag tag : ddlTags) {
    		sb.append(enableAllTriggersForTable(tag.sqlName));
    		sb.append('\n');   		
    	}
    	String result = sb.toString();
    	return result;
    }
    
    public static List<String> getFullPathListForDir(String dirPath, String wildcard){
    	File dir = new File(dirPath); 
    	FileFilter fileFilter = new WildcardFileFilter(wildcard, IOCase.INSENSITIVE);  
//    	FileFilter fileFilter = new WildcardFileFilter("*.json", IOCase.INSENSITIVE);  
    	File[] fileList = dir.listFiles(fileFilter);   
    	List<String> result = new ArrayList<String>();
    	for (int i = 0; i < fileList.length; i++) {
    		result.add(fileList[i].getAbsolutePath());
    	}
    	return result;
    }
    
    public static void removeFile(String filePath) {
    	File file = new File(filePath);
    	file.delete();
    }
    
    public static String transliterate(String inStr) {
    	Translator translator = new Translator(Schemas.ICAO_DOC_9303);
        return translator.translate(inStr); 
    }
        
//    /////////////////////////////////////////////
//	public static void main(String[] args) {		
//		System.out.println(transliterate("Юлия"));		
//	}

}
