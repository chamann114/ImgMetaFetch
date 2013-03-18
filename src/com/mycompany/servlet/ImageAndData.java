package com.mycompany.servlet;

import java.awt.image.BufferedImage;

class ImageAndData {
	private String url;
	private BufferedImage img;
	private int height;
	private int width;
	private int color_depth;
	private boolean alpha;
	private String mime;
	private int file_size;
	
	public ImageAndData() {
	}
	
	public void set_url(String u) {
		this.url = u;
	}
	public String get_url() {
		return url;
	}
	
	public void set_img(BufferedImage i) {
		this.img = i;;
	}
	public BufferedImage get_img() {
		return img;
	}
	
	public void set_height(int h) {
		this.height = h;
	}
	public int get_height() {
		return height;
	}
	
	public void set_width(int w) {
		this.width = w;
	}
	public int get_width() {
		return width;
	}
	
	public void set_cd(int cd) {
		this.color_depth = cd;
	}
	public int get_cd() {
		return color_depth;
	}
	
	public void set_alpha(boolean a) {
		this.alpha = a;
	}
	public boolean get_alpha() {
		return alpha;
	}
	
	public void set_mime(String m) {
		this.mime = m;
	}
	public String get_mime() {
		return mime;
	}
	
	public void set_size(int s) {
		this.file_size = s;
	}
	public int get_size() {
		return file_size;
	}
	
	public String get_meta_data() {
		String s = "'File url: " + url +"&#13";
		s+= "Image width: " + width + "&#13";
		s+= "Image height: " + height + "&#13";
		s+= "Image color depth: " + color_depth + "&#13";
		s+= "Alpha channel used: " + alpha + "&#13";
		s+= "Mime type: " + mime + "&#13";
		s+= "File size: " + file_size + " bytes'";
		return s;
	}
}