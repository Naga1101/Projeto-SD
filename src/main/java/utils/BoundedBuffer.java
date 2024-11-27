package utils;

import java.io.Closeable;
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
				//System.out.println("Buffer full no push: " + size);
 				notFull.await();
 			}

			//System.out.println("Push efetuado: " + size);
 			arr[size] = item;
 			size++;

 			notEmpty.signal();
			//System.out.println("Sinal de não empty enviado");
 		} finally {
 			lock.unlock();
 		}
 	}

 	public T pop() throws InterruptedException {
 		T item = null;
 		try {
 			lock.lock();

 			while (size < 1) { // empty
				//System.out.println("Wait no pop por estar empty: " + size);
 				notEmpty.await();
 			}

			//System.out.println("Buffer já não está empty: " + size);
 			size--;
 			item = arr[size];
			//System.out.println("Pop do item: " + item);

 			notFull.signal();
 		} finally {
 			lock.unlock();
 		}
 		return item;
 	}

 	public int size() {
		 lock.lock();
		 try{
			return size;
		 } finally {
			 lock.unlock();
		 }
	}
 }