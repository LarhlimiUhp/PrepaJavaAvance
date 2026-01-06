package com.examen.stock.dao;

import com.examen.stock.exception.StockException;
import java.util.List;

public interface IDao<T> {
    void create(T element);

    List<T> readAll();

    T readByName(String nom) throws StockException;

    void update(T element) throws StockException;

    void delete(String nom) throws StockException;
}
