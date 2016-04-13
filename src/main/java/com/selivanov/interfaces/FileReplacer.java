package com.selivanov.interfaces;

import com.independentsoft.office.word.tables.Table;
import org.apache.commons.csv.CSVRecord;
import java.io.File;
import java.util.ArrayList;

/**
 * Interface for replacing placeholders in document with values from csv
 */
public interface FileReplacer {
    // basic method
    public void replaceTags(ArrayList<String> header, CSVRecord record, File resultFile);

    // value with coefficient
    public void replaceTagsWithCoef(ArrayList<String> header, int headerIndex, double coefficient, CSVRecord record, File resultFile);

    // replace special placeholder with table with multiple values
    public void replaceInOneDoc(ArrayList<String> header, CSVRecord record, File file, Table cellTable);
}
