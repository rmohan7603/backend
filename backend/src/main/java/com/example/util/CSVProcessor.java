package com.example.util;

import com.example.model.UsageData;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVProcessor {
    public static List<UsageData> parseCSV(InputStream inputStream) throws IOException, CsvValidationException {
        List<UsageData> dataList=new ArrayList<>();
        try (CSVReader reader=new CSVReader(new InputStreamReader(inputStream))) {
            String[] line;
            reader.readNext();
            while ((line=reader.readNext()) != null) {
                try {
                    String userId=line[0].trim();
                    int usageValue=Integer.parseInt(line[1].trim());
                    long epoch=Long.parseLong(line[2].trim());
                    dataList.add(new UsageData(userId, usageValue, epoch));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return dataList;
    }
}