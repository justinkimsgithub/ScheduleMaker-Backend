package database;

import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import api.Schedule;
import models.User;
import models.Session;

public class Firebase {
	private final static String USERS_DB = "UsersV2";
	private final static String COURSES_DB = "CoursesV4";
	
public static void register(String email, User user) {
		
		Map<String, Object> data = new HashMap<>();
		

		try {
			Firestore db = initFirestore();

			System.out.println("Admitting " + email + " to the database");
			
			DocumentReference docRef = db.collection("UsersV2").document(email);
			ApiFuture<DocumentSnapshot> future = docRef.get();
			DocumentSnapshot docSnap = null;
			try {
				docSnap = future.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!docSnap.exists()) {
	        	data.put("name", user.getName());
	        	data.put("savedSchedules", user.getSavedSchedules());
	        	data.put("username", user.getName().get(0));
	        	data.put("email", email);
	        	
	        	ApiFuture<WriteResult> result = docRef.set(data);
				System.out.println("Update time : " + result.get().getUpdateTime());
				System.out.println(email + " is in the database" );
			}
			else  {
				System.out.println("User Already Exists");
			}

		}
    	
		catch(ExecutionException e) {
			System.out.println(e.getMessage());
		}
		catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}  		
		
    
	}

	public static List<Session> getCourses(List<String> courses) {

		Firestore db = initFirestore();
		List<Session> sessions = new LinkedList<>();

		//Define the query, feel free to pass a string in through an argument
		for(String query : courses) {
			try {


				//Define the Collection you want to search
				CollectionReference myCollection = db.collection(COURSES_DB);


				//In this case “ID” is the array name which contains the info we want to query
				Query myQuery = myCollection.whereArrayContains("ID", query);

				ApiFuture<QuerySnapshot> querySnapshot = myQuery.get();

				//Cycle through all the documents that come back
				for (DocumentSnapshot docSnap : querySnapshot.get().getDocuments()) {
					String days = docSnap.getString("Class Days");
					String start = docSnap.getString("Class Time Start");
					String end = docSnap.getString("Class Time End");
					String sessionType = docSnap.getString("Class Type");
					String instructor = docSnap.getString("Instructor");
					String location = docSnap.getString("Location");
					String section = docSnap.getString("Section Number");
					String title = docSnap.getString("Course Name");
					String id = docSnap.getId();

					boolean[] onDay;
					if(days.equals("TBA") || start.equals("TBA") || end.equals("TBA"))
						onDay = mapDays("");
					else
						onDay = mapDays(days);
					
					boolean isTimeTBA = false;
					if (start.equals("TBA") || end.equals("TBA"))
						isTimeTBA = true;
					sessions.add(new Session(id, title,instructor, sessionType, section, start, end, onDay,location, isTimeTBA));
				}
			}

			catch(ExecutionException e) {
				System.out.println(e.getMessage());
			}
			catch(InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		return sessions;
	}
	
	public static List<JsonObject> searchUsers(String currentUserEmail, String query) {
		Firestore db = initFirestore();
		List<JsonObject> users = new LinkedList<>();
		
		//Define the Collection you want to search
		CollectionReference usersCollection = db.collection(USERS_DB);
		Query myQuery = usersCollection.whereArrayContains("name", query);

		ApiFuture<QuerySnapshot> querySnapshot = myQuery.get();
		
		try {
			System.out.println(querySnapshot.get().toString());
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Cycle through all the documents that come back
		try {
			for (DocumentSnapshot docSnap : querySnapshot.get().getDocuments()) {		
				System.out.println("loop");

				JsonObject user = new JsonObject();
				String email = docSnap.getId();
				String name = query;
				
				if(currentUserEmail.equals(email)) { //don't add current user to list of returned user
					continue;
				}
				
				user.addProperty("name", name);
				user.addProperty("email", email);
				
				System.out.println(user.toString());
				
				users.add(user);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return users;
	}

	public static void saveSchedule(String email, List<String> schedule) {
		Firestore db = initFirestore();
		
		DocumentReference docRef = db.collection(USERS_DB).document(email);
		//check what happens if we give a bad email

		Gson gson = new Gson();

		System.out.println(schedule);
		// asynchronously retrieve the document
		ApiFuture<WriteResult> arrayUnion = docRef.update("savedSchedules", FieldValue.arrayUnion(gson.toJson(schedule)));


		try {
			arrayUnion.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static ArrayList<ArrayList<String>> getSavedSchedules(String email) {
		System.out.println("In get saved schedules");
		System.out.println(email);
		Firestore db = initFirestore();
		DocumentReference docRef = db.collection(USERS_DB).document(email);
		//check what happens if we give a bad email

		// asynchronously retrieve the document
		ApiFuture<DocumentSnapshot> future = docRef.get();
		// ...
		// future.get() blocks on response
		DocumentSnapshot docSnap = null;
		try {
			docSnap = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		User user;
		ArrayList<ArrayList<String>> savedSchedules = new ArrayList<>();

		if(docSnap!=null && docSnap.exists()) {
			user = docSnap.toObject(User.class);
			System.out.println(user.getSavedSchedules());
			for(String schedule : user.getSavedSchedules()) {
				ArrayList<String> thisSchedule = new ArrayList<String>();
				String[] courses = schedule.substring(1, schedule.length() -1).split(",");
				for(String courseTitle: courses) {
					courseTitle=courseTitle.substring(1, courseTitle.length() - 1);
					docRef = db.collection(COURSES_DB).document(courseTitle);
					System.out.println(courseTitle);
					future = docRef.get();
				
					try {
						docSnap = future.get();
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(docSnap!=null && docSnap.exists()) {
						String days = docSnap.getString("Class Days");
						String start = docSnap.getString("Class Time Start");
						String end = docSnap.getString("Class Time End");
						String sessionType = docSnap.getString("Class Type");
						String instructor = docSnap.getString("Instructor");
						String location = docSnap.getString("Location");
						String section = docSnap.getString("Section Number");
						String title = docSnap.getString("Course Name");
						String id = docSnap.getId();

						boolean isTimeTBA = false;
						if (start.equals("TBA") || end.equals("TBA"))
							isTimeTBA = true;
						Session thisSession = new Session(id, title,instructor, sessionType, section, start, end, mapDays(days),location, isTimeTBA);
						String sessionString = thisSession.getJsonString();
						System.out.println("new session:");
						System.out.println(sessionString);
						thisSchedule.add(sessionString);
						
					}
				}
				savedSchedules.add(thisSchedule);
			}

		} else  {
			//user not found
		}
	
		return savedSchedules;
	}

	public static List<String> getFriends(String email) {

		Firestore db = initFirestore();
		DocumentReference docRef = db.collection(USERS_DB).document(email);
		//check what happens if we give a bad email

		// asynchronously retrieve the document
		ApiFuture<DocumentSnapshot> future = docRef.get();
		// ...
		// future.get() blocks on response
		DocumentSnapshot docSnap = null;
		try {
			docSnap = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		User user;
		List<Session> savedSchedules = new LinkedList<>();

		if(docSnap!=null && docSnap.exists()) {
			user = docSnap.toObject(User.class);
			for(String courseTitle : user.getSavedSchedules()) {
				System.out.println(courseTitle);
				docRef = db.collection(COURSES_DB).document(courseTitle);
				future = docRef.get();
				try {
					docSnap = future.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(docSnap!=null && docSnap.exists()) {
					//					System.out.println(docSnap.get("savedSchedules").toString());
					//					docSnap.getData().forEach((key, value) -> System.out.println(key + ":" + value.toString()));
					String days = docSnap.getString("Class Days");
					String start = docSnap.getString("Class Time Start");
					String end = docSnap.getString("Class Time End");
					String sessionType = docSnap.getString("Class Type");
					String instructor = docSnap.getString("Instructor");
					String location = docSnap.getString("Location");
					String section = docSnap.getString("Section Number");
					String title = docSnap.getString("Course Name");
					String id = docSnap.getId();

					boolean isTimeTBA = false;
					if (start.equals("TBA") || end.equals("TBA"))
						isTimeTBA = true;
					savedSchedules.add(new Session(id, title,instructor, sessionType, section, start, end, mapDays(days),location, isTimeTBA));
				}
			}

		} else  {
			//user not found
		}

		return null;
	}

	private static Firestore initFirestore() {
		Firestore db = null;
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();

		if(firebaseApps!=null && !firebaseApps.isEmpty()){
			for(FirebaseApp app : firebaseApps){
				if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME))
					db = FirestoreClient.getFirestore();
			}
		}
		else {
			try {
				InputStream serviceAccount = new FileInputStream("final-project-firebase-adminsdk-mobr9-2f3704063b.json");
				GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(credentials)
						.build();
				FirebaseApp.initializeApp(options);

				db = FirestoreClient.getFirestore();
			}
			catch(FileNotFoundException e) {
				System.out.println(e.getMessage());
			}
			catch(IOException e) {
				System.out.println(e.getMessage());
			}
			System.out.println("Connected to firebase");
		}
		if(db==null) {
			System.out.println("DB IS NULL");
		}
		return db;
	}
	/**
	 * Maps days to a boolean array of size 7
	 * 
	 * @param schedule Days to be mapped
	 * @return boolean array representing schedule
	 */
	private static boolean[] mapDays(String schedule) {
		schedule = schedule.toLowerCase();
		boolean[] onDay = new boolean[5];
		String[] days = schedule.split("/");
		System.out.println(Arrays.toString(days));

		for(String day : days) {
			day=day.trim();
			if(day.equals("mon") || day.equals("monday"))
				onDay[0] = true;
			else if(day.equals("tue") || day.equals("tuesday"))
				onDay[1] = true;
			else if(day.equals("wed") || day.equals("wednesday"))
				onDay[2] = true;
			else if(day.equals("thu") || day.equals("thursday"))
				onDay[3] = true;
			else if(day.equals("fri") || day.equals("friday"))
				onDay[4] = true;
		}

		return onDay;
	}
	
}

