package com.mycompany.servlet;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	
	public void print(PrintWriter out) {
		out.println("<p>File url: " + url + "<br>");
		out.println("Image width: " + width + "<br>");
		out.println("Image height: " + height + "<br>");
		out.println("Image color depth: " + color_depth + "<br>");
		out.println("Alpha channel used: " + alpha + "<br>");
		out.println("Mime type: " + mime + "<br>");
		out.println("File size: " + file_size + "</p>");
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

class Producer implements Runnable {
	SyncQueue q; String [] imgs; String dir;
	Producer(SyncQueue q,String [] i,String d) {
		this.q = q;
		this.imgs = i;
		this.dir = d;
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
			    q.put(tmp);
				i++;
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class Consumer implements Runnable {
	SyncQueue q; int num; PrintWriter out;
	Consumer(SyncQueue q,int n,PrintWriter o) {
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
			out.flush();
		}
	}
}


/**
 * Servlet implementation class ImageMetaFetch
 */
@WebServlet({"/test","/img","/ImageMetaFetch"})
public class ImageMetaFetch extends HttpServlet {
	private static final long serialVersionUID = 1L;
    PrintWriter out;  
    String [] imgs;
	int curr_size;
	int img_count;
	String currentdir;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageMetaFetch() {
        super();
    }
    //Dynamically resize array
    public void grow_arr() {
		curr_size += 10;
		String [] tmp = new String[curr_size];
		for(int i = 0; i < img_count; i++) {
		  tmp[i] = imgs[i];
		}
		imgs = tmp;
    }
    public String getUrlSource(String url) {
	    try {
            URL u = new URL(url);
            URLConnection urlcon = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder a = new StringBuilder();
            while (((inputLine = in.readLine()) != null)) {
				  a.append(inputLine);
			}
            in.close();
            return a.toString();
			}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "-1";
    }
    public void parseSource(String s) {
    	Pattern pattern = Pattern.compile("http://[^\\s]*");
		Matcher matcher = pattern.matcher(s);
		for (int begin = 0; matcher.find(begin); begin = matcher.end()) {
			String img = matcher.group(0);
			//Remove all but img links
			if(img.contains(".jpg") || img.contains(".bmp") || img.contains("png") || img.contains(".gif")) {
				if(img_count + 1 > curr_size) grow_arr();
				imgs[img_count++] = img.substring(0,img.indexOf("\""));
				}
		}
		
		//Make unique array of image urls
		Set<String> urls = new LinkedHashSet<String>(Arrays.asList(imgs));
		String[] unique_imgs = urls.toArray(new String[urls.size()-1]);
		img_count = urls.size()-1;
		imgs = unique_imgs;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
        out = response.getWriter();
        
        imgs = new String [10];
    	curr_size = 10;
    	img_count = 0;
    	
    	String currentdir = getServletContext().getRealPath("/");
        SyncQueue q = new SyncQueue();
        
        String tmp_url = request.getParameter("webUrl").toString();
        String url;
        if(tmp_url.equals(""))
        	url = "http://flickr.com/explore";
        else url = tmp_url;
        
        out.println("<html>");
		out.println("<head>");
        out.println("<title>Image Meta Fetch Results</title>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<form action='test' method='POST'> " +
        		"Web URL: <input type='text' name='webUrl' size='30'><br><br> " +
        		"<input type='submit' value='Submit'></form>");
        
        out.println("<p>Image Fetch from: " + url + "</p><br>");
       
        
        //Get and parse source
        String source = getUrlSource(url);
        parseSource(source);
    	
        if(img_count-1 < 0)
        	out.println("<FONT COLOR='FF0000 '>Requested Page either does not exist or does not have any images.</FONT><br>");
        else
        	out.println("<p>Number of pictures: " + (img_count-1) + "</p><br>");
        out.flush();
        
        //Create start and join producer and consumer threads
		Runnable p = new Producer(q,imgs,currentdir);
		Runnable c = new Consumer(q,imgs.length-1,out);
		Thread t_p = new Thread(p);
		Thread t_c = new Thread(c);
		t_p.start();
		t_c.start();
		try {
			t_p.join();
			t_c.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		
		}
		
        out.println("</body>");
        out.println("</html>");
        out.close();	
	}

}
