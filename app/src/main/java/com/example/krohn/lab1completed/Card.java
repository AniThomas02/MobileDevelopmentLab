package com.example.krohn.lab1completed;

import java.io.Serializable;

/**
 * Created by Ani Thomas on 12/11/2016.
 */
public class Card implements Serializable {
    public int id, value;
    public String type;

    public Card(int id) {
        this.id = id;
        getType();
        getValue();
    }

    private void getType(){
        if(id < 13){
            type = "clubs";
        }else if (id < 26){
            type = "diamonds";
        }else if (id < 39){
            type = "hearts";
        }else if (id < 52){
            type = "spades";
        }
    }

    private void getValue(){
        value = (id + 1) % 13;
        if(value == 1){
            value = 14;
        }
    }
}
