package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.CommentDto;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Format;
import edu.utech.helpdesk.client.util.Navigator;
import edu.utech.helpdesk.client.util.Session;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

/** Read-only ticket details plus the comment thread (view and add comments). */
public final class TicketDetailDialog {

    private TicketDetailDialog() {
    }

    public static void show(TicketDto ticket) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(Navigator.stage());
        dialog.setTitle("Ticket #" + ticket.id());
        dialog.getDialogPane().getStylesheets().add(Theme.css());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label subject = Ui.title(ticket.subject());
        HBox badges = new HBox(8, Badges.of(ticket.status()), Badges.of(ticket.priority()));

        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(6);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(90);
        meta.getColumnConstraints().add(labelCol);
        addMeta(meta, 0, "Requester", Format.orDash(ticket.userName()));
        addMeta(meta, 1, "Technician", ticket.technicianName() == null ? "Unassigned" : ticket.technicianName());
        addMeta(meta, 2, "Created", Format.dateTime(ticket.creationDate()));

        TextArea description = readOnly(ticket.description(), 4);
        VBox content = new VBox(12, subject, badges, meta, Ui.field("Description", description));

        if (ticket.resolutionNotes() != null && !ticket.resolutionNotes().isBlank()) {
            content.getChildren().add(Ui.field("Resolution notes", readOnly(ticket.resolutionNotes(), 3)));
        }

        ListView<CommentDto> comments = new ListView<>();
        comments.setPrefHeight(170);
        comments.setPlaceholder(new Label("No comments yet"));
        comments.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CommentDto c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setGraphic(null);
                    return;
                }
                Label who = new Label(Objects.toString(c.authorName(), "Unknown"));
                who.getStyleClass().add("comment-author");
                Label when = new Label(Format.dateTime(c.createdAt()));
                when.getStyleClass().add("muted");
                Label body = new Label(c.content());
                body.setWrapText(true);
                body.prefWidthProperty().bind(lv.widthProperty().subtract(40));
                setGraphic(new VBox(2, new HBox(8, who, when), body));
            }
        });

        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment…");
        Button post = Ui.primary(new Button("Post"));
        HBox composer = new HBox(8, commentField, post);
        Ui.growH(commentField);
        content.getChildren().addAll(Ui.field("Comments", comments), composer);

        Runnable load = () -> Async.run(
                () -> ApiClient.get().getComments(ticket.id()),
                list -> comments.getItems().setAll(list),
                err -> comments.setPlaceholder(new Label("Could not load comments: " + err.getMessage())));

        post.setOnAction(e -> {
            String text = commentField.getText() == null ? "" : commentField.getText().trim();
            if (text.isEmpty()) {
                new FormValidator().required(commentField, "Comment").validateOrAlert();
                return;
            }
            post.setDisable(true);
            Async.run(() -> ApiClient.get().addComment(ticket.id(), Session.user().id(), text),
                    saved -> {
                        post.setDisable(false);
                        commentField.clear();
                        load.run();
                    },
                    err -> {
                        post.setDisable(false);
                        AlertUtil.showError(err);
                    });
        });
        commentField.setOnAction(e -> post.fire());

        content.setPadding(new Insets(4));
        content.setPrefWidth(600);
        dialog.getDialogPane().setContent(content);
        load.run();
        dialog.showAndWait();
    }

    private static void addMeta(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        grid.add(l, 0, row);
        grid.add(new Label(value), 1, row);
    }

    private static TextArea readOnly(String text, int rows) {
        TextArea area = new TextArea(text == null ? "" : text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        return area;
    }
}
