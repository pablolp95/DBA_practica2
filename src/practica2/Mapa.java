/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import java.util.ArrayList;

/**
 *
 * @author pablo
 */
public class Mapa {
    private int [][] datosRadar;
    private double [][] datosScanner;
    private Position posOrigen;
    private Position posDestino;
    private double valorG;
    private double valorH;
    private Mapa padre;
    private String movimientoAnterior;
    
    public Mapa(int [][] datosRadar, double [][] datosScanner, Position posOrigen, Position posDestino){
        this.datosRadar = datosRadar;
        this.datosScanner = datosScanner;
        this.posOrigen = posOrigen;
        this.posDestino = posDestino;
        this.padre = null;
        this.movimientoAnterior = null;
    }
    
    public double getG(){
        return this.valorG;
    }
    
    public void setG(double valorG){
        this.valorG = valorG;
    }
    
    public double getH(){
        return this.valorG;
    }
    
    public void setH(double valorH){
        this.valorH = valorH;
    }
    
    public String getMovimientoAnterior(){
        return this.movimientoAnterior;
    }
    
    public void setMovimientoAnterior(String movAnterior){
        this.movimientoAnterior = movAnterior;
    }
    
    public Mapa getPadre(){
        return this.padre;
    }
    
    public void setPadre(Mapa padre){
        this.padre = padre;
    }
    
    public Position getPosicionOrigen(){
        return this.posOrigen;
    }
    
    public Position getPosicionDestino(){
        return this.posDestino;
    }
    
    public ArrayList<Mapa> generarHijos(){
        ArrayList<Mapa> hijos = new ArrayList();
        Mapa mapa;
        Position posicionHijo;
        
        //CASO A: me encuentro centrado, puedo generar todos los hijos adyacentes:
        /*  +--+---+---+---+--+
            |  |   |   |   |  |
            +--+---+---+---+--+
            |  | x | x | x |  |
            +--+---+---+---+--+
            |  | x | x | x |  |
            +--+---+---+---+--+
            |  | x | x | x |  |
            +--+---+---+---+--+
            |  |   |   |   |  |
            +--+---+---+---+--+
        */
        if(this.posOrigen.getX() >= 1 && this.posOrigen.getX() <= 3 &&
                this.posOrigen.getY() >=1 && this.posOrigen.getY() <=3){
            if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()-1] != 1){
                mapa = generarHijo(Accion.moveNW);
                hijos.add(mapa);
            }

