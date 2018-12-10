package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.lang.reflect.Type;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

import database.Firebase;
import models.Session;
import models.User;

/**
 * Servlet implementation class Save
 */
@WebServlet("/save")
public class Save extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Save() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String pathInfo = request.getPathInfo();

		if(pathInfo == null || pathInfo.equals("/")){

			StringBuilder buffer = new StringBuilder();
		    BufferedReader reader = request.getReader();
		    String line;
		    while ((line = reader.readLine()) != null) {
		        buffer.append(line);
		    }
		    
		    String payload = buffer.toString();
		    System.out.println(payload);
		    Gson gson = new Gson();
		    JsonReader jr = new JsonReader(new StringReader(payload.trim())); 
		    jr.setLenient(true); 
		    
		    JsonObject body = gson.fromJson(jr, JsonObject.class);
		    
		    String email = gson.fromJson(body.get("email"), String.class);
		    Type scheduleType = new TypeToken<List<String>>() {}.getType();    

		    List<String> schedule = gson.fromJson(body.get("schedule"), scheduleType);
		    
		    Firebase.saveSchedule(email, schedule);
		    
		}
		else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}

}
