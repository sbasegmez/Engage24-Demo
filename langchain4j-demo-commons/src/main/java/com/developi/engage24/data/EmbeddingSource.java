package com.developi.engage24.data;

public enum EmbeddingSource {
    PROJECTS;

    public static EmbeddingSource fromLabel(String label) {
        switch (label) {
            case "projectsmt":
                return PROJECTS;
            default:
                return null;
        }
    }

    public String getLabel() {
        switch (this) {
            case PROJECTS:
                return "projectsmt";
            default:
                return this.name();
        }
    }
}
