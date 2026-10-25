package com.petshop.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * Tiện ích cho TableView: menu chuột phải (popup menu) để thao tác nhanh
 * và cột số thứ tự (STT).
 */
public final class TableMenuUtil {

    private TableMenuUtil() {
    }

    /** Một mục trong menu chuột phải; hành động nhận dòng đang chọn. */
    public static final class Item<T> {
        private final String text;
        private final Consumer<T> action;
        private final boolean separatorBefore;

        public Item(String text, Consumer<T> action) {
            this(text, action, false);
        }

        public Item(String text, Consumer<T> action, boolean separatorBefore) {
            this.text = text;
            this.action = action;
            this.separatorBefore = separatorBefore;
        }
    }

    /**
     * Gắn menu chuột phải vào bảng. Bấm chuột phải lên dòng nào thì chọn dòng đó
     * rồi hiện menu; bấm vào vùng trống thì các mục bị vô hiệu hóa.
     */
    @SafeVarargs
    public static <T> void attach(TableView<T> table, Item<T>... items) {
        ContextMenu menu = new ContextMenu();
        for (Item<T> it : items) {
            if (it.separatorBefore) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            MenuItem mi = new MenuItem(it.text);
            mi.setOnAction(e -> {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    it.action.accept(selected);
                }
            });
            menu.getItems().add(mi);
        }

        table.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                return;
            }
            Node n = e.getPickResult().getIntersectedNode();
            while (n != null && n != table && !(n instanceof TableRow)) {
                n = n.getParent();
            }
            if (n instanceof TableRow<?> row && !row.isEmpty()) {
                table.getSelectionModel().select(row.getIndex());
            } else {
                table.getSelectionModel().clearSelection();
            }
        });

        menu.setOnShowing(e -> {
            boolean none = table.getSelectionModel().getSelectedItem() == null;
            for (MenuItem m : menu.getItems()) {
                if (!(m instanceof SeparatorMenuItem)) {
                    m.setDisable(none);
                }
            }
        });
        table.setContextMenu(menu);
    }

    /** Biến một cột thành cột số thứ tự (1, 2, 3, ...). */
    public static <T> void indexColumn(TableColumn<T, Void> column) {
        column.setSortable(false);
        column.setCellValueFactory(c -> new SimpleObjectProperty<Void>());
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
    }
}
