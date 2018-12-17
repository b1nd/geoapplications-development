package hw1;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class ShapeViewer extends ApplicationTemplate {

    public static void main(String[] args) {
        start("World Wind Shapefile Viewer", ShapeViewer.AppFrame.class);
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame
            implements ShapefileLayerFactory.CompletionCallback {

        static final LatLon KREMLIN = LatLon.fromDegrees(55.752023, 37.617499);

        ArrayList<Double> distances = new ArrayList<>();
        ArrayList<ShapefileRenderable.Record> records = new ArrayList<>();

        private double maxDistance = Double.NEGATIVE_INFINITY;

        public AppFrame() {
            makeMenu(this);
        }

        void loadShapeFile(Object source) {
            ShapefileLayerFactory factory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(
                    AVKey.SHAPEFILE_LAYER_FACTORY);
            factory.setAttributeDelegate(new ShapefileRenderable.AttributeDelegate() {
                @Override
                public void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord) {
                    records.add(renderableRecord);
                    double distance = LatLon.linearDistance(renderableRecord.getSector().getCentroid(), KREMLIN).degrees;

                    if (distance > maxDistance) {
                        maxDistance = distance;
                    }
                    distances.add(distance);
                }
            });
            factory.createFromShapefileSource(source, this);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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

            Color[] gradient = new Color[256];
            for (int i = 0; i < gradient.length; ++i) {
                gradient[i] = new Color(200, i, 50);
            }
            for (int i = 0; i < records.size(); ++i) {
                records.get(i).setAttributes(makeAttributes(gradient[(int) (distances.get(i) / maxDistance * 255)]));
            }

            Layer layer = (Layer) result;
            layer.setName(WWIO.getFilename(layer.getName()));
            this.getWwd().getModel().getLayers().add(layer);

            Sector sector = (Sector) layer.getValue(AVKey.SECTOR);
            if (sector != null) {
                ExampleUtil.goTo(this.getWwd(), sector);
            }

            this.setCursor(null);
        }

        @Override
        public void exception(Exception e) {
            Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
    }

    private static ShapeAttributes makeAttributes(Color color) {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(new Material(color));
        return attributes;
    }

    private static void makeMenu(final ShapeViewer.AppFrame appFrame) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[1]);

        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...") {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int status = fileChooser.showOpenDialog(appFrame);
                    if (status == JFileChooser.APPROVE_OPTION) {
                        for (File file : fileChooser.getSelectedFiles()) {
                            appFrame.loadShapeFile(file);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openFileMenuItem);

        JMenuItem openURLMenuItem = new JMenuItem(new AbstractAction("Open URL...") {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    String status = JOptionPane.showInputDialog(appFrame, "URL");

                    if (!WWUtil.isEmpty(status)) {
                        appFrame.loadShapeFile(status.trim());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openURLMenuItem);
    }
}
