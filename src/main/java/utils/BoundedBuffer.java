package utils;

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

 	public BoundedBuffer(int size) {
		 arr = (T[]) new Object[size];
		 this.size = 0;
		 lock = new ReentrantLock();
		 notEmpty = lock.newCondition();
		 notFull = lock.newCondition();
 	}

 	public void push(T item) throws InterruptedException {
		 System.out.println("Push do item: " + item);
		lock.lock();
 		try {
 			while (size >= arr.length) { // full
				System.out.println("Buffer full do push");
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
			System.out.println("Pop do item: " + item);

 			notFull.signal();
 		} finally {
 			lock.unlock();
 		}
 		return item;
 	}
 }