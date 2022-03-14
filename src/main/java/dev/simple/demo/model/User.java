package dev.simple.demo.model;


import lombok.Data;

@Data
public class User {
    private int id;
    private String name;
    private String surname;
    private int age;

    public User(){}

    public User(int id, String name, String surname, int age){
        this.name = name;
        this.id = id;
        this.age = age;
        this.surname = surname;
    }
}
