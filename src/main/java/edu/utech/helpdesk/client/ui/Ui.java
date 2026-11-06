package edu.utech.helpdesk.client.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Function;

/** Small helpers so the view classes stay readable. */
public final class Ui {

    private Ui() {
    }

    public static Label title(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("page-title");
        l.setWrapText(true);
        return l;
    }

    public static Label muted(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        l.setWrapText(true);
        return l;
    }

    public static VBox field(String label, Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        return new VBox(4, l, control);
    }

    public static <B extends ButtonBase> B primary(B button) {
        button.getStyleClass().add("btn-primary");
        return button;
    }

    public static <B extends ButtonBase> B danger(B button) {
        button.getStyleClass().add("btn-danger");
        return button;
    }

    public static VBox card(double spacing, Node... children) {
        VBox box = new VBox(spacing, children);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(20));
        return box;
    }

    public static VBox statTile(String caption, Label value) {
        value.getStyleClass().add("stat-value");
        Label c = new Label(caption);
        c.getStyleClass().add("stat-label");
        VBox box = new VBox(2, value, c);
        box.getStyleClass().add("stat-card");
        HBox.setHgrow(box, javafx.scene.layout.Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public static void growV(Node n) {
        VBox.setVgrow(n, javafx.scene.layout.Priority.ALWAYS);
    }

    public static void growH(Node n) {
        HBox.setHgrow(n, javafx.scene.layout.Priority.ALWAYS);
    }

    public static Region spacer() {
        Region r = new Region();
        growH(r);
        return r;
    }

    public static <T> TableColumn<T, String> column(String title, Function<T, String> getter, double width) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cd -> new ReadOnlyStringWrapper(Objects.toString(getter.apply(cd.getValue()), "")));
        col.setPrefWidth(width);
        return col;
    }

    public static <T> TableColumn<T, Number> numberColumn(String title, Function<T, Number> getter, double width) {
        TableColumn<T, Number> col = new TableColumn<>(title);
        col.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<Number>(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    /** Shows {@code items} in the table while keeping the user's column sorting. */
    public static <T> void bindSorted(TableView<T> table, ObservableList<T> items) {
        SortedList<T> sorted = new SortedList<>(items);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }
}
