package com.gaspas.model;

import java.util.List;

public class Platform {
    private long id;
    private String name;
    private List<Field> fields;

    public Platform() {}

    public Platform(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Field> getFields() { return fields; }
    public void setFields(List<Field> fields) { this.fields = fields; }
}
