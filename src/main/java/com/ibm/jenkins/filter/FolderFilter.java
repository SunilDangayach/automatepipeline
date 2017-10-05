package com.ibm.jenkins.filter;
import java.io.File;
import java.io.FilenameFilter;

public class FolderFilter implements FilenameFilter{

	 @Override
     public boolean accept(File file, String name) {
         if(file.isDirectory()){
             return true;
         } else {
             return false;
         }
     }

}    