package com.riis.androidarduino.barcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	
	public static boolean doesFileExist(String filePath) {
		try {
			@SuppressWarnings({ "resource", "unused" })
			FileInputStream fin = new FileInputStream(filePath);
		} catch(FileNotFoundException e) {
			return false;
		}

		return true;

	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if(i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public static byte[] readFileAsBytes(String filePath) throws IOException {
		int fileLength = (int) new File(filePath).length();
		byte[] fileBytes = new byte[fileLength];

		FileInputStream fin = new FileInputStream(filePath);
		fin.read(fileBytes, 0, fileLength);
		fin.close();

		return fileBytes;
	}

	public static String readFileAsString(String filePath) throws IOException {
		return new String(readFileAsBytes(filePath));
	}

	public static void writeStringToFile(String stringToWrite, String filePath) throws IOException {
		writeBytesToFile(stringToWrite.getBytes(), filePath);
	}

	public static void writeBytesToFile(byte[] bytes, String filePath) throws IOException {
		FileOutputStream fout = new FileOutputStream(filePath);
		fout.write(bytes);
		fout.close();
	}

	public static void zipFileIntoNewZip(String fileToZip, String newZipFilePath) throws Exception {
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(newZipFilePath));
		ZipEntry dexEntry = new ZipEntry(getNameOfFileFromFullPath(fileToZip));

		zipOut.putNextEntry(dexEntry);
		
		byte[] fileBytes = readFileAsBytes(fileToZip);
		zipOut.write(fileBytes);
		
		zipOut.closeEntry();
		zipOut.close();
	}

	private static String getNameOfFileFromFullPath(String filePath) {
		int lastSeparatorIndex = filePath.lastIndexOf("/");
		
		if(lastSeparatorIndex >= 0) {
			return filePath.substring(lastSeparatorIndex + 1, filePath.length());
		} else {
			return filePath;
		}
	}
	
	public static boolean deleteDirectoryAndContents(String directoryPath) {
		File directory = new File(directoryPath);
		
		return deleteDirectoryAndContents(directory);
	}
	
	public static boolean deleteDirectoryAndContents(File directory) {		
		if (directory == null) {
			return false;
		}
		if (!directory.exists()) {
			return true;
		}
		if (!directory.isDirectory()) {
			return false;
		}

		String[] list = directory.list();

		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);

				if (entry.isDirectory()) {
					if (!deleteDirectoryAndContents(entry)) {
						return false;
					}
				} else {
					if (!entry.delete()) {
						return false;
					}
				}
			}
		}

		return directory.delete();
	}
}
