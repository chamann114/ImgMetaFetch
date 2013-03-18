package com.mycompany.servlet;


class SyncHolder {
	ImageAndData ind;
	boolean valueSet = false;
	public SyncHolder() {
	}
	synchronized ImageAndData get() {
		if(!valueSet)
		try {
			wait();
		} 
		catch(InterruptedException e) {
	        System.out.println("InterruptedException caught");
	    }
		valueSet = false;
		notify();
		return ind;
	}
	synchronized void put(ImageAndData ind) {
		if(valueSet)
		try {
			wait();
		}
		catch(InterruptedException e) {
	        System.out.println("InterruptedException caught");
	    }
		this.ind = ind;
		valueSet = true;
		notify();
	}	
}