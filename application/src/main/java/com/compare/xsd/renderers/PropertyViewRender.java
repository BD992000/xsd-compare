package com.compare.xsd.renderers;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.XsdNode;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
public class PropertyViewRender {
    private final TableView<Property> propertyView;

    /**
     * Initialize a new instance of {@link PropertyViewRender}.
     *
     * @param propertyView Set the view to use for rendering.
     */
    public PropertyViewRender(TableView<Property> propertyView) {
        this.propertyView = propertyView;

        init();
    }

    //region Methods

    /**
     * Render the properties of the given node.
     *
     * @param node
     */
    public void render(XsdNode node) {
        ObservableList<Property> items = this.propertyView.getItems();
        Modifications modifications = node.getModifications() != null ? node.getModifications() : new Modifications(ModificationType.NONE);

        items.clear();
        items.add(new Property("Name", node.getName(), modifications.isNameChanged()));
        items.add(new Property("Type", node.getType(), modifications.isTypeChanged()));
        items.add(new Property("Cardinality", node.getCardinality(), modifications.isCardinalityChanged()));
    }

    //endregion

    //region Functions

    private void init() {
        addPropertyColumn();
        addValueColumn();
    }

    private void addPropertyColumn() {
        TableColumn<Property, String> property = createNewColumn("Property");

        property.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProperty()));
    }

    private void addValueColumn() {
        TableColumn<Property, String> property = createNewColumn("Value");

        property.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
    }

    private TableColumn<Property, String> createNewColumn(String name) {
        TableColumn<Property, String> column = new TableColumn<>(name);

        column.prefWidthProperty().bind(propertyView.widthProperty().divide(2));
        column.setSortable(false);
        column.setEditable(false);

        column.setCellFactory(new Callback<TableColumn<Property, String>, TableCell<Property, String>>() {
            @Override
            public TableCell<Property, String> call(TableColumn<Property, String> param) {
                return new TableCell<Property, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        Property property = (Property) this.getTableRow().getItem();

                        if (property != null && property.isModified()) {
                            setTextFill(Paint.valueOf("#be0000"));
                        } else {
                            setTextFill(Paint.valueOf("#000000"));
                        }
                    }
                };
            }
        });

        propertyView.getColumns().add(column);

        return column;
    }

    //endregion

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Property {
        private String property;
        private String value;
        private boolean modified;
    }
}
