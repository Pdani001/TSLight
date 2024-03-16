package com.mikemik44.light.filemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileHandler {

	public static boolean writeToFile(String file, String text) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(text);
			bw.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean appendToFile(String file, String text) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.append(text);
			bw.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String readFromFile(String file) {
		try {
			BufferedReader bw = new BufferedReader(new FileReader(file));
			String line = "", l;
			while ((l = bw.readLine()) != null) {
				if (!line.isEmpty()) {
					line += "\n";
				}
				line += l;
			}
			bw.close();
			return line;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String readFromFile(File file) {
		try {
			BufferedReader bw = new BufferedReader(new FileReader(file));
			String line = "", l;
			while ((l = bw.readLine()) != null) {
				if (!line.isEmpty()) {
					line += "\n";
				}
				line += l;
			}
			bw.close();
			return line;
		} catch (Exception e) {
			return null;
		}
	}

}
