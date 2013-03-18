package com.mycompany.servlet;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    	//SyncHolder q = new SyncHolder();
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
		Runnable p = new Producer(q,imgs,currentdir, out);
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
		out.flush();
        out.println("</body>");
        out.println("</html>");
        out.close();	
	}

}
