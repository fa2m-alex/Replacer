package com.selivanov.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

/**
 * Controller for view Ftp.fxml
 */
public class FtpController {

    private File file;

    @FXML
    private TextField serverField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField pathField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public File getFile() {
        return this.file;
    }

    public FtpController(){

    }

    @FXML
    private void initialize() {

    }

    private boolean isInputValid() {

        String errorMessage = "";

        if (serverField.getText() == null || serverField.getText().length() == 0) {
            errorMessage += "No valid server name!\n";
        }
        if (userField.getText() == null || userField.getText().length() == 0) {
            errorMessage += "No valid username!\n";
        }
        if (passwordField.getText() == null || passwordField.getText().length() == 0) {
            errorMessage += "No valid pass!\n";
        }
        if (pathField.getText() == null || pathField.getText().length() == 0) {
            errorMessage += "No valid path!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    @FXML
    private void handleOk(){
        if(isInputValid()){
            String server = serverField.getText();
            //int port = 21;
            String user = userField.getText();
            String pass = passwordField.getText();
            String path = pathField.getText();

            FTPClient ftpClient = new FTPClient();
            try {

                ftpClient.connect(server);
                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                this.file = File.createTempFile("tmp", ".csv");

                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(this.file));
                boolean success = ftpClient.retrieveFile(path, outputStream1);
                outputStream1.close();

                if (success) {
                    System.out.println(file.getName() + " has been downloaded successfully.");
                }

                file.deleteOnExit();
                dialogStage.close();

            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void handleCancel(){
        dialogStage.close();
    }
}
