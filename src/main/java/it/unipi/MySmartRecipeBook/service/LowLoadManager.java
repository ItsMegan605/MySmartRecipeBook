package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.event.TaskToDo;
import it.unipi.MySmartRecipeBook.model.enums.Task;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class LowLoadManager {

    /* Tipo di coda che è utile per evitare problemi di sincronizzazione con i thread */
    private final Queue<TaskToDo> taskQueue = new ConcurrentLinkedQueue<>();
    private final RecipeMongoRepository recipeMongoRepository;
    private final ChefRepository chefRepository;

    public LowLoadManager(RecipeMongoRepository recipeMongoRepository, ChefRepository chefRepository) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.chefRepository = chefRepository;
    }

    public void addTask (Task.TaskType type, String recipeId, String chefId){
        TaskToDo task = new TaskToDo(type, recipeId,chefId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    public void addTask (Task.TaskType type, InfoToDeleteDTO infoToDelete){
        TaskToDo task = new TaskToDo(type, infoToDelete);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    public void addTask (Task.TaskType type, String recipeId){
        TaskToDo task = new TaskToDo(type, recipeId);
        taskQueue.add(task);
        System.out.println("Task succesfully added to the queue");
    }

    @Scheduled(fixedDelay = 10000)
    public void taskHandler(){
        if(taskQueue.isEmpty()){
            System.out.println("No task in the queue");
            return;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpuLoad = osBean.getCpuLoad();

        if(cpuLoad < 0.3){

            Integer processedTasks = 0;
            while(processedTasks < 10 && !taskQueue.isEmpty()){
                TaskToDo task = taskQueue.poll();
                executeTask(task);
                processedTasks++;
            }
        }
    }

    private void executeTask(TaskToDo task){

        switch (task.getType()){
            case SET_COUNTERS_FOODIE_DELETE:
                decrementSavesCounters(task);
                break;

            case SET_COUNTERS_ADD_FAVOURITE:
                break;

            case SET_COUNTERS_REMOVE_FAVOURITE:
                break;

            case CREATE_RECIPE_NEO4J:
                break;

            case DELETE_CHEF_RECIPE:
                break;

            default:
                System.out.println("Invalid task type");
        }
    }


    private void decrementSavesCounters(TaskToDo task){

        List<String> recipesId = task.getInfoToDelete().getRecipeIds();
        Map<String, Long> chefDecrements = task.getInfoToDelete().getChefDecrements();

        for(String recipeId : recipesId){
            recipeMongoRepository.updateSavesCounter(recipeId, -1);
        }

        for (Map.Entry<String, Long> element : chefDecrements.entrySet()) {
            String chefId = element.getKey();
            int savesToRemove = element.getValue().intValue();

            chefRepository.updateTotalSaves(chefId, -savesToRemove);
        }
    }
}
