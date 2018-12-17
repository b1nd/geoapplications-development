package hw3;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {
    private static final String PATH = "data\\shapes\\ru-mow\\mo.shp";
    private static final String SAVE = "src\\hw3\\data.js";

    private static void shapeFileToGeoJson() throws IOException {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("url", new File(PATH).toURI().toURL());
        }};
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features =
                dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();

        File resultFile = new File(SAVE);
        if (!resultFile.exists()) {
            resultFile.createNewFile();
            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.writeFeatureCollection(features, resultFile);
        }
    }

    public static void main(String[] args) throws IOException {
        FileUtils.shapeFileToGeoJson();
    }
}
