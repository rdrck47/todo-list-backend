package com.rdrck47.todolist.repository;

import com.rdrck47.todolist.model.ToDo;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class ToDoRepository {

    HashMap<Long, ToDo> toDoStorage = new HashMap<>();
    Long currentId = 0L;

    public ToDo save(ToDo entity) {
        Long entityId;
        if(entity.getId()==null){ //Creation of a new entity in the storage //if(!toDoStorage.containsKey(entity.getId())){
            entityId = currentId++;
            entity.setId(entityId);
            toDoStorage.put(entityId,entity);
            return toDoStorage.get(entityId);
        }else{ //Update operation for an entity that already exists
            entityId = entity.getId();
            if(toDoStorage.containsKey(entityId)){
                toDoStorage.put(entityId, entity);
                return toDoStorage.get(entityId);
            }else {
                throw new IllegalArgumentException("ID: " + entityId + " doesn't exist");
            }
        }

    }


    public Iterable<ToDo> saveAll(Iterable<ToDo> entities) {
        ArrayList<ToDo> temporalEntitiesStorage = new ArrayList<ToDo>();
        for (ToDo entity : entities) {
            temporalEntitiesStorage.add(this.save(entity));
        }
        return temporalEntitiesStorage;
    }


    public Optional<ToDo> findById(Long entityId) {
        return Optional.ofNullable(toDoStorage.get(entityId));
    }

    public boolean existsById(Long entityId) {
        return toDoStorage.containsKey(entityId);
    }


    public Iterable<ToDo> findAll() {
        return new ArrayList<ToDo>(toDoStorage.values());
    }


    public Iterable<ToDo> findAllById(Iterable<Long> entitiesId) {
        ArrayList<ToDo> temporalSearchStorage = new ArrayList<>();
        for (Long entityId : entitiesId) {
            if(toDoStorage.containsKey(entityId)){
                temporalSearchStorage.add(toDoStorage.get(entityId));
            }
        }
        return temporalSearchStorage;
    }


    public long count() {
        return toDoStorage.size();
    }

    public void deleteById(Long entityId) {
        toDoStorage.remove(entityId);
    }


    public void delete(ToDo entity) {
        this.deleteById(entity.getId());
    }

    public void deleteAllById(Iterable<Long> entitiesId) {
        for(Long entityId : entitiesId){
           this.deleteById(entityId);
        }
    }


    public void deleteAll(Iterable<ToDo> entities) {
        for(ToDo entity : entities){
            this.delete(entity);
        }
    }

    public void deleteAll() {
        toDoStorage.clear();
    }
}
