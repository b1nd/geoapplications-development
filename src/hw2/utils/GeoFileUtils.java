package hw2.utils;

import hw2.data.Band;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeoFileUtils {

    private GeoFileUtils() {
    }

    public static void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features, SaveType type) throws IOException {
        type.save(features);
    }

    public static Band readTiffFile(String path) throws IOException {
        GeoTiffReader reader = new GeoTiffReader(new File(path));
        Band nir = new Band();
        nir.setCoverage(reader.read(null));
        nir.setImage(nir.getCoverage().getRenderedImage());

        return nir;
    }

    public static FeatureCollection<SimpleFeatureType, SimpleFeature> readShapeFile(String path) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("url", new File(path).toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);

        return dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();
    }
}
