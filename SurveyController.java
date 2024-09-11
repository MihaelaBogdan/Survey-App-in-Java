import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class SurveyController {

    @FXML
    private TextField nameField;

    @FXML
    private RadioButton option1;

    @FXML
    private RadioButton option2;

    @FXML
    private RadioButton option3;

    @FXML
    private CheckBox agreeCheckBox;

    @FXML
    void submitSurvey(ActionEvent event) {
        String name = nameField.getText();
        String selectedOption = "";
        
        if (option1.isSelected()) {
            selectedOption = option1.getText();
        } else if (option2.isSelected()) {
            selectedOption = option2.getText();
        } else if (option3.isSelected()) {
            selectedOption = option3.getText();
        }

        boolean agreed = agreeCheckBox.isSelected();

        if (name.isEmpty() || selectedOption.isEmpty() || !agreed) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Eroare");
            alert.setHeaderText("Formular incomplet");
            alert.setContentText("Te rugăm să completezi toate câmpurile și să accepți termenii și condițiile.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sondaj trimis");
            alert.setHeaderText("Mulțumim pentru feedback!");
            alert.setContentText("Nume: " + name + "\nOpțiune selectată: " + selectedOption);
            alert.showAndWait();
        }
    }
}
