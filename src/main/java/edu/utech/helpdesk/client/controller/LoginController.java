package edu.utech.helpdesk.client.controller;

import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.service.ApiException;
import edu.utech.helpdesk.client.ui.AlertUtil;
import edu.utech.helpdesk.client.ui.FormValidator;
import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Navigator;
import edu.utech.helpdesk.client.view.LoginView;
import javafx.application.Platform;

/** Handles sign-in and self-registration. */
public final class LoginController {

    private static final System.Logger LOG = System.getLogger(LoginController.class.getName());
    private static final String USERNAME_RULE = "[A-Za-z0-9._-]{3,30}";

    private final LoginView v;
    private final ApiClient api = ApiClient.get();

    public LoginController(LoginView view) {
        this.v = view;
        v.loginButton.setOnAction(e -> login());
        v.password.setOnAction(e -> login());
        v.registerButton.setOnAction(e -> register());
        v.showRegisterLink.setOnAction(e -> v.showRegister(true));
        v.showLoginLink.setOnAction(e -> v.showRegister(false));
        Platform.runLater(v.username::requestFocus);
    }

    private void login() {
        boolean valid = new FormValidator()
                .required(v.username, "Username")
                .required(v.password, "Password")
                .validateOrAlert();
        if (!valid) {
            return;
        }
        String username = v.username.getText().trim();
        String password = v.password.getText();

        setSigningIn(true);
        Async.run(() -> api.login(username, password),
                user -> {
                    if (user == null) {
                        setSigningIn(false);
                        AlertUtil.error("Invalid Login Credentials", "The server did not return an account for these details.");
                        return;
                    }
                    Navigator.showDashboardFor(user);
                },
                err -> {
                    setSigningIn(false);
                    LOG.log(System.Logger.Level.WARNING, "Login failed for {0}: {1}", username, err.getMessage());
                    AlertUtil.showError(err);
                    v.password.clear();
                    v.password.requestFocus();
                });
    }

    private void register() {
        String username = v.regUsername.getText() == null ? "" : v.regUsername.getText().trim();
        boolean valid = new FormValidator()
                .required(v.regFullName, "Full name")
                .minLength(v.regFullName, "Full name", 2)
                .required(v.regUsername, "Username")
                .custom(v.regUsername, username.isBlank() || username.matches(USERNAME_RULE),
                        "Username must be 3–30 characters: letters, numbers, . _ -")
                .required(v.regEmail, "Email")
                .email(v.regEmail, "Email")
                .required(v.regPassword, "Password")
                .minLength(v.regPassword, "Password", 6)
                .required(v.regConfirm, "Confirm password")
                .matches(v.regPassword, v.regConfirm, "The two passwords do not match.")
                .validateOrAlert();
        if (!valid) {
            return;
        }
        String fullName = v.regFullName.getText().trim();
        String email = v.regEmail.getText().trim();
        String password = v.regPassword.getText();

        setRegistering(true);
        Async.run(() -> api.register(fullName, username, email, password),
                created -> {
                    setRegistering(false);
                    clearRegisterForm();
                    v.username.setText(username);
                    v.password.clear();
                    v.showRegister(false);
                    AlertUtil.info("Account created", "You can now sign in as \"" + username + "\".");
                    v.password.requestFocus();
                },
                err -> {
                    setRegistering(false);
                    boolean duplicate = err instanceof ApiException ae && ae.kind() == ApiException.Kind.CONFLICT;
                    AlertUtil.showError(err, duplicate ? "Duplicate User Account" : "Registration Failed");
                });
    }

    private void setSigningIn(boolean busy) {
        v.loginSpinner.setVisible(busy);
        v.loginButton.setDisable(busy);
        v.username.setDisable(busy);
        v.password.setDisable(busy);
    }

    private void setRegistering(boolean busy) {
        v.registerSpinner.setVisible(busy);
        v.registerButton.setDisable(busy);
    }

    private void clearRegisterForm() {
        v.regFullName.clear();
        v.regUsername.clear();
        v.regEmail.clear();
        v.regPassword.clear();
        v.regConfirm.clear();
    }
}
