package com.selivanov.controllers;

import com.independentsoft.office.ExtendedBoolean;
import com.independentsoft.office.word.Paragraph;
import com.independentsoft.office.word.Run;
import com.independentsoft.office.word.StandardBorderStyle;
import com.independentsoft.office.word.tables.*;
import com.independentsoft.office.word.tables.Cell;
import com.selivanov.App;
import com.selivanov.services.CSVReader;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for view Table.fxml
 */
public class TableController {

    @FXML
    private TableView<Integer> table;

    List header;
    List records;
    private CSVReader csvReader;

    private App mainApp;

    private Stage dialogStage;
    @FXML
    private ChoiceBox<String> headerFields;
    @FXML
    private TextField coefficientField;
    @FXML
    private CheckBox activateCoef;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setHeaderRecords(List header, List records, CSVReader csvReader){
        this.header = header;
        this.records = records;
        this.csvReader = csvReader;
        setTable(header, records);
        initializeChoiceBox();
    }

    public void setMainApp(App mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize(){

    }

    private void setTable(List<String> header, List records){
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().setCellSelectionEnabled(true);

        for (int i = 0; i < records.size(); i++) {
            table.getItems().add(i);
        }

        for(int i=0; i<header.size(); i++){
            TableColumn<Integer, String> column = new TableColumn<>(header.get(i));
            column.setSortable(false);
            List list = csvReader.getVerticalRecords(i, records);
            column.setCellValueFactory(cellData -> {
                Integer rowIndex = cellData.getValue();
                return new ReadOnlyStringWrapper((String) list.get(rowIndex));
            });

            table.getColumns().add(column);
        }

    }

    @FXML
    public Table getTable(){
        ArrayList<String> headerItemsStr = new ArrayList<String>();
            ArrayList<Paragraph> headerItems = new ArrayList<Paragraph>();
            ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();

            for(int i=0; i<selectedCells.size(); i++){
                if(!headerItemsStr.contains(header.get(selectedCells.get(i).getColumn()).toString())){
                    Paragraph temp = new Paragraph();
                    String text = header.get(selectedCells.get(i).getColumn()).toString();
                    Run run = new Run(text);
                    run.setBold(ExtendedBoolean.TRUE);
                    temp.add(run);
                    headerItems.add(temp);
                    headerItemsStr.add(text);
                }
            }

            ArrayList<Cell> headerCells = new ArrayList<Cell>();

            for(int i=0; i<headerItems.size(); i++){
                Cell cell = new Cell();
                cell.add(headerItems.get(i));
                headerCells.add(cell);
            }

            Row headerRow = new Row();

            for(int i=0; i<headerCells.size(); i++){
                headerRow.add(headerCells.get(i));
            }

        ArrayList<Row> rows = new ArrayList<Row>();

            /**********************************/
            for(int i=0; i<table.getSelectionModel().getSelectedIndices().size(); i++){

                CSVRecord record = (CSVRecord) records.get(i);
                Row tempRow = new Row();
                //String str = "";
                for(int j=0; j<table.getSelectionModel().getSelectedCells().size(); j++){
                    if(table.getSelectionModel().getSelectedCells().get(j).getRow() == i){
                        Paragraph tempPar = new Paragraph();
                        String text = "";

                        if(table.getSelectionModel().getSelectedCells().get(j).getColumn() ==
                                headerFields.getSelectionModel().getSelectedIndex() &&
                                activateCoef.isSelected()){
                            double coefficient = Double.parseDouble(coefficientField.getText());
                            double replacer = Double.parseDouble(record.get(table.getSelectionModel().getSelectedCells().get(j).getColumn()));
                            text = String.valueOf(replacer * coefficient);
                        }
                        else{
                            text = record.get(table.getSelectionModel().getSelectedCells().get(j).getColumn());
                        }

                        Run run = new Run(text);
                        tempPar.add(run);
                        Cell tempCell = new Cell();
                        tempCell.add(tempPar);
                        tempRow.add(tempCell);
                    }
                }

                rows.add(tempRow);
            }
        /**************************************/


            Table table1 = new Table(StandardBorderStyle.SINGLE_LINE);
            table1.setWidth(new Width(TableWidthUnit.PERCENT, 100));

            table1.add(headerRow);

        for(int i=0; i<rows.size(); i++){
            table1.add(rows.get(i));
        }

            dialogStage.close();

            if(headerItems.size() > 0)
                return table1;
            else
                return null;
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }

    private void initializeChoiceBox(){
        ObservableList<String> headerList = FXCollections.observableArrayList();

        List list = header;

        for(int i=0; i<list.size(); i++){
            //if(isNumberPure(csvReader.getVerticalRecords(i, records).get(0).toString()))
                headerList.add((String) list.get(i));
        }
        headerFields.setItems(headerList);
        headerFields.getSelectionModel().selectFirst();
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

    private void showAlert(String title, String headerText, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    @FXML
    private void selectInColumn(){
        table.getSelectionModel().selectAll();
    }

}
