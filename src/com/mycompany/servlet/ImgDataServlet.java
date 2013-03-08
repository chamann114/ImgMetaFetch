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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


class Counter {
	private int count = 0;
	public int inc() {
		synchronized(this) {
			return count++;
		}
	}
	public int get_count() {
		synchronized(this) {
			return count;
		}
	}
}



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
		s+= "File size: " + file_size + "'";
		return s;
	}
}


/**
 * Servlet implementation class GreetingServlet
 */
@WebServlet({ "/GreetingServlet", "/hello" })
public class GreetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Counter c = new Counter();
	String [] imgs = new String [10];
	int curr_size = 10;
	int img_count = 0;
	int num_prod;
	int num_cons;
	LinkedList <ImageAndData> id_list = new LinkedList<ImageAndData>();
	Lock lock = new ReentrantLock();
	PrintWriter out;
	int counter = 0;
	String currentdir;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GreetingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    class Get_Img implements Runnable {
		int id;
		String [] a;
		int a_size;
		public Get_Img(int i,String [] arr, int size) {
			id = i;
			a = arr;
			a_size = size;
		}
		public void run() {
			BufferedImage bimg;
			ImageAndData tmp = new ImageAndData();
			
			for(int i = 0; i < a_size; i++) {
				try {
					URL url = new URL(a[i]);
					tmp.set_url(a[i]);
					bimg = (BufferedImage) ImageIO.read(url);
					tmp.set_img(bimg);
					
					lock.lock();
					try{
						id_list.add(tmp);
					}
					finally {
						lock.unlock();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

    class Get_Meta implements Runnable {
		int id;
		
		public Get_Meta(int i) {
			id = i;
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
			ImageAndData in = new ImageAndData();
			while(c.get_count() < img_count) {
				boolean removed = false;
				lock.lock();
				try{
					if(!id_list.isEmpty()) {
						in = id_list.removeFirst();
						removed = true;
						//System.out.println("Meta ID: " + id + "</p> ");
						c.inc();
					}
					else {
						removed = false;
					}
				}
				finally {
					lock.unlock(); 
					if(removed == true) {
						set_img_meta(in);
						String n = in.get_url();
						String name = n.substring(n.lastIndexOf('/')+1);
						String imagename = currentdir + name;
					    File outputfile = new File(imagename);
					    try{
					    	ImageIO.write(in.get_img(), "jpg", outputfile);
					    }
					    catch(Exception e) {
					    	e.printStackTrace();
					    }
					    String os ="Image";
						out.println("<img src=" + name + " alt="+ os + " width =304 height=228 TITLE= " + in.get_meta_data() + ">");
					}
				}
			}
		}
	}
    
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
    
    
    public void setup(String url) {
        currentdir = getServletContext().getRealPath("/");
        
        
    	String source = getUrlSource(url);
        parseSource(source);
        
        int num_per_prod = (int)Math.ceil((double)img_count/num_prod);
		out.println("Number of pictures: " + img_count);
		out.println("Max number of pictures per producer: " + num_per_prod + "<br");
		
		//Create arrays for runnables and threads
		Runnable [] r_img_gets = new Runnable[num_prod];
		Runnable [] r_meta_gets = new Runnable[num_cons];
		Thread [] t_img_gets = new Thread[num_prod];
		Thread [] t_meta_gets = new Thread[num_cons];
		
		//Set arrays for runnable and threads
		int start = 0;
		int end = num_per_prod;
		for(int i = 0; i < num_prod; i++) {
			String [] tmp = Arrays.copyOfRange(imgs, start, end);
			r_img_gets[i] = new Get_Img(i,tmp,end-start);
			t_img_gets[i] = new Thread(r_img_gets[i]);
			
			start = end;
			if(i == num_prod-2) end = img_count;
			else end += num_per_prod;
		}
		for(int i = 0; i < num_cons; i++) {
			r_meta_gets[i] = new Get_Meta(i);
			t_meta_gets[i] = new Thread(r_meta_gets[i]);
		}
		//Start threads
		for(int i = 0; i < num_prod; i++) {
			t_img_gets[i].start();
		}
		for(int i = 0; i < num_cons; i++) {
			t_meta_gets[i].start();
		}
		try {
			//Join threads
			for(int i = 0; i < num_prod; i++) {
				t_img_gets[i].join();
			}
			
			
			
			for(int i = 0; i < num_cons; i++) {
				t_meta_gets[i].join();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
        }
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
        out = response.getWriter();
        String url = request.getParameter("webUrl").toString();
        num_prod = Integer.parseInt(request.getParameter("num_prod").toString());
        num_cons = Integer.parseInt(request.getParameter("num_cons").toString());

        
        
        out.println("<html>");
		out.println("<head>");
        out.println("<title>Image Fetch</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>Image Fetch from " + url + "</p>");
        out.println("<p>With " + num_prod + " producers and " + num_cons + " consumers</p>");    
        setup(url);
       // deleteFile("saved313.jpg");
        out.println("</body>");
        out.println("</html>");
        out.close();
	}

}
