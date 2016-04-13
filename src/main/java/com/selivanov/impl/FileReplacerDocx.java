package com.selivanov.impl;

import com.independentsoft.office.word.*;
import com.independentsoft.office.word.tables.Table;
import com.selivanov.interfaces.FileReplacer;
import org.apache.commons.csv.CSVRecord;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * implementation of FileReplacer interface for docx documents
 */
public class FileReplacerDocx implements FileReplacer {

    private File rootFile;

    public FileReplacerDocx(File rootFile){
        this.rootFile = rootFile;
    }

    @Override
    public void replaceTags(ArrayList<String> header, CSVRecord record, File resultFile) {
        try {
            WordDocument doc = new WordDocument(rootFile.getAbsolutePath());
            for(int i=0; i<header.size(); i++){
                doc.replace("<" + header.get(i) + ">", record.get(i));
            }
            doc.save(resultFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replaceTagsWithCoef(ArrayList<String> header, int headerIndex, double coefficient, CSVRecord record, File resultFile) {
        try {
            WordDocument doc = new WordDocument(rootFile.getAbsolutePath());
            for(int i=0; i<header.size(); i++){
                if(i == headerIndex){
                    double replacer = Double.parseDouble(record.get(i)) * coefficient;
                    doc.replace("<" + header.get(i) + ">", String.valueOf(replacer));
                }
                else{
                    doc.replace("<" + header.get(i) + ">", record.get(i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replaceInOneDoc(ArrayList<String> header, CSVRecord record, File file, Table cellTable) {
        try {
            WordDocument doc = new WordDocument(rootFile.getAbsolutePath());
            for(int i=0; i<header.size(); i++){
                doc.replace("<" + header.get(i) + ">", record.get(i));
            }

            replaceTable(doc, cellTable);

            doc.save(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    //
    private void replaceTable(WordDocument doc, Table table){
        for(int i=0; i < doc.getBody().getContent().size(); i++)
        {
            if(doc.getBody().getContent().get(i) instanceof Paragraph)
            {
                Paragraph paragraph = (Paragraph)doc.getBody().getContent().get(i);

                String paragraphText = "";

                for(IParagraphContent pContent : paragraph.getContent())
                {
                    if(pContent instanceof Run)
                    {
                        Run run = (Run)pContent;

                        for(IRunContent rContent : run.getContent())
                        {
                            if(rContent instanceof Text)
                            {
                                Text text = (Text)rContent;
                                paragraphText += text.getValue();
                            }
                        }
                    }
                }

                if(paragraphText.indexOf("<table>") > -1)
                {
                    paragraph.getContent().clear();
                    doc.getBody().getContent().add(i, table);
                }
            }
        }
    }
}
