package ru.kozyar.alicestation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Filer {

    public static String getFile(String fileName) {
        try {
            String result = "";
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String curLine;
            // читаем содержимое
            while ((curLine = br.readLine()) != null) {
                result += curLine;
            }
            br.close();
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    public static void setFile(String fileName, String data) {
        File file = new File(fileName);
        try {

            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(data);
            bw.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}