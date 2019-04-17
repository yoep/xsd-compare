package com.compare.xsd.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Observable wrapper which allows observers to be subscribed before an observable value is registered.
 *
 * @param <T> Set the observable value type.
 */
public class ObservableWrapper<T extends Observable> {
    private final List<Observer> observers = new ArrayList<>();

    private T observable;

    /**
     * Get the observable value.
     *
     * @return Returns the observable.
     */
    public T get() {
        return this.observable;
    }

    /**
     * Set the observable and trigger all observers.
     *
     * @param observable Set the observable value.
     */
    public void set(T observable) {
        this.observable = observable;
        this.observers.forEach(e -> {
            this.observable.addObserver(e);
            e.update(this.observable, null);
        });
    }

    /**
     * Add a new observer to the observable value.
     *
     * @param observer Set the observer to add.
     */
    public synchronized void addObserver(Observer observer) {
        observers.add(observer);

        if (this.observable != null) {
            this.observable.addObserver(observer);
        }
    }
}
