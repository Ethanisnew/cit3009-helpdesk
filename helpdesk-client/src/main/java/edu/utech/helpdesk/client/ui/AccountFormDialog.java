package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.Role;
import edu.utech.helpdesk.client.model.TechnicianDto;
import edu.utech.helpdesk.client.model.UserDto;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Add / edit dialog for users and technicians (administrator screens).
 * Returns the JSON body to send to the REST service.
 */
public final class AccountFormDialog {

    private AccountFormDialog() {
    }

    /** @param existing null to create a new user */
    public static Optional<Map<String, Object>> showUser(UserDto existing) {
        boolean creating = existing == null;
        TextField fullName = new TextField(creating ? "" : existing.fullName());
        TextField username = new TextField(creating ? "" : existing.username());
        TextField email = new TextField(creating ? "" : existing.email());
        PasswordField password = new PasswordField();
        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.values());
        role.setConverter(roleConverter());
        role.setValue(creating || existing.role() == null ? Role.USER : existing.role());
        role.setMaxWidth(Double.MAX_VALUE);
        username.setDisable(!creating);

        VBox form = form(Ui.field("Full name", fullName), Ui.field("Username", username), Ui.field("Email", email));
        if (creating) {
            form.getChildren().add(Ui.field("Initial password", password));
        }
        form.getChildren().add(Ui.field("Role", role));

        return FormDialog.show(creating ? "Add User" : "Edit User",
                creating ? "Create a new user account" : "Edit " + existing.username(),
                creating ? "Create user" : "Save changes", form,
                () -> {
                    FormValidator v = new FormValidator()
                            .required(fullName, "Full name")
                            .required(username, "Username")
                            .minLength(username, "Username", 3)
                            .required(email, "Email")
                            .email(email, "Email")
                            .selected(role, "Role");
                    if (creating) {
                        v.required(password, "Password").minLength(password, "Password", 6);
                    }
                    return v.validateOrAlert();
                },
                () -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("fullName", fullName.getText().trim());
                    body.put("username", username.getText().trim());
                    body.put("email", email.getText().trim());
                    body.put("role", role.getValue().name());
                    if (creating) {
                        body.put("password", password.getText());
                    }
                    return body;
                });
    }

    /** @param existing null to create a new technician */
    public static Optional<Map<String, Object>> showTechnician(TechnicianDto existing) {
        boolean creating = existing == null;
        TextField fullName = new TextField(creating ? "" : existing.fullName());
        TextField username = new TextField(creating ? "" : existing.username());
        TextField email = new TextField(creating ? "" : existing.email());
        TextField specialization = new TextField(creating || existing.specialization() == null ? "" : existing.specialization());
        specialization.setPromptText("e.g. Networking, Hardware, Software");
        PasswordField password = new PasswordField();
        username.setDisable(!creating);

        VBox form = form(Ui.field("Full name", fullName), Ui.field("Username", username), Ui.field("Email", email),
                Ui.field("Specialization", specialization));
        if (creating) {
            form.getChildren().add(Ui.field("Initial password", password));
        }

        return FormDialog.show(creating ? "Add Technician" : "Edit Technician",
                creating ? "Register a new technician" : "Edit " + existing.fullName(),
                creating ? "Add technician" : "Save changes", form,
                () -> {
                    FormValidator v = new FormValidator()
                            .required(fullName, "Full name")
                            .required(username, "Username")
                            .minLength(username, "Username", 3)
                            .required(email, "Email")
                            .email(email, "Email");
                    if (creating) {
                        v.required(password, "Password").minLength(password, "Password", 6);
                    }
                    return v.validateOrAlert();
                },
                () -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("fullName", fullName.getText().trim());
                    body.put("username", username.getText().trim());
                    body.put("email", email.getText().trim());
                    body.put("specialization", specialization.getText().trim());
                    if (creating) {
                        body.put("password", password.getText());
                    }
                    return body;
                });
    }

    private static VBox form(Node... rows) {
        VBox box = new VBox(12, rows);
        box.setPrefWidth(380);
        return box;
    }

    private static StringConverter<Role> roleConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Role r) {
                return r == null ? "" : r.label();
            }

            @Override
            public Role fromString(String s) {
                return null;
            }
        };
    }
}
