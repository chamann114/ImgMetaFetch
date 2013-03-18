package com.mycompany.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.imageio.ImageIO;

class Producer implements Runnable {
	SyncQueue q;
	//SyncHolder q; 
	String [] imgs; String dir; PrintWriter out;
	Producer(SyncQueue q,String [] i,String d, PrintWriter out) {
	//Producer(SyncHolder q,String [] i,String d, PrintWriter out) {
		this.q = q;
		this.imgs = i;
		this.dir = d;
		this.out = out;
	}

	public void run() {
		int i = 0;
		BufferedImage bimg;
		ImageAndData tmp = new ImageAndData();
		while(i < imgs.length-1) {
			try {
				URL url = new URL(imgs[i]);
				tmp.set_url(imgs[i]);
				bimg = (BufferedImage) ImageIO.read(url);
				tmp.set_img(bimg);
				String name = imgs[i].substring(imgs[i].lastIndexOf('/')+1);
				String imagename = dir + name;
			    File outputfile = new File(imagename);
			    ImageIO.write(bimg, "jpg", outputfile);
				i++;
				out.flush();
			    q.put(tmp);
			    tmp = new ImageAndData();

				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
