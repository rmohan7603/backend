package com.example.util;

import com.example.model.UsageData;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CSVProcessor {
	public static Stream<UsageData> parseCSV(InputStream inputStream) throws IOException, CsvException {
	    CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
	        return reader.readAll().stream().skip(1)
	            .map(line -> {
	                try {
	                    String userId = line[0].trim();
	                    int usageValue = Integer.parseInt(line[1].trim());
	                    long epoch = Long.parseLong(line[2].trim());
	                    return new UsageData(userId, usageValue, epoch);
	                } catch (Exception e) {
	                    return null;
	                }
	            })
	        .filter(data -> data != null);
	}
}