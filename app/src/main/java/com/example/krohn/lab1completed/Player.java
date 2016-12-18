package com.example.krohn.lab1completed;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ani Thomas on 12/11/2016.
 */
public class Player implements Serializable{
    public int id, turnNumber;
    public String name;
    public ArrayList<Card> hand;
    public ArrayList<String> scores;

    Player(int id, String name){
        this.id = id;
        this.name = name;
        hand = new ArrayList<>();
        scores = new ArrayList<>();
        scores.add(name);
    }

    Player(int id, String name, int turnNumber){
        this.id = id;
        this.name = name;
        this.turnNumber = turnNumber;
        hand = new ArrayList<>();
        scores = new ArrayList<>();
    }
}
