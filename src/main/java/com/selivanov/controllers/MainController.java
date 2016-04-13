package com.selivanov.controllers;

import com.independentsoft.office.word.*;
import com.independentsoft.office.word.fields.Time;
import com.independentsoft.office.word.tables.Table;
import com.selivanov.App;
import com.selivanov.impl.FileReplacerDocx;
import com.selivanov.interfaces.FileReplacer;
import com.selivanov.services.CSVReader;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controller for view Main.fxml
 */
public class MainController {

    @FXML
    private TableView<Integer> headerTable = new TableView<>();;
    @FXML
    private Label txtLabel = new Label();
    @FXML
    private Button selectAllBut;
    @FXML
    private TextField searchField;
    @FXML
    private TextField coefficientField;
    @FXML
    private CheckBox activateCoef;
    @FXML
    private CheckBox inOneDocBox;
    @FXML
    private ChoiceBox<String> headerFields;
    @FXML
    private Label cellLabel;

    // Reference to the main application
    private App mainApp;

    private CSVReader csvReader;
    private File csvFile;
    private File templateFile;

    private List currentRecords;

    private Table cellTable;

    @FXML
    private Button getTableButton;

    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }


    @FXML
    private void setTable(List records) throws ClassNotFoundException {
        headerTable.setEditable(true);
        headerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        for (int i = 0; i < records.size(); i++) {
            headerTable.getItems().add(i);
        }

        for(int i=0; i<csvReader.getHeader().size(); i++){
            TableColumn<Integer, String> column = new TableColumn<>(csvReader.getHeader().get(i));
            column.setSortable(false);
            List list = csvReader.getVerticalRecords(i, records);
            column.setCellValueFactory(cellData -> {
                Integer rowIndex = cellData.getValue();
                return new ReadOnlyStringWrapper((String) list.get(rowIndex));
            });

            headerTable.getColumns().add(column);
        }

        if(selectAllBut.isDisable())
            selectAllBut.setDisable(false);
    }

    @FXML
    private void importCsv() throws ClassNotFoundException {
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File temp = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if(temp!=null){
            csvFile = temp;
            csvReader = new CSVReader(csvFile);
            clearTable();
            setTable(csvReader.getRecords());
            currentRecords = csvReader.getRecords();
            initializeChoiceBox();
        }
    }

    @FXML
    private void oneDoc(){
        if(csvFile != null){
            Table temp = getCellTable();
            if(temp != null){
                this.cellTable = temp;
                cellLabel.setText("Table loaded");
            }
        }
        else{
            showAlert("No files", "No files", "Please import csv file.");
        }

    }

    private Table getCellTable(){
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("/view/Table.fxml"));
            BorderPane page = (BorderPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Table");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            TableController controller = loader.getController();
            controller.setHeaderRecords(csvReader.getHeader(), currentRecords, csvReader);
            controller.setDialogStage(dialogStage);
            controller.setMainApp(mainApp);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.getTable();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void importTemplate(){
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.docx)", "*.docx");
        fileChooser.getExtensionFilters().add(extFilter);

        File temp = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if(temp!=null){
            templateFile = temp;
            txtLabel.setText("Imported file: " + templateFile.getName());
        }
    }


    @FXML
    private void saveFile(){
        if(templateFile != null && csvFile != null){
            if(!activateCoef.isSelected()){
                replace();
            }
            else{
                if(isNumber(coefficientField.getText())){
                    replace();
                }
            }
        }
        else{
            showAlert("No files", "No files", "Please import files.");
        }

    }

    private void replace(){
        FileReplacer fileReplacer = null;

        fileReplacer = new FileReplacerDocx(templateFile);



        int tableIndex = headerTable.getSelectionModel().getSelectedIndex();


        if(tableIndex >= 0){

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(mainApp.getPrimaryStage());;

            if(inOneDocBox.isSelected() && cellTable != null){
                CSVRecord record = (CSVRecord) currentRecords.get(headerTable.getSelectionModel().getSelectedIndex());


                File file = null;

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date date = new Date();
                String dateStr = dateFormat.format(date);

                if(directory != null) {
                    file = new File(directory.getAbsolutePath() + "/(" + dateStr + ")-new" + "-" + templateFile.getName());
                }

                fileReplacer.replaceInOneDoc(csvReader.getHeader(), record, file, cellTable);
            }
            else{
                for(int i=0; i<headerTable.getSelectionModel().getSelectedIndices().size(); i++){
                    int temp = headerTable.getSelectionModel().getSelectedIndices().get(i);
                    CSVRecord record = (CSVRecord) currentRecords.get(temp);

                    File file = null;

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    Date date = new Date();
                    String dateStr = dateFormat.format(date);

                    if(directory != null) {
                        file = new File(directory.getAbsolutePath() + "/(" + dateStr + ")" + i + "-" + templateFile.getName());
                    }

                    if (file != null) {
                        if(activateCoef.isSelected())
                            fileReplacer.replaceTagsWithCoef(csvReader.getHeader(), headerFields.getSelectionModel().getSelectedIndex(), Double.parseDouble(coefficientField.getText()), record, file);
                        else
                            fileReplacer.replaceTags(csvReader.getHeader(), record, file);

                    }
                }
            }

            if(directory != null)
                showMessage("Done", "Done", "Done");
        }
        else{
            showAlert("No Selection", "No Row Selected", "Please select a row in the table.");
        }
    }

    @FXML
    private void searchRecords(){
        if(csvFile != null){
            clearTable();
            try {
                setTable(csvReader.searchRecords(searchField.getText()));
                currentRecords = csvReader.searchRecords(searchField.getText());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            showMessage("", "", "");
        }
    }


    private void showAlert(String title, String headerText, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    private void showMessage(String title, String headerText, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    private void clearTable(){
        headerTable.getColumns().remove(0, headerTable.getColumns().size());
        headerTable.getItems().remove(0, headerTable.getItems().size());
    }

    @FXML
    private void selectAll(){
        headerTable.getSelectionModel().selectAll();
    }

    @FXML
    private void activateCoefficient(){
        if(activateCoef.isSelected()){
            coefficientField.setDisable(false);
            headerFields.setDisable(false);
        }
        else{
            coefficientField.setDisable(true);
            headerFields.setDisable(true);
        }

    }

    @FXML
    private void activateOneDocBox(){
        if(inOneDocBox.isSelected()){
            getTableButton.setDisable(false);
            headerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            activateCoef.setDisable(true);
        }
        else{
            getTableButton.setDisable(true);
            headerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            activateCoef.setDisable(false);
        }
    }

    private void initializeChoiceBox(){
        ObservableList<String> headerList = FXCollections.observableArrayList();

        List list = csvReader.getHeader();

        for(int i=0; i<list.size(); i++){
            //if(isNumberPure(csvReader.getVerticalRecords(i, currentRecords).get(0).toString()))
                headerList.add((String) list.get(i));
        }
        headerFields.setItems(headerList);
        headerFields.getSelectionModel().selectFirst();
    }

    private boolean isNumber(String string) {
        String errorMessage = "";

        if (string == null || string.length() == 0) {
            errorMessage += "No valid coefficient!\n";
        } else {
            // try to parse the postal code into a double.
            try {
                Double.parseDouble(string);
            } catch (NumberFormatException e) {
                errorMessage += "No valid coefficient (must be a double)!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            showAlert("", "", errorMessage);

            return false;
        }
    }

    private boolean isNumberPure(String string){
        String errorMessage = "";

        if (string == null || string.length() == 0) {
            errorMessage += "No valid coefficient!\n";
        } else {
            // try to parse the postal code into a double.
            try {
                Double.parseDouble(string);
            } catch (NumberFormatException e) {
                errorMessage += "No valid coefficient (must be an integer)!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @FXML
    private void importFromFtp() throws ClassNotFoundException {
        File temp = getFromFtp();

        if(temp!=null){
            csvFile = temp;
            csvReader = new CSVReader(csvFile);
            clearTable();
            setTable(csvReader.getRecords());
            currentRecords = csvReader.getRecords();
            initializeChoiceBox();
        }
    }

    private File getFromFtp(){
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("/view/Ftp.fxml"));
            GridPane page = (GridPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Load");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the controller.
            FtpController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.getFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void replaceTable(File file, Table table){
        try
        {
            WordDocument doc = new WordDocument(file.getAbsolutePath());

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
                        doc.getBody().getContent().add(i,table);
                    }
                }
            }

            doc.save(file.getAbsolutePath(), true);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
