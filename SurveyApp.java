import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class SurveyApp extends Application {

    private static final String SURVEYS_FILE = "surveys.ser";
    private static final String USERS_FILE = "users.ser";
    private Map<String, Survey> surveys = new HashMap<>();
    private Map<String, User> users = new HashMap<>();
    private Map<String, List<String>> userSurveys = new HashMap<>();
    private String currentUserId;
    private Label userLabel;
    private BorderPane mainLayout;
    private VBox sidebar;
    private TextField searchField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadUsers();
        loadSurveys();

        mainLayout = new BorderPane();
        sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        HBox header = createHeader();
        mainLayout.setTop(header);

        VBox initialLayout = createSurveyCreationLayout();
        mainLayout.setCenter(initialLayout);

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setTitle("");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2C3E50;");

        userLabel = new Label();
        userLabel.setTextFill(Color.WHITE);
        userLabel.setStyle("-fx-font-size: 16px;");

        Button toggleSidebarButton = createStyledButton("☰", "#34495e");
        toggleSidebarButton.setOnAction(e -> toggleSidebarVisibility());

        Button authButton = createStyledButton("Authentication", "#3498db");
        authButton.setOnAction(e -> showLoginForm());

        searchField = new TextField();
        searchField.setPromptText("Search by ID");
        searchField.setMinWidth(200);

        Button searchButton = createStyledButton("Search", "#3498db");
        searchButton.setOnAction(e -> searchSurvey());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightSection = new HBox(10, spacer, toggleSidebarButton, authButton, searchField, searchButton);
        rightSection.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        header.getChildren().addAll(userLabel, rightSection);

        return header;
    }

    private VBox createSurveyCreationLayout() {
        VBox surveyCreationLayout = new VBox(10);
        surveyCreationLayout.setPadding(new Insets(20));
        surveyCreationLayout.setStyle("-fx-background-color: #2C3E50;");

        Label titleLabel = new Label("Survey Title:");
        titleLabel.setTextFill(Color.WHITE);
        TextField surveyTitleField = new TextField();
        surveyTitleField.setPromptText("Enter survey title");

        Label questionLabel = new Label(" Question:");
        questionLabel.setTextFill(Color.WHITE);
        TextField questionField = new TextField();
        questionField.setPromptText("Enter survey question");

        Label optionLabel = new Label("Option:");
        optionLabel.setTextFill(Color.WHITE);
        TextField optionField = new TextField();
        optionField.setPromptText("Enter an option");

        Button addOptionButton = new Button("Add Option");
        ListView<String> optionsListView = new ListView<>();
        List<String> options = new ArrayList<>();

        addOptionButton.setOnAction(e -> {
            String option = optionField.getText();
            if (!option.isEmpty()) {
                options.add(option);
                optionsListView.getItems().add(option);
                optionField.clear();
            }
        });

        Button createSurveyButton = new Button("Create ");
        createSurveyButton.setOnAction(e -> {
            String title = surveyTitleField.getText();
            String question = questionField.getText();
            if (!title.isEmpty() && !question.isEmpty() && !options.isEmpty()) {
                String surveyId = UUID.randomUUID().toString();
                Survey survey = new Survey(title, question, options);
                survey.setCreatorId(currentUserId);
                surveys.put(surveyId, survey);
                userSurveys.computeIfAbsent(currentUserId, k -> new ArrayList<>()).add(surveyId);
                saveSurveys();
                showAlert(Alert.AlertType.INFORMATION, "Survey Created", "Survey ID: " + surveyId);
                surveyTitleField.clear();
                questionField.clear();
                options.clear();
                optionsListView.getItems().clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields and add options.");
            }
        });

        surveyCreationLayout.getChildren().addAll(titleLabel, surveyTitleField, questionLabel, questionField, optionLabel, optionField, addOptionButton, optionsListView, createSurveyButton);

        return surveyCreationLayout;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #2C3E50;");
        sidebar.setPrefWidth(200);

        VBox buttons = new VBox(10);
        buttons.setPadding(new Insets(10));

        Button createSurveyButton = createStyledButton("Create ", "#3498db");
        createSurveyButton.setOnAction(e -> mainLayout.setCenter(createSurveyCreationLayout()));

        Button viewSurveysButton = createStyledButton("View ", "#3498db");
        viewSurveysButton.setOnAction(e -> mainLayout.setCenter(createViewSurveysLayout()));

        Button viewStatsButton = createStyledButton("View Stats", "#3498db");
        viewStatsButton.setOnAction(e -> mainLayout.setCenter(createViewStatsLayout()));

        Button helpButton = createStyledButton("Help", "#3498db");
        helpButton.setOnAction(e -> showHelp());

        Button profileButton = createStyledButton("Profile", "#3498db");
        profileButton.setOnAction(e -> showProfile());

        Button logoutButton = createStyledButton("Logout", "#e74c3c");
        logoutButton.setOnAction(e -> confirmLogout());

        buttons.getChildren().addAll(createSurveyButton, viewSurveysButton, viewStatsButton, helpButton, profileButton, logoutButton);

        sidebar.getChildren().add(buttons);

        return sidebar;
    }

    private void showRegisterForm() {
        mainLayout.setCenter(createRegisterForm());
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-font-size: 14px;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", -20%); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-font-size: 14px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-font-size: 14px;"
        ));
        return button;
    }

    private void toggleSidebarVisibility() {
        sidebar.setVisible(!sidebar.isVisible());
        sidebar.setManaged(sidebar.isVisible());
    }

    private void searchSurvey() {
        String searchId = searchField.getText().trim();
        if (!searchId.isEmpty() && surveys.containsKey(searchId)) {
            Survey survey = surveys.get(searchId);
            mainLayout.setCenter(createEditSurveyLayout(survey, searchId));
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Survey ID not found.");
        }
    }

    private void showLoginForm() {
        mainLayout.setCenter(createLoginForm());
    }

    private VBox createLoginForm() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setStyle("-fx-background-color: #3498db;");

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 10px;");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Label registerLabel = new Label("Don't have an account? Register here.");
        registerLabel.setTextFill(Color.WHITE);
        registerLabel.setStyle("-fx-underline: true;");
        registerLabel.setOnMouseClicked(e -> showRegisterForm());

        loginLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerLabel);

        return loginLayout;
    }

    private void handleLogin(String username, String password) {
        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
            currentUserId = username;
            userLabel.setText("Logged in as: " + username);
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username);
            mainLayout.setCenter(createSurveyCreationLayout());
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
    }

    private VBox createRegisterForm() {
        VBox registerLayout = new VBox(10);
        registerLayout.setPadding(new Insets(20));
        registerLayout.setStyle("-fx-background-color: #3498db;");

        Label usernameLabel = new Label("Username:");
        usernameLabel.setTextFill(Color.WHITE);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setTextFill(Color.WHITE);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 10px;");
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText()));

        registerLayout.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, registerButton);

        return registerLayout;
    }

    private void handleRegister(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty() && !users.containsKey(username)) {
            User newUser = new User(username, password);
            users.put(username, newUser);
            saveUsers();
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User " + username + " registered successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Please fill all fields or choose a different username.");
        }
    }

    private VBox createViewSurveysLayout() {
        VBox surveysLayout = new VBox(10);
        surveysLayout.setPadding(new Insets(20));
        surveysLayout.setStyle("-fx-background-color: #2C3E50;");

        ListView<String> surveyListView = new ListView<>();
        if (currentUserId != null && userSurveys.containsKey(currentUserId)) {
            List<String> surveyIds = userSurveys.get(currentUserId);
            for (String surveyId : surveyIds) {
                surveyListView.getItems().add(surveyId + " - " + surveys.get(surveyId).getTitle());
            }
        }

        surveysLayout.getChildren().addAll(surveyListView);

        return surveysLayout;
    }

    private VBox createViewStatsLayout() {
        // Implement your stats view here
        VBox statsLayout = new VBox(10);
        statsLayout.setPadding(new Insets(20));
        statsLayout.setStyle("-fx-background-color: #2C3E50;");

        Label statsLabel = new Label("Statistics");
        statsLabel.setTextFill(Color.WHITE);

        statsLayout.getChildren().addAll(statsLabel);

        return statsLayout;
    }

    private VBox createEditSurveyLayout(Survey survey, String surveyId) {
        VBox editSurveyLayout = new VBox(10);
        editSurveyLayout.setPadding(new Insets(20));
        editSurveyLayout.setStyle("-fx-background-color: #2C3E50;");

        Label titleLabel = new Label("Edit Survey Title:");
        titleLabel.setTextFill(Color.WHITE);
        TextField titleField = new TextField(survey.getTitle());

        Label questionLabel = new Label("Edit Survey Question:");
        questionLabel.setTextFill(Color.WHITE);
        TextField questionField = new TextField(survey.getQuestion());

        Label optionsLabel = new Label("Edit Options:");
        optionsLabel.setTextFill(Color.WHITE);

        ListView<String> optionsListView = new ListView<>(FXCollections.observableArrayList(survey.getOptions()));

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            survey.setTitle(titleField.getText());
            survey.setQuestion(questionField.getText());
            survey.setOptions(new ArrayList<>(optionsListView.getItems()));
            surveys.put(surveyId, survey);
            saveSurveys();
            showAlert(Alert.AlertType.INFORMATION, "Survey Updated", "Survey changes saved.");
        });

        editSurveyLayout.getChildren().addAll(titleLabel, titleField, questionLabel, questionField, optionsLabel, optionsListView, saveButton);

        return editSurveyLayout;
    }

    private void showProfile() {
        if (currentUserId != null) {
            User user = users.get(currentUserId);
            showAlert(Alert.AlertType.INFORMATION, "Profile", "Username: " + user.getUsername());
        }
    }

    private void confirmLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be logged out from your current session.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentUserId = null;
            userLabel.setText("");
            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been successfully logged out.");
            mainLayout.setCenter(createLoginForm());
        }
    }

    private void showHelp() {
        showAlert(Alert.AlertType.INFORMATION, "Help", "This is a basic survey application.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (HashMap<String, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            users = new HashMap<>();
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            out.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSurveys() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SURVEYS_FILE))) {
            surveys = (HashMap<String, Survey>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            surveys = new HashMap<>();
        }
    }

    private void saveSurveys() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SURVEYS_FILE))) {
            out.writeObject(surveys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

class Survey implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String question;
    private List<String> options;
    private String creatorId;

    public Survey(String title, String question, List<String> options) {
        this.title = title;
        this.question = question;
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
