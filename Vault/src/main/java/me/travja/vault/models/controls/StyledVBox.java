package me.travja.vault.models.controls;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import lombok.Data;

@Data
public class StyledVBox {

    private Pos alignment;

    private double spacing = -1d;

    private Background background;

    private Insets padding;

    private Node[] nodes;

    public StyledVBox(Node... nodes) {
        setNodes(nodes);
    }

    public StyledVBox alignment(Pos pos) {
        alignment = pos;
        return this;
    }

    public StyledVBox spacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public StyledVBox background(Background background) {
        this.background = background;
        return this;
    }

    public StyledVBox padding(Insets insets) {
        this.padding = insets;
        return this;
    }

    //We can flesh this out more later I suppose

    public VBox build() {
        VBox box = new VBox(nodes);

        if (alignment != null)
            box.setAlignment(alignment);

        if (spacing != -1d)
            box.setSpacing(spacing);

        if (background != null)
            box.setBackground(background);

        if (padding != null)
            box.setPadding(padding);

        return box;
    }

}
