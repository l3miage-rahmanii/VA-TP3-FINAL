package fr.uga.l3miage.spring.tp3.exceptions.rest;

public class NotFoundSessionEntityRestExeption extends RuntimeException{
    public NotFoundSessionEntityRestExeption(String message) {
        super(message);
    }
}
