/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

/**
 *
 * @author pablo
 */
public class Position {
    private int x;
    private int y;
    
    public Position(){
        
    }
    
    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public void setX(int x){
        this.x = x;
    }
    
    public int getX(){
        return this.x;
    }
    
    public void setY(int y){
        this.y = y;
    }
    
    public int getY(){
        return this.y;
    }
    
    public Double distancia(Position other){
        return Math.sqrt((this.x - other.x)*(this.x - other.x) + (this.y - other.y)*(this.y - other.y));
    }
    
    public boolean equals(Position other){
        return this.x == other.x && this.y == other.y;
    }
    
}
