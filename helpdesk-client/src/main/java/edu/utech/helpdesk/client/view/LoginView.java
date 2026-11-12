package edu.utech.helpdesk.client.view;

import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.ui.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Login screen with an inline "create account" card. Layout only: events live in LoginController. */
public final class LoginView {

    public final HBox root = new HBox();

    // sign-in card
    public final TextField username = new TextField();
    public final PasswordField password = new PasswordField();
    public final Button loginButton = Ui.primary(new Button("Sign in"));
    public final ProgressIndicator loginSpinner = new ProgressIndicator();
    public final Hyperlink showRegisterLink = new Hyperlink("Create an account");

    // register card
    public final TextField regFullName = new TextField();
    public final TextField regUsername = new TextField();
    public final TextField regEmail = new TextField();
    public final PasswordField regPassword = new PasswordField();
    public final PasswordField regConfirm = new PasswordField();
    public final Button registerButton = Ui.primary(new Button("Create account"));
    public final ProgressIndicator registerSpinner = new ProgressIndicator();
    public final Hyperlink showLoginLink = new Hyperlink("Back to sign in");

    private final VBox signInCard;
    private final VBox registerCard;

    public LoginView() {
        root.getChildren().addAll(brandPanel(), formSide());
        Ui.growH(root.getChildren().get(1));

        signInCard = buildSignInCard();
        registerCard = buildRegisterCard();
        StackPane stack = (StackPane) root.getChildren().get(1);
        stack.getChildren().addAll(signInCard, registerCard);
        showRegister(false);
    }

    public void showRegister(boolean register) {
        signInCard.setVisible(!register);
        signInCard.setManaged(!register);
        registerCard.setVisible(register);
        registerCard.setManaged(register);
    }

    private VBox brandPanel() {
        Label title = new Label("University\nHelp Desk");
        title.getStyleClass().add("brand-title");
        Region rule = new Region();
        rule.getStyleClass().add("brand-rule");
        Label sub = new Label("Report it. Track it. Get it fixed.");
        sub.getStyleClass().add("brand-sub");

        VBox points = new VBox(10,
                point("Submit and follow support tickets"),
                point("Live notifications as status changes"),
                point("Technicians and administrators in one place"));

        VBox panel = new VBox(18, title, rule, sub, new Region(), points);
        VBox.setVgrow(panel.getChildren().get(3), javafx.scene.layout.Priority.ALWAYS);
        panel.getStyleClass().add("brand-panel");
        panel.setPadding(new Insets(56, 48, 48, 48));
        panel.setPrefWidth(400);
        panel.setMinWidth(400);
        panel.setMaxWidth(400);
        return panel;
    }

    private static Label point(String text) {
        Label l = new Label("✓  " + text);
        l.getStyleClass().add("brand-point");
        return l;
    }

    private StackPane formSide() {
        StackPane side = new StackPane();
        side.getStyleClass().add("form-side");
        side.setPadding(new Insets(32));
        return side;
    }

    private VBox buildSignInCard() {
        username.setPromptText("Your username");
        password.setPromptText("Your password");
        fill(username, password, loginButton);
        loginButton.setDefaultButton(true);
        loginSpinner.setPrefSize(22, 22);
        loginSpinner.setVisible(false);

        Label server = Ui.muted("Server: " + ApiClient.get().baseUrl());
        server.setAlignment(Pos.CENTER);

        HBox buttonRow = new HBox(10, loginButton, loginSpinner);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        Ui.growH(loginButton);

        HBox switchRow = new HBox(4, Ui.muted("New here?"), showRegisterLink);
        switchRow.setAlignment(Pos.CENTER);

        return centered(Ui.card(16,
                Ui.title("Welcome back"),
                Ui.muted("Sign in to report or manage support tickets."),
                Ui.field("Username", username),
                Ui.field("Password", password),
                buttonRow,
                switchRow,
                server));
    }

    private VBox buildRegisterCard() {
        regFullName.setPromptText("e.g. Jordan Brown");
        regUsername.setPromptText("3–30 letters, numbers, . _ -");
        regEmail.setPromptText("name@example.com");
        regPassword.setPromptText("At least 6 characters");
        regConfirm.setPromptText("Repeat the password");
        fill(regFullName, regUsername, regEmail, regPassword, regConfirm, registerButton);
        registerSpinner.setPrefSize(22, 22);
        registerSpinner.setVisible(false);

        HBox buttonRow = new HBox(10, registerButton, registerSpinner);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        Ui.growH(registerButton);

        HBox switchRow = new HBox(4, Ui.muted("Already registered?"), showLoginLink);
        switchRow.setAlignment(Pos.CENTER);

        return centered(Ui.card(12,
                Ui.title("Create your account"),
                Ui.field("Full name", regFullName),
                Ui.field("Username", regUsername),
                Ui.field("Email", regEmail),
                Ui.field("Password", regPassword),
                Ui.field("Confirm password", regConfirm),
                buttonRow,
                switchRow));
    }

    private static VBox centered(VBox card) {
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(card, Pos.CENTER);
        return card;
    }

    private static void fill(javafx.scene.layout.Region... nodes) {
        for (Region n : nodes) {
            n.setMaxWidth(Double.MAX_VALUE);
        }
    }
}
