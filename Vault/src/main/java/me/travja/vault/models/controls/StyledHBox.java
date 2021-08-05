package me.travja.vault.models.controls;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import lombok.Data;

@Data
public class StyledHBox {

    private Pos alignment;

    private double spacing = -1d;

    private Background background;

    private Insets padding;

    private Node[] nodes;

    private String id;

    public StyledHBox(Node... nodes) {
        setNodes(nodes);
    }

    public StyledHBox alignment(Pos pos) {
        alignment = pos;
        return this;
    }

    public StyledHBox spacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public StyledHBox background(Background background) {
        this.background = background;
        return this;
    }

    public StyledHBox padding(Insets insets) {
        this.padding = insets;
        return this;
    }

    public StyledHBox id(String id) {
        this.id = id;
        return this;
    }

    //We can flesh this out more later I suppose

    public HBox build() {
        HBox box = new HBox(nodes);

        if (alignment != null)
            box.setAlignment(alignment);

        if (spacing != -1d)
            box.setSpacing(spacing);

        if (background != null)
            box.setBackground(background);

        if (padding != null)
            box.setPadding(padding);

        if (id != null)
            box.setId(id);

        return box;
    }

}
