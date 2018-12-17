package hw2.calc;

import hw2.data.NDVI;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

import java.awt.*;
import java.util.Objects;

class FeatureProcessor {

    SimpleFeatureType getUpdatedFeatureType(FeatureType featureType) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        typeBuilder.setName(featureType.getName());
        typeBuilder.setCRS(featureType.getCoordinateReferenceSystem());

        for (AttributeDescriptor attributeDescriptor : ((SimpleFeatureType) featureType).getAttributeDescriptors()) {
            typeBuilder.add(attributeDescriptor);
        }
        typeBuilder.add("min", Double.class);
        typeBuilder.add("max", Double.class);
        typeBuilder.add("average", Double.class);
        typeBuilder.add("fill", String.class);

        return typeBuilder.buildFeatureType();
    }

    Feature getUpdatedFeature(Feature feature, FeatureType featureType, NDVI ndvi) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType) featureType);

        for (Property property : feature.getProperties()) {
            builder.set(property.getName(), property.getValue());
        }
        builder.set("min", ndvi.getMin());
        builder.set("max", ndvi.getMax());
        builder.set("average", ndvi.getAvg());
        String hex = Integer.toHexString(Objects.requireNonNull(getColorFotNDVI(ndvi.getAvg())).getRGB() & 0xffffff);
        hex = hex.length() < 6 ? "0" + hex : hex;
        builder.set("fill", "#" + hex);

        return builder.buildFeature(((SimpleFeature) feature).getID());
    }

    private Color getColorFotNDVI(double ndvi) {
        if (ndvi <= 1.0 && ndvi >= 0.9) {
            return new Color(4, 18, 4);
        } else if (ndvi < 0.9 && ndvi >= 0.8) {
            return new Color(4, 38, 4);
        } else if (ndvi < 0.8 && ndvi >= 0.7) {
            return new Color(4, 58, 4);
        } else if (ndvi < 0.7 && ndvi >= 0.6) {
            return new Color(4, 74, 4);
        } else if (ndvi < 0.6 && ndvi >= 0.5) {
            return new Color(4, 98, 4);
        } else if (ndvi < 0.5 && ndvi >= 0.45) {
            return new Color(28, 114, 4);
        } else if (ndvi < 0.45 && ndvi >= 0.4) {
            return new Color(60, 134, 4);
        } else if (ndvi < 0.4 && ndvi >= 0.35) {
            return new Color(68, 142, 4);
        } else if (ndvi < 0.35 && ndvi >= 0.3) {
            return new Color(92, 154, 4);
        } else if (ndvi < 0.3 && ndvi >= 0.25) {
            return new Color(116, 170, 4);
        } else if (ndvi < 0.25 && ndvi >= 0.2) {
            return new Color(148, 182, 20);
        } else if (ndvi < 0.2 && ndvi >= 0.166) {
            return new Color(132, 162, 44);
        } else if (ndvi < 0.166 && ndvi >= 0.133) {
            return new Color(148, 144, 60);
        } else if (ndvi < 0.133 && ndvi >= 0.1) {
            return new Color(164, 130, 76);
        } else if (ndvi < 0.1 && ndvi >= 0.066) {
            return new Color(172, 144, 92);
        } else if (ndvi < 0.066 && ndvi >= 0.033) {
            return new Color(204, 190, 172);
        } else if (ndvi < 0.033 && ndvi >= 0.0) {
            return new Color(252, 254, 252);
        } else if (ndvi < 0 && ndvi >= -1) {
            return new Color(4, 18, 60);
        } else {
            return null;
        }
    }
}
