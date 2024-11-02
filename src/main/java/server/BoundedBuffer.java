package server;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class BoundedBuffer<T> {
 	private T[] arr;
 	private int size; // elements in array;

 	private ReentrantLock lock;
 	private Condition notEmpty;
 	private Condition notFull;

 	public BoundedBuffer(int _size) {
 		arr = (T[]) new Object[size];
 		this.size = 0;
 		lock = new ReentrantLock();
 		notEmpty = lock.newCondition();
 		notFull = lock.newCondition();
 	}

 	public void push(T item) throws InterruptedException {
 		try {
 			lock.lock();

 			while (size >= arr.length) { // full
 				notFull.await();
 			}

 			arr[size] = item;
 			size++;

 			notEmpty.signal();
 		} finally {
 			lock.unlock();
 		}
 	}

 	public T pop() throws InterruptedException {
 		T item = null;
 		try {
 			lock.lock();

 			while (size < 1) { // empty
 				notEmpty.await();
 			}

 			size--;
 			item = arr[size];

 			notFull.signal();
 		} finally {
 			lock.unlock();
 		}
 		return item;
 	}
 }