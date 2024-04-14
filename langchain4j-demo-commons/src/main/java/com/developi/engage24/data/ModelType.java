package com.developi.engage24.data;

public enum ModelType {
    LOCAL_MINILM("Local - MiniLm", 384),
    CLOUD_OPENAI("Cloud - OpenAI", 768);

    private final String label;
    private final int dimension;

    ModelType(String label, int dimension) {
        this.label = label;
        this.dimension = dimension;
    }

    public static ModelType fromLabel(String label) {
        for (ModelType type : ModelType.values()) {
            if (type.getLabel()
                    .equals(label)) {
                return type;
            }
        }

        return null;
    }

    public String getLabel() {
        return this.label;
    }

    public int getDimension() {
        return this.dimension;
    }
}
