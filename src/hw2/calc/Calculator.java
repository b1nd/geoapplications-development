package hw2.calc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.MultiPolygon;
import hw2.data.Band;
import hw2.data.NDVI;
import hw2.paths.Paths;
import hw2.utils.GeoFileUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.data.DataUtilities;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Calculator {
    private final Band nirBand;
    private final Band redBand;
    private final FeatureCollection<SimpleFeatureType, SimpleFeature> shape;
    private final FeatureProcessor processor;

    public Calculator() throws IOException {
        processor = new FeatureProcessor();
        nirBand = GeoFileUtils.readTiffFile(Paths.NIR);
        redBand = GeoFileUtils.readTiffFile(Paths.RED);
        shape = GeoFileUtils.readShapeFile(Paths.SHAPE);
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> calculateUpdatedFeatures() throws TransformException {
        List<SimpleFeature> updatedFeatures = new ArrayList<>();
        final FeatureType featureType = processor.getUpdatedFeatureType(shape.getSchema());

        try (FeatureIterator<SimpleFeature> iterator = shape.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                // Получение, копирование и репроекция полигонов.
                MultiPolygon multiPolygon = (MultiPolygon) ((MultiPolygon) feature.getDefaultGeometry()).clone();
                multiPolygon.apply(new CoordinateFilter() {
                    @Override
                    public void filter(Coordinate coordinate) {
                        try {
                            double[] latLon = {coordinate.x, coordinate.y};
                            double[] coordinates = new double[2];
                            MathTransform transform = CRS.findMathTransform(featureType.getCoordinateReferenceSystem(),
                                    nirBand.getCoverage().getCoordinateReferenceSystem2D());
                            transform.transform(latLon, 0, coordinates, 0, 1);
                            coordinate.x = coordinates[0];
                            coordinate.y = coordinates[1];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Coverage ndviCoverage = getShapeNDVICoverage(multiPolygon)/*getBoundaryNDVICoverage(feature.getBounds())*/;
                NDVI ndvi = getNDVIData(ndviCoverage);
                updatedFeatures.add((SimpleFeature) processor.getUpdatedFeature(feature, featureType, ndvi));
            }
        }
        return DataUtilities.collection(updatedFeatures);
    }

    // Bounding box
    /*private Coverage getBoundaryNDVICoverage(Envelope envelope) throws TransformException {
        Coverage boxedNirCoverage = cropCoverage(nirBand.getCoverage(), envelope);
        Coverage boxedRedCoverage = cropCoverage(redBand.getCoverage(), envelope);

        RenderedImage boxedNirImage = ((GridCoverage2D) boxedNirCoverage).getRenderedImage();
        RenderedImage boxedRedImage = ((GridCoverage2D) boxedRedCoverage).getRenderedImage();

        ParameterBlock pbSubtracted = new ParameterBlock();
        pbSubtracted.addSource(boxedNirImage);
        pbSubtracted.addSource(boxedRedImage);
        RenderedOp subtractedImage = JAI.create("subtract", pbSubtracted);

        ParameterBlock pbAdded = new ParameterBlock();
        pbAdded.addSource(boxedNirImage);
        pbAdded.addSource(boxedRedImage);
        RenderedOp addedImage = JAI.create("add", pbAdded);

        RenderedOp typeAdd = FormatDescriptor.create(addedImage, DataBuffer.TYPE_DOUBLE, null);
        RenderedOp typeSub = FormatDescriptor.create(subtractedImage, DataBuffer.TYPE_DOUBLE, null);
        ParameterBlock pbNDVI = new ParameterBlock();
        pbNDVI.addSource(typeSub);
        pbNDVI.addSource(typeAdd);

        RenderedOp NDVIop = JAI.create("divide", pbNDVI);
        GridCoverageFactory gridCoverageFactory = new GridCoverageFactory();

        return gridCoverageFactory.create("Raster", NDVIop, redBand.getCoverage().getEnvelope());
    }*/

    private Coverage getShapeNDVICoverage(MultiPolygon multiPolygon) throws TransformException {
        Coverage nirCoverage = cropCoverage(nirBand.getCoverage(), multiPolygon);
        Coverage redCoverage = cropCoverage(redBand.getCoverage(), multiPolygon);

        RenderedImage nirImage = ((GridCoverage2D) nirCoverage).getRenderedImage();
        RenderedImage redImage = ((GridCoverage2D) redCoverage).getRenderedImage();

        ParameterBlock pbSubtracted = new ParameterBlock();
        pbSubtracted.addSource(nirImage);
        pbSubtracted.addSource(redImage);
        RenderedOp subtractedImage = JAI.create("subtract", pbSubtracted);

        ParameterBlock pbAdded = new ParameterBlock();
        pbAdded.addSource(nirImage);
        pbAdded.addSource(redImage);
        RenderedOp addedImage = JAI.create("add", pbAdded);

        RenderedOp typeAdd = FormatDescriptor.create(addedImage, DataBuffer.TYPE_DOUBLE, null);
        RenderedOp typeSub = FormatDescriptor.create(subtractedImage, DataBuffer.TYPE_DOUBLE, null);
        ParameterBlock pbNDVI = new ParameterBlock();
        pbNDVI.addSource(typeSub);
        pbNDVI.addSource(typeAdd);

        RenderedOp NDVIop = JAI.create("divide", pbNDVI);
        GridCoverageFactory gridCoverageFactory = new GridCoverageFactory();

        return gridCoverageFactory.create("Raster", NDVIop, redBand.getCoverage().getEnvelope());
    }

    // Bounding box
    /*private Coverage cropCoverage(Coverage coverage, Envelope envelope) throws TransformException {
        CoverageProcessor processor = CoverageProcessor.getInstance();
        GeneralEnvelope transformedEnvelope = CRS.transform(envelope, coverage.getCoordinateReferenceSystem());
        ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("Envelope").setValue(transformedEnvelope);

        return processor.doOperation(param);
    }*/

    private Coverage cropCoverage(Coverage coverage, MultiPolygon multiPolygon) throws TransformException {
        CoverageProcessor processor = CoverageProcessor.getInstance();
        ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("ROI").setValue(multiPolygon);

        return processor.doOperation(param);
    }

    private NDVI getNDVIData(Coverage coverage) {
        RenderedImage image = ((GridCoverage2D) coverage).getRenderedImage();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double avg = 0;
        double[] ndvis = image.getData().getPixels(
                image.getMinX(),
                image.getMinY(),
                image.getData().getWidth(),
                image.getData().getHeight(),
                (double[]) null);
        int count = 0;

        for (double ndvi : ndvis) {
            // Проверка на значение NoData.
            if (ndvi != -0) {
                if (ndvi < min) {
                    min = ndvi;
                }
                if (ndvi > max) {
                    max = ndvi;
                }
                avg += ndvi;
                count++;
            }
        }
        avg /= count;

        return new NDVI(min, max, avg);
    }

}
