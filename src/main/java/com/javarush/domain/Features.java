package com.javarush.domain;

import static java.util.Objects.isNull;

public enum Features {
    TRAILERS("Trailers"),
    COMMENTARIES("Commentaries"),
    DELETED_SCENES("Deleted_Scenes"),
    BEHIND_THE_SCENES("Behind_the_Scenes");
    private final String value;

    Features(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Features getFeaturesByValue(String value) {
        if (isNull(value) || value.isEmpty()) {
            return null;
        }
        Features[] feature = Features.values();
        for (Features features : feature) {
            if (features.value.equals(value)) {
                return features;
            }
        }
        return null;
    }
}