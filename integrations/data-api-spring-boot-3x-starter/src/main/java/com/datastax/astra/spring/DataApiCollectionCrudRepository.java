package com.datastax.astra.spring;

import com.datastax.astra.client.collections.Collection;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public abstract class DataApiCollectionCrudRepository<RECORD, T> implements CrudRepository<RECORD, T> {

    Collection<RECORD> dataAPICollection;

    public Collection<RECORD> getCollection() {
        return dataAPICollection;
    }

    @Override
    public <S extends RECORD> S save(S entity) {
        return null;
    }

    @Override
    public <S extends RECORD> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<RECORD> findById(T t) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(T t) {
        return false;
    }

    @Override
    public Iterable<RECORD> findAll() {
        return null;
    }

    @Override
    public Iterable<RECORD> findAllById(Iterable<T> ts) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(T t) {

    }

    @Override
    public void delete(RECORD entity) {
        // TODO: Implement delete logic
    }

    @Override
    public void deleteAllById(Iterable<? extends T> ts) {
        // TODO: Implement deleteAllById logic
    }

    @Override
    public void deleteAll(Iterable<? extends RECORD> entities) {
    }

    @Override
    public void deleteAll() {
        getCollection().deleteAll();
    }
}
