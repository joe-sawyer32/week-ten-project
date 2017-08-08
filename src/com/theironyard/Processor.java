package com.theironyard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.EMPTY_SET;

/**
 * Created by Joe on 7/27/17.
 */
public class Processor {

    private final String WORK_ORDER_DIRECTORY = "./work-orders/";
    private static Map<Status, Set<WorkOrder>> workOrders = new LinkedHashMap<>();
    private static Set<Status> workFlow = new LinkedHashSet<>();
    private final long sleepTime = 10000L;


    public static void main(String args[]) {
        initializeMapAndWorkFlow();
        Processor processor = new Processor();
        processor.processWorkOrders();
    }

    private static void initializeMapAndWorkFlow() {
        workOrders.put(Status.INITIAL, EMPTY_SET);
        workOrders.put(Status.ASSIGNED, EMPTY_SET);
        workOrders.put(Status.IN_PROGRESS, EMPTY_SET);
        workOrders.put(Status.DONE, EMPTY_SET);

        workFlow.add(Status.IN_PROGRESS);
        workFlow.add(Status.ASSIGNED);
        workFlow.add(Status.INITIAL);
    }

    public void processWorkOrders() {
        while (true) {
            printWorkOrders();
            moveWorkOrders();
            printWorkOrders();
            readWorkOrders();
            sleep();
        }
    }

    private void printWorkOrders() {
        System.out.println("Current work orders:");
        for (Status status : workOrders.keySet()) {
            System.out.println(status);
            System.out.println(workOrders.get(status));
        }
    }

    private void moveWorkOrders() {
        // move work orders in map from one state to another
        System.out.println("Moving work orders...");
        for (Status status : workFlow) {
            Set<WorkOrder> pile = workOrders.get(status);
            moveToNextPile(pile, status);
        }
    }

    private void moveToNextPile(Set<WorkOrder> pile, Status status) {
        Set<WorkOrder> nextPile;
        switch (status) {
            case IN_PROGRESS:
                for (WorkOrder order : pile) {
                    order.setStatus(Status.DONE);
                }
                nextPile = workOrders.get(Status.DONE);
                if(nextPile.isEmpty()) {
                    workOrders.replace(Status.DONE, pile);
                } else {
                    nextPile.addAll(pile);
                    workOrders.replace(Status.DONE, nextPile);
                }
                break;
            case ASSIGNED:
                for (WorkOrder order : pile) {
                    order.setStatus(Status.IN_PROGRESS);
                }
                nextPile = workOrders.get(Status.IN_PROGRESS);
                if(nextPile.isEmpty()) {
                    workOrders.replace(Status.IN_PROGRESS, pile);
                } else {
                    nextPile.addAll(pile);
                    workOrders.replace(Status.IN_PROGRESS, nextPile);
                }
                break;
            case INITIAL:
                for (WorkOrder order : pile) {
                    order.setStatus(Status.ASSIGNED);
                }
                nextPile = workOrders.get(Status.ASSIGNED);
                if(nextPile.isEmpty()) {
                    workOrders.replace(Status.ASSIGNED, pile);
                } else {
                    nextPile.addAll(pile);
                    workOrders.replace(Status.ASSIGNED, nextPile);
                }
                break;
        }
        workOrders.replace(status, EMPTY_SET);
    }

    private void readWorkOrders() {
        // read the json files into WorkOrders and put in map
        System.out.println("Searching for new work orders...");
        ObjectMapper mapper = new ObjectMapper();
        File workOrderDirectory = new File(WORK_ORDER_DIRECTORY);
        File files[] = workOrderDirectory.listFiles();
        if (files.length != 0) {
            Set<WorkOrder> initialWorkOrders = new HashSet<>();
            for (File f : files) {
                if (f.getName().endsWith(".json")) {
                    // f is a reference to a json file
                    WorkOrder newWorkOrder = convertJsonFileToObject(f, mapper);
                    // f.delete(); will delete the file
                    f.delete();
                    System.out.println("Found new work order:");
                    System.out.println(newWorkOrder);
                    initialWorkOrders.add(newWorkOrder);
                }
            }
            workOrders.put(Status.INITIAL, initialWorkOrders);
        } else {
            System.out.println("No new work orders found");
        }
    }

    private static WorkOrder convertJsonFileToObject(File json, ObjectMapper mapper) {
        WorkOrder wo = new WorkOrder();
        try {
            wo = mapper.readValue(json, WorkOrder.class);
        } catch (JsonProcessingException ex) {
            System.out.println("\nUnable to convert from JSON");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("\nUnsuccessful read from file");
            ex.printStackTrace();
        }
        return wo;
    }

    private void sleep() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Couldn't fall asleep....");
            ex.printStackTrace();
        }
    }
}
