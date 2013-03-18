package com.mycompany.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.FileNameMap;
import java.net.URLConnection;

import javax.imageio.ImageIO;

class Consumer implements Runnable {
	SyncQueue q;
	//SyncHolder q; 
	int num; PrintWriter out;
	Consumer(SyncQueue q,int n,PrintWriter o) {
	//Consumer(SyncHolder q,int n,PrintWriter o) {
		this.q = q;
		this.num = n;
		this.out = o;
	}
	public void set_img_meta(ImageAndData ind) {
		BufferedImage bimg = ind.get_img();
		ind.set_width(bimg.getWidth());
		ind.set_height(bimg.getHeight());
		ind.set_cd(bimg.getColorModel().getPixelSize());
		if(ind.get_cd() == 32) ind.set_alpha(true);
		else ind.set_alpha(false);
		String u = ind.get_url();
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		ind.set_mime(fileNameMap.getContentTypeFor(u));
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		try {
			if(u.contains(".bmp")) ImageIO.write(bimg, "bmp", tmp);
			else if(u.contains(".png")) ImageIO.write(bimg, "png", tmp);
			else if(u.contains(".gif")) ImageIO.write(bimg, "gif", tmp);
			else ImageIO.write(bimg, "jpg", tmp);
			ind.set_size(tmp.size());
			tmp.close();
		}
		catch (Exception e) {
				e.printStackTrace();
		}
	}
	public void run() {
		int i = 0;
		ImageAndData ind;
		while(i++ < num-1) {
			ind = q.get();
			set_img_meta(ind);
			String n = ind.get_url();
			String name = n.substring(n.lastIndexOf('/')+1);
			out.println("<img src=" + name + " alt="+ n + " width =304 height=228 TITLE= " + ind.get_meta_data() + ">");
			//out.flush();
		}
	}
}
