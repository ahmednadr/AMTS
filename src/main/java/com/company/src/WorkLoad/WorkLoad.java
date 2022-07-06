package com.company.src.WorkLoad;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkLoad {
    private final String FilePath ;

    public WorkLoad(String FilePath){
        this.FilePath= FilePath;
    }

    public ArrayList<Integer> ParseCSV(){
        List<String[]> r = null;
        try (CSVReader reader = new CSVReader(new FileReader(this.FilePath))) {
            r = reader.readAll();          // List of arrays each array contains one line
            r.remove(0);                            // remove the seconds | count line
        } catch (Exception e) {
            e.printStackTrace();                          // todo add to logs
        }
        return convert(r);
    }

    private static ArrayList<Integer> convert(List<String[]> r){
        ArrayList<Integer> parsed = new ArrayList<Integer>();
        r.forEach(x -> {
            String [] values = x[0].split("\\|");          // [second , value]
            parsed.add(Integer.valueOf(values[1]));
        });
        return parsed;
    }
}
