package com.mycompany.servlet;

import java.util.LinkedList;

class SyncQueue {
	LinkedList <ImageAndData> l = new LinkedList<ImageAndData>();
	
	synchronized ImageAndData get() {
		if(l.size() == 0)
			try {
				wait();
			} 
			catch(InterruptedException e) {
				System.out.println("InterruptedException caught");
			}
		ImageAndData ret = l.pop();
		return ret;
	}

	synchronized void put(ImageAndData n) {
		this.l.add(n);
		notify();
		}
}