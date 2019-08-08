package com.example.test;

public class Person {

    private String id;
    private String name;
    private int startIndex;
    private int endIndex;

    public Person() {
    }

    public Person(String id, String name, int startIndex, int endIndex) {
        this.id = id;
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Person) {
            Person person = (Person) obj;
            if (person.id.equals(id)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

}
