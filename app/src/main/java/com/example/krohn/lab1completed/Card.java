package com.example.krohn.lab1completed;

import java.io.Serializable;

/**
 * Created by Ani Thomas on 12/11/2016.
 */
public class Card implements Serializable {
    int id, value;
    String type;

    public Card(int id, int value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }
}
