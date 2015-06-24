package com.schmidtdesigns.shiftez.models;

import java.util.ArrayList;

/**
 * A Store in the spinners for stores. Has a name and contains departments.
 * Created by braden on 15-06-23.
 */
public class Store {
    private String name;
    private ArrayList<String> deps;

    public Store(String name, ArrayList<String> deps) {
        this.name = name;
        this.deps = deps;
    }

    public ArrayList<String> getDeps() {
        return deps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Store{" +
                "name='" + name + '\'' +
                ", deps=" + deps +
                '}';
    }
}