            if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                mapa = generarHijo(Accion.moveN);
                hijos.add(mapa);
            }

            if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()+1] != 1){
                mapa = generarHijo(Accion.moveNE);
                hijos.add(mapa);
            }

            //Genero fila 2
            if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                mapa = generarHijo(Accion.moveW);
                hijos.add(mapa);
            }

            if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                mapa = generarHijo(Accion.moveE);
                hijos.add(mapa);
            }

            //Genero fila 3
            if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()-1] != 1){
                mapa = generarHijo(Accion.moveSW);
                hijos.add(mapa);
            }

            if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                mapa = generarHijo(Accion.moveS);
                hijos.add(mapa);
            }

            if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()+1] != 1){
                mapa = generarHijo(Accion.moveSE);
                hijos.add(mapa);
            }
        }
        //CASOS B y C: estoy pegado a una de las paredes laterales y centrado: puedo generar solo 5 hijos
        /*  +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            | x |  |  |  |  |       |  |  |  |  | x |
            +---+--+--+--+--+       +--+--+--+--+---+
            | x |  |  |  |  |       |  |  |  |  | x |
            +---+--+--+--+--+       +--+--+--+--+---+
            | x |  |  |  |  |       |  |  |  |  | x |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
        */
        else if(this.posOrigen.getX() >= 1 && this.posOrigen.getX() <= 3){
            //CASO B
            if(this.posOrigen.getY() == 0){
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveN);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveNE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveSE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveS);
                    hijos.add(mapa);
                }
            }
            //CASO C
            else if(this.posOrigen.getY() == 4){
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveN);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveNW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveSW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveS);
                    hijos.add(mapa);
                }
            }
        }
        //CASOS D y E: estoy pegado arriba o abajo, y centrado: puedo generar solo 5 hijos
        /*  +--+---+---+---+--+       +--+---+---+---+--+
            |  | x | x | x |  |       |  |   |   |   |  |
            +--+---+---+---+--+       +--+---+---+---+--+
            |  |   |   |   |  |       |  |   |   |   |  |
            +--+---+---+---+--+       +--+---+---+---+--+
            |  |   |   |   |  |       |  |   |   |   |  |
            +--+---+---+---+--+       +--+---+---+---+--+
            |  |   |   |   |  |       |  |   |   |   |  |
            +--+---+---+---+--+       +--+---+---+---+--+
            |  |   |   |   |  |       |  | x | x | x |  |
            +--+---+---+---+--+       +--+---+---+---+--+
        */
        else if(this.posOrigen.getY() >= 1 && this.posOrigen.getY() <= 3){
            //CASO D
            if(this.posOrigen.getX() == 0){
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveSW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveS);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveSE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveE);
                    hijos.add(mapa);
                }
                
            }
            //CASO E
            else if(this.posOrigen.getX() == 4){               
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveNW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveN);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveNE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveE);
                    hijos.add(mapa);
                }
            }
        }
        //CASOS F y G
        /*  +---+--+--+--+--+       +--+--+--+--+---+
            | x |  |  |  |  |       |  |  |  |  | x |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
        */
        else if(this.posOrigen.getX() == 0){
            //CASO F
            if(this.posOrigen.getY() == 0){
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveS);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveSE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveE);
                    hijos.add(mapa);
                }
            }
            //CASO G
            else if(this.posOrigen.getY() == 4){
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveS);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()+1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveSW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveW);
                    hijos.add(mapa);
                }
                
            }
        }
        //CASOS H e I
        /*  +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            |   |  |  |  |  |       |  |  |  |  |   |
            +---+--+--+--+--+       +--+--+--+--+---+
            | x |  |  |  |  |       |  |  |  |  | x |
            +---+--+--+--+--+       +--+--+--+--+---+
        */
        else if(this.posOrigen.getX() == 4){
            //CASO H
            if(this.posOrigen.getY() == 0){
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveN);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveNE);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()+1] != 1){
                    mapa = generarHijo(Accion.moveE);
                    hijos.add(mapa);
                }
            }
            //CASO I
            else if(this.posOrigen.getY() == 4){
                if(this.datosRadar[this.posOrigen.getX()][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()-1] != 1){
                    mapa = generarHijo(Accion.moveNW);
                    hijos.add(mapa);
                }
                
                if(this.datosRadar[this.posOrigen.getX()-1][this.posOrigen.getY()] != 1){
                    mapa = generarHijo(Accion.moveN);
                    hijos.add(mapa);
                }
            }
        }
        
        return hijos;
    }
    
    public boolean objetivoAlcanzado(){
        boolean equal = posOrigen.equals(posDestino);
        return equal;
    }
    
    public boolean equals(Mapa other){
        boolean igual;
        
        igual = this.posOrigen.equals(other.posOrigen) && this.posDestino.equals(other.posDestino);
        
        if(igual){
            for(int i = 0; i < 5; ++i){
                for(int j = 0; j < 5; ++j){
                    if(this.datosRadar[i][j] != other.datosRadar[i][j])
                        return false;
                }
            }
        }
        else{
            return false;
        }
        
        return true;
    }
    
    private void calcularG(){
        if(this.padre != null){
            this.valorG = this.padre.valorG;
        
            if(this.movimientoAnterior.equals(Accion.moveE.toString()) ||
                this.movimientoAnterior.equals(Accion.moveW.toString()) ||
                this.movimientoAnterior.equals(Accion.moveN.toString()) ||
                this.movimientoAnterior.equals(Accion.moveS.toString())){
                this.valorG += 1.0;
            }
            else{
                this.valorG += 1.414213562;
            }
        }
        else{
            this.valorG = 0.0;
        }
    }
    
    private void calcularH(){
        if(this.padre != null){
            this.valorH = this.datosScanner [this.posOrigen.getX()][this.posOrigen.getY()];
        }
        else{
            this.valorH = 0.0;
        }
    }
    
    public double calcularF(){
        return this.valorG + this.valorH;
    }
    
    private Mapa generarHijo(Accion accion){
        Mapa mapa = null;
        Position posicionHijo = null;
        
        switch(accion){
            case moveNW:
                posicionHijo = new Position(this.posOrigen.getX()-1, this.posOrigen.getY()-1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveNW.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveN:
                posicionHijo = new Position(this.posOrigen.getX()-1, this.posOrigen.getY());
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveN.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveNE:
                posicionHijo = new Position(this.posOrigen.getX()-1, this.posOrigen.getY()+1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveNE.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveW:
                posicionHijo = new Position(this.posOrigen.getX(), this.posOrigen.getY()-1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveW.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveE:
                posicionHijo = new Position(this.posOrigen.getX(), this.posOrigen.getY()+1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveE.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveSW:
                posicionHijo = new Position(this.posOrigen.getX()+1, this.posOrigen.getY()-1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveSW.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveS:
                posicionHijo = new Position(this.posOrigen.getX()+1, this.posOrigen.getY());
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveS.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
                
            case moveSE:
                posicionHijo = new Position(this.posOrigen.getX()+1, this.posOrigen.getY()+1);
                mapa = new Mapa(this.datosRadar, this.datosScanner, posicionHijo, this.posDestino);
                mapa.setPadre(this);
                mapa.movimientoAnterior = Accion.moveSE.toString();
                mapa.calcularG();
                mapa.calcularH();
                break;
            
        }
        
        return mapa;
    }
}
