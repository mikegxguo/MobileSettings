package com.mitac.mobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.media.MediaScannerConnection;

public class zipFileUtil{
	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
	private static boolean mStopFlag = false;

	
	public static void setStopFlag(boolean f){
		mStopFlag = f;
	}
	 /**
     * mass zip files/folders
     *  
     * @param resFileList :zipped
     * @param zipFile :create
     * @throws IOException :error throw
     */
    public static boolean zipFiles(Collection<File> resFileList, File zf) throws IOException {
		ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zf), BUFF_SIZE));
        for (File resFile : resFileList) {
        	if(mStopFlag){
        		zipout.close();
        		zf.delete();
        		return false;
        	}
        	if(zipFile(resFile, zipout, "")==false){
        		zipout.close();
        		zf.delete();
        		return false;
        	}
        }
        zipout.close();
        return true;
    }

    /**
     * mass zip files/folders
     * 
     * @param resFileList :zipped file list
     * @param zipFile :create file 
     * @param comment :comment
     * @throws IOException :error throw
     */
    public static boolean zipFiles(Collection<File> resFileList, File zipFile, String comment)
            throws IOException {
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                zipFile), BUFF_SIZE));
        for (File resFile : resFileList) {
        	if(mStopFlag){
        		zipout.close();
        		zipFile.delete();
        		return false;
        	}
            if(zipFile(resFile, zipout, "")==false){
            	zipout.close();
        		zipFile.delete();
        		return false;
            }
        }
        zipout.setComment(comment);
        zipout.close();
        return true;
    }

    /**
     * unzip a file
     * 
     * @param zipFile 
     * @param folderPath :gold folder path
     * @throws IOException 
     */
    public static void unZipFile(File zipFile, String folderPath, MediaScannerConnection conn) throws ZipException, IOException {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
        	if(mStopFlag){
        		break;
        	}
        	ZipEntry entry = ((ZipEntry)entries.nextElement());
        	InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + entry.getName();
           // str = new String(str.getBytes("GB2312"), "GB2312");
            File desFile = new File(str);
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte buffer[] = new byte[BUFF_SIZE];
            int realLength;
            while ((realLength = in.read(buffer)) > 0) {
            	if(mStopFlag){
            		in.close();
                    out.close();
                    desFile.delete();
            		return;
            	}
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
            conn.scanFile(str, null);
        }
    }

    /**
     * unzip file(include name matching)
     * 
     * @param zipFile 
     * @param folderPath 
     * @param nameContains :match name
     * @throws ZipException 
     * @throws IOException 
     */
    public static ArrayList<File> upZipSelectedFile(File zipFile, String folderPath,
            String nameContains) throws ZipException, IOException {
        ArrayList<File> fileList = new ArrayList<File>();

        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdir();
        }

        ZipFile zf = new ZipFile(zipFile);
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
        	if(mStopFlag){
        		break;
        	}
            ZipEntry entry = ((ZipEntry)entries.nextElement());
            if (entry.getName().contains(nameContains)) {
                InputStream in = zf.getInputStream(entry);
                String str = folderPath + File.separator + entry.getName();
              //  str = new String(str.getBytes("GB2312"), "GB2312");
                // str.getBytes("GB2312"),"8859_1" input
                // str.getBytes("8859_1"),"GB2312" output
                File desFile = new File(str);
                if (!desFile.exists()) {
                    File fileParentDir = desFile.getParentFile();
                    if (!fileParentDir.exists()) {
                        fileParentDir.mkdirs();
                    }
                    desFile.createNewFile();
                }
                OutputStream out = new FileOutputStream(desFile);
                byte buffer[] = new byte[BUFF_SIZE];
                int realLength;
                while ((realLength = in.read(buffer)) > 0) {
                	if(mStopFlag){
                		in.close();
                        out.close();
                        desFile.delete();
                		break;
                	}
                    out.write(buffer, 0, realLength);
                }
                in.close();
                out.close();
                fileList.add(desFile);
            }
        }
        return fileList;
    }

    /**
     * get filelist of zip file
     * 
     * @param zipFile 
     * @return file list in the zipfile
     * @throws ZipException 
     * @throws IOException 
     */
    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList<String>();
        Enumeration<?> entries = getEntriesEnumeration(zipFile);
        while (entries.hasMoreElements()) {
            ZipEntry entry = ((ZipEntry)entries.nextElement());
            //entryNames.add(new String(getEntryName(entry).getBytes("GB2312"), "GB2312"));
            entryNames.add(new String(getEntryName(entry)));
        }
        return entryNames;
    }

    /**
     * get the property of zipfile
     * 
     * @param zipFile 
     * @return 
     * @throws ZipException 
     * @throws IOException 
     */
    public static Enumeration<?> getEntriesEnumeration(File zipFile) throws ZipException,
            IOException {
        ZipFile zf = new ZipFile(zipFile);
        return zf.entries();
    }

    /**
     * Get Comment of zip files
     * 
     * @param entry 
     * @return 
     * @throws UnsupportedEncodingException
     */
    public static String getEntryComment(ZipEntry entry) throws UnsupportedEncodingException {
        //return new String(entry.getComment().getBytes("GB2312"), "8859_1");
        return new String(entry.getComment());
    }

    /**
     * get the name of zip files
     * 
     * @param entry 
     * @return 
     * @throws UnsupportedEncodingException
     */
    public static String getEntryName(ZipEntry entry) throws UnsupportedEncodingException {
        //return new String(entry.getName().getBytes("GB2312"), "GB2312");
    	return new String(entry.getName());
    }

    /**
     * zip file
     * 
     * @param resFile 
     * @param zipout 
     * @param rootpath 
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    private static boolean zipFile(File resFile, ZipOutputStream zipout, String rootpath)
            throws FileNotFoundException, IOException {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator)
                + resFile.getName();
       // rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                if(zipFile(file, zipout, rootpath)==false){
                	return false;
                }
            }
        } else {
            byte buffer[] = new byte[BUFF_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(resFile),
                    BUFF_SIZE);
            zipout.putNextEntry(new ZipEntry(rootpath));
            int realLength;
            while ((realLength = in.read(buffer)) != -1) {
            	if(mStopFlag){
            		in.close();
            		zipout.closeEntry();
            		return false;
            	}
                zipout.write(buffer, 0, realLength);
            }
            in.close();
            zipout.flush();
            zipout.closeEntry();
        }
        return true;
    }
}
