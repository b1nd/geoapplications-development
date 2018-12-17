package hw2.data;

import org.geotools.coverage.grid.GridCoverage2D;

import java.awt.image.RenderedImage;

public class Band {
    private RenderedImage image;
    private GridCoverage2D coverage;

    public Band() {
    }

    public Band(RenderedImage image, GridCoverage2D coverage) {
        this.image = image;
        this.coverage = coverage;
    }

    public RenderedImage getImage() {
        return image;
    }

    public void setImage(RenderedImage image) {
        this.image = image;
    }

    public GridCoverage2D getCoverage() {
        return coverage;
    }

    public void setCoverage(GridCoverage2D coverage) {
        this.coverage = coverage;
    }
}
