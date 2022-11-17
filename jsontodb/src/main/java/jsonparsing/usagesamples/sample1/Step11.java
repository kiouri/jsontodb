package jsonparsing.usagesamples.sample1;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import jsonparsing.ddl.TagsDescListGenerator;
import jsonparsing.postgresql.ToPostgresNameConvertor;

public class Step11 {
	
	public static String DESCLISTFILENAME = "serializedDescList.json";
	
	public static void main(String[] args) throws IOException {
		String pathToSrcJson = args[0];
		String pathToDescList = getFullPathToDescListFile(pathToSrcJson);
		TagsDescListGenerator tagsTreeGenerator = new TagsDescListGenerator(new ToPostgresNameConvertor());
		tagsTreeGenerator.generateTagsDescFileFromJsonFiles(pathToSrcJson,
				pathToDescList, pathToDescList);
		System.out.println("Generated file : " + pathToDescList);
	}
	
	public static String getFullPathToDescListFile(String pathToSrcJson) {
		String path = getDirOfSrcJson(pathToSrcJson);
		String pathToDescList = path + DESCLISTFILENAME;
		return pathToDescList;
	}
	
	public static String getDirOfSrcJson(String pathToSrcJson) {
		String path = FilenameUtils.getFullPath(pathToSrcJson);
		return path;
	}

}
