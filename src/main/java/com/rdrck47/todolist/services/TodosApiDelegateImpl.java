package com.rdrck47.todolist.services;

import com.rdrck47.todolist.api.TodosApiDelegate;
import com.rdrck47.todolist.model.ToDo;
import com.rdrck47.todolist.model.ToDoTemplate;
import com.rdrck47.todolist.model.ToDoValidationError;
import com.rdrck47.todolist.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@Service
public class TodosApiDelegateImpl implements TodosApiDelegate {

    @Autowired
    ToDoRepository toDoRepository;

    @Override
    public ResponseEntity<Object> createToDo(ToDoTemplate body) {
        ToDo temporalToDo = new ToDo();
        if(body.getPriority()!=null){
            try {
                temporalToDo.setPriority(ToDo.PriorityEnum.fromValue(body.getPriority().getValue()));
            }catch (IllegalArgumentException illegalPriority){
                ToDoValidationError toDoValidationError = new ToDoValidationError();
                toDoValidationError.setErrorMessage("Invalid priority argument: Unknown priority '"+body.getPriority().getValue()+"'");
                return ResponseEntity.badRequest().body(toDoValidationError);
            }
        }
        if(body.getName()!=null){
            if(!(body.getName().length() >120)){
                temporalToDo.setName(body.getName());
            }else{
                ToDoValidationError toDoValidationError = new ToDoValidationError();
                toDoValidationError.setErrorMessage("Invalid to-do name. Name should not exceed 120 characters.");
                return ResponseEntity.badRequest().build();
            }
        }

        temporalToDo.setCreationDate(LocalDate.now());
        if(body.getDueDate()!=null){
            temporalToDo.setDueDate(body.getDueDate());
        }
        Long createdToDoId = toDoRepository.save(temporalToDo).getId();
        URI location = URI.create(String.format("/todos/%o", createdToDoId));
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<Void> deleteToDoById(Long id) {
        Optional<ToDo> optionalToDo = toDoRepository.findById(id);
        if(optionalToDo.isPresent()){
            toDoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    @Override
    public ResponseEntity<List<ToDo>> getToDoByPage(Integer pageNumber, Boolean sortByPriority, Boolean sortByDueDate, String filterByDone, String filterByName, String filterByPriority) {
        List<ToDo> toDoList = (List<ToDo>) toDoRepository.findAll();
        List<ToDo> paginatedList = new ArrayList<ToDo>();
        //Filtering by name
        if(filterByName!=null){
            toDoList.removeIf(s -> !s.getName().toLowerCase().contains(filterByName.toLowerCase()));
        }
        //Filtering by done/undone

        if(filterByDone!=null){
            if(filterByDone.equals("Done")){
                toDoList.removeIf(d->!d.getDone());
            } else if (filterByDone.equals("Undone")) {
                toDoList.removeIf(d->d.getDone());
            }
        }

        //Filtering by priority
        if(filterByPriority!=null){
            toDoList.removeIf(p -> !(p.getPriority() == ToDo.PriorityEnum.fromValue(filterByPriority)));
        }

        //Sorting by dueDate
        if(sortByDueDate!=null){
            if(sortByDueDate){
                toDoList.sort(new Comparator<ToDo>() {
                    @Override
                    public int compare(ToDo o1, ToDo o2) {
                        if (o1.getDueDate() == null || o2.getDueDate() == null) {
                            return 0;
                        }
                        return o1.getDueDate().compareTo(o2.getDueDate());
                    }
                }.reversed());
            }
        }



        //Sorting by priority
        if(sortByPriority!=null){
            if(sortByPriority){
                toDoList.sort(new Comparator<ToDo>() {
                    @Override
                    public int compare(ToDo p1, ToDo p2) {
                        return Integer.compare(priority2Num(p1.getPriority()), priority2Num(p2.getPriority()));
                    }
                });
            }
        }

        //Pagination
        int to = pageNumber*10;
        int from = to - 10;
        for(int x=from;x<to;x++){
            if(x<toDoList.size()){
                paginatedList.add(toDoList.get(x));
            }
        }

        return ResponseEntity.ok(paginatedList);
    }

    private int priority2Num(ToDo.PriorityEnum priority){
        switch (priority){
            case HIGH -> {
                return 0;
            }
            case MEDIUM -> {
                return 1;
            }
            case LOW -> {
                return 2;
            }
            default -> {
                return -1;
            }
        }
    }

    @Override
    public ResponseEntity<Void> setToDoAsDone(Long id) {
        Optional<ToDo> optionalToDo = toDoRepository.findById(id);
        if(optionalToDo.isPresent()){
            ToDo persistenceToDo = optionalToDo.get();
            if(!persistenceToDo.getDone()){
                persistenceToDo.setDone(true);
                persistenceToDo.setDoneDate(LocalDate.now());
                toDoRepository.save(persistenceToDo);
            }
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> setToDoAsUndone(Long id) {
        Optional<ToDo> optionalToDo = toDoRepository.findById(id);
        if(optionalToDo.isPresent()){
            ToDo persistenceToDo = optionalToDo.get();
            if(persistenceToDo.getDone()){
                persistenceToDo.setDone(false);
                persistenceToDo.setDoneDate(null);
                toDoRepository.save(persistenceToDo);
            }
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> updateToDoById(Long id, String name, String dueDate, String priority) {
        Optional<ToDo> optionalToDo = toDoRepository.findById(id);
        if(optionalToDo.isPresent()){
            ToDo persistenceToDo = optionalToDo.get();
            if(priority!=null){
                try {
                    persistenceToDo.setPriority(ToDo.PriorityEnum.fromValue(priority));
                }catch (IllegalArgumentException illegalPriority){
                    return ResponseEntity.badRequest().build();
                }
            }
            if(name!=null){
                if(!(name.length() >120)){
                    persistenceToDo.setName(name);
                }else{
                    return ResponseEntity.badRequest().build();
                }
            }
            if(dueDate!=null){
                persistenceToDo.setDueDate(LocalDate.parse(dueDate));
            }
            toDoRepository.save(persistenceToDo);
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }


}