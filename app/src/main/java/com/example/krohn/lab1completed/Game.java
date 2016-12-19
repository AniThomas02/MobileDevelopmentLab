package com.example.krohn.lab1completed;

import java.io.Serializable;

/**
 * Created by Ani Thomas on 12/13/2016.
 */
public class Game implements Serializable {
    public int id, status, playerTurn;

    public Game(int id, int status, int playerTurn){
        this.id = id;
        this.status = status;
        this.playerTurn = playerTurn;
    }
}
