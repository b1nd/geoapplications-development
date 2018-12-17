package hw2.utils;

import hw2.paths.Paths;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public enum SaveType implements Saveable {
    SHAPE {
        @Override
        public void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws IOException {
            File resultFile = new File(Paths.SAVE_TO + "result.shp");
            if (!resultFile.exists())
                resultFile.createNewFile();

            Map<String, Serializable> params = new HashMap<>();
            params.put("url", resultFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore newDataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);

            newDataStore.createSchema(features.getSchema());
            SimpleFeatureStore featureStore = (SimpleFeatureStore) newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
            Transaction transaction = new DefaultTransaction();

            try {
                featureStore.addFeatures(features);
                transaction.commit();
            } catch (IOException ex) {
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }
    },
    KML {
        @Override
        public void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws IOException {
            File resultFile = new File(Paths.SAVE_TO + "result.kml");
            if (!resultFile.exists())
                resultFile.createNewFile();

            Encoder encoder = new Encoder(new KMLConfiguration());
            encoder.setIndenting(true);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            encoder.encode(features, org.geotools.kml.KML.kml, bos);

            try (FileWriter writer = new FileWriter(resultFile)) {
                writer.write(bos.toString());
            }
        }
    },
    GEOJSON {
        @Override
        public void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws IOException {
            File resultFile = new File(Paths.SAVE_TO + "result.geojson");
            if (!resultFile.exists())
                resultFile.createNewFile();

            FeatureJSON featureJSON = new FeatureJSON();
            featureJSON.writeFeatureCollection(features, resultFile);
        }
    },
    ALL {
        @Override
        public void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws IOException {
            SHAPE.save(features);
            KML.save(features);
            GEOJSON.save(features);
        }
    }
}

interface Saveable {
    void save(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws IOException;
}
