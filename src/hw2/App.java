package hw2;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import hw2.calc.Calculator;
import hw2.paths.Paths;
import hw2.utils.GeoFileUtils;
import hw2.utils.SaveType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class App implements ShapefileLayerFactory.CompletionCallback {
    private final JFrame mainWindow;
    private WorldWindowGLCanvas worldWindow;
    private ArrayList<ShapefileRenderable.Record> records;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> features;

    private App() {
        records = new ArrayList<>();

        mainWindow = new JFrame("NDVI");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setSize(800, 600);
        mainWindow.setLocationRelativeTo(null);

        worldWindow = new WorldWindowGLCanvas();
        worldWindow.setModel(new BasicModel());

        Panel panel = new Panel(new BorderLayout());
        panel.add(worldWindow, BorderLayout.CENTER);

        mainWindow.add(panel);
    }

    public static void main(String[] args) throws IOException, TransformException {
        new App().start();
    }

    private void start() throws IOException, TransformException {
        mainWindow.setVisible(true);

        System.out.println("-----calculating features!");

        Calculator calc = new Calculator();
        features = calc.calculateUpdatedFeatures();

        System.out.println("-----features calculated!");

        GeoFileUtils.save(features, SaveType.ALL);

        System.out.println("-----files saved!");

        loadShapeFile();
    }

    @Override
    public void completion(final Object result) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    completion(result);
                }
            });
            return;
        }

        ArrayList<Color> colors = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> iterator = features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                String colorStr = (String) feature.getAttribute("fill");
                colors.add(new Color(
                        Integer.valueOf(colorStr.substring(1, 3), 16),
                        Integer.valueOf(colorStr.substring(3, 5), 16),
                        Integer.valueOf(colorStr.substring(5, 7), 16)));
            }
        }

        for (int i = 0; i < records.size(); ++i) {
            records.get(i).setAttributes(makeAttributes(colors.get(i)));
        }

        Layer layer = (Layer) result;
        worldWindow.getModel().getLayers().add(layer);

        Sector sector = (Sector) layer.getValue(AVKey.SECTOR);
        if (sector != null) {
            ExampleUtil.goTo(worldWindow, sector);
        }
    }

    @Override
    public void exception(Exception e) {
        e.printStackTrace();
    }

    private void loadShapeFile() {
        ShapefileLayerFactory factory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(AVKey.SHAPEFILE_LAYER_FACTORY);
        factory.setAttributeDelegate(new ShapefileRenderable.AttributeDelegate() {
            @Override
            public void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord) {
                records.add(renderableRecord);
            }
        });
        factory.createFromShapefileSource(Paths.SAVE_TO + "result.shp", this);
    }

    private static ShapeAttributes makeAttributes(Color color) {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(new Material(color));
        return attributes;
    }
}
