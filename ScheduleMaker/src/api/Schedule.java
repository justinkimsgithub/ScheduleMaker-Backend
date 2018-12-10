package api;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import models.Session;

public class Schedule implements Iterable<Session>, Cloneable{
	public static final int MON = 0;
	public static final int TUE = 1;
	public static final int WED = 2;
	public static final int THU = 3;
	public static final int FRI = 4;
	
	private ArrayList<SessionGroup> sessionGroupList;
	private DefaultUndirectedGraph<Session, DefaultEdge> graph;
	
	/**
	 * Description: Constructor for Schedule
	 */
	public Schedule() {
		sessionGroupList = new ArrayList<SessionGroup>();
		graph = null;
	}
	
	/**
	 * Description: Deep copy
	 */
	public Object clone() throws CloneNotSupportedException {
		// Assign the shallow copy to a new reference variable
		Schedule s = (Schedule)super.clone();
		
		// Assign a new ArrayList object to s
		s.sessionGroupList = new ArrayList<SessionGroup>();
		// Copy contents
		for (SessionGroup sessionGroup : this.sessionGroupList) {
			s.sessionGroupList.add(sessionGroup);
		}
		
		// Set graph to null
		s.graph = null;
		
		return s;
	}
	
	/**
	 * Description: Create a SessionGroup in the schedule
	 * @param SessionGroup a SessionGroup object
	 * @return the index of the SessionGroup just created
	 */
	public int createSessionGroup(SessionGroup SessionGroup) {
		sessionGroupList.add(SessionGroup);
		return sessionGroupList.size() - 1;
	}
	
	// Create a SessionGroup
	// Return the index
	/**
	 * Description: Create a SessionGroup in the schedule
	 * @param courseName Course name
	 * @param sessionType Session type
	 * @return the index of the SessionGroup just created
	 */
	public int createSessionGroup(String courseId, String courseName, String sessionType) {
		return this.createSessionGroup(new SessionGroup(courseId, courseName, sessionType));
	}
	
	/**
	 * Description: get SessionGroup course name by its index
	 * @param index the index of the SessionGroup
	 * @return the course name of this SessionGroup
	 */
	public String getSessionGroupCourseName(int index) {
		return sessionGroupList.get(index).getCourseName();
	}
	
	/**
	 * Description: get SessionGroup session type by its index
	 * @param index the index of the SessionGroup
	 * @return the session type of this SessionGroup
	 */
	public String getSessionGroupSessionType(int index) {
		return sessionGroupList.get(index).getSessionType();
	}
	
	/**
	 * Description: Remove a SessionGroup by the index
	 * @param index the index of the SessionGroup to remove
	 */
	public void removeSessionGroup(int index) {
		sessionGroupList.remove(index);
	}
	
	/**
	 * Description: Get SessionGroup index by course name and session type
	 * @param courseName Course name
	 * @param sessionType Session type
	 * @return the index of the SessionGroup. Return -1 if SessionGroup not found
	 */
	public int getSessionGroupID(String courseName, String sessionType) {
		for (int i = 0; i < sessionGroupList.size(); i++) {
			if (sessionGroupList.get(i).getCourseName().equals(courseName) && 
				sessionGroupList.get(i).getSessionType().equals(sessionType)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Description: Return the number of SessionGroups in this schedule
	 * @return The number of SessionGroups in this schedule
	 */
		public int size() {
			return sessionGroupList.size();
		}
	
	/**
	 * Description: Add a session to a SessionGroup
	 * @param sessionGroupId The index of the SessionGroup to which the session will be added
	 * @param sessionID The session ID. e.g. "29645R"
	 * @param startTime Start time of the session. In format "hh:mm", 24-hour time system
	 * @param endTime End time of the session. In format "hh:mm", 24-hour time system
	 * @param onDay A boolean array of size 5 indicating which day in a week this session is taking place. 
	 * @param location The location of this session
	 * @throws DateTimeParseException
	 */
	public void addSession(int sessionGroupId, String instructor, String sessionID, String startTime, String endTime, 
						   boolean [] onDay, String location, boolean isTimeTBA) 
				throws DateTimeParseException {
		sessionGroupList.get(sessionGroupId).addSession(sessionID, instructor, startTime, endTime, onDay, location, isTimeTBA);
	}
	
	/**
	 * Description: Add a session to a SessionGroup
	 * @param sessionGroupId The index of the SessionGroup to which the session will be added
	 * @param sessionID The session ID. e.g. "29645R"
	 * @param startTime Start time of the session. A java.time.Instant object
	 * @param endTime End time of the session. A java.time.Instant object
	 * @param onDay A boolean array of size 5 indicating which day in a week this session is taking place. 
	 * @param location The location of this session
	 * @throws DateTimeParseException
	 */
	public void addSession(int sessionGroupId, String instructor, String sessionID, Instant startTime, Instant endTime, 
						   boolean [] onDay, String location, boolean isTimeTBA) 
				throws DateTimeParseException {
		sessionGroupList.get(sessionGroupId).addSession(sessionID, instructor, startTime, endTime, onDay, location, isTimeTBA);
	}
	
	/**
	 * Description: Iterator for Schedule. Will traverse all SessionGroup and return only ONE session in each SessionGroup
	 */
	public Iterator<Session> iterator() {
		Iterator<Session> it = new Iterator<Session> () {
			private int currentIndex = 0;
			
			public boolean hasNext() {
				return currentIndex < sessionGroupList.size() && sessionGroupList.get(currentIndex) != null;
			}
			
			public Session next() {
				// Only return the first session in each 
				return sessionGroupList.get(currentIndex++).getSession(0);
			}
			
			public void remove() {
                throw new UnsupportedOperationException();
            }
		};
		return it;
	}
	
	
	// Store all session conflicts by creating a graph
	private void createGraph() {
		// Initialize graph
		graph = new DefaultUndirectedGraph<>(DefaultEdge.class);
		
		// Add vertices
		// Traverse every SessoinGroup and add every Session as a vertex to the graph
		for (int i = 0; i < sessionGroupList.size(); i++) {
			for (int j = 0; j < sessionGroupList.get(i).size(); j++) {
				Session session = sessionGroupList.get(i).getSession(j);
				graph.addVertex(session);
			}
		}
		
		// Check for conflicts and add edges
		for (int i = 0; i < sessionGroupList.size(); i++) {
			for (int j = 0; j < sessionGroupList.get(i).size(); j++) {
				Session session1 = sessionGroupList.get(i).getSession(j);
				// For each session, check with all sessions in following SessionGroup
				for (int p = i+1; p < sessionGroupList.size(); p++) {
					for (int q = 0; q < sessionGroupList.get(p).size(); q++) {
						Session session2 = sessionGroupList.get(p).getSession(q);
						// If have time conflict, add an edge
						if (Session.isConflict(session1, session2)) {
							graph.addEdge(session1, session2);
						}
					}
				}
			}
		}
	}
		
	private void backtracking(ArrayList<Schedule> schedules, Schedule candidate, Set<Session> neighborSet, int index) {
		// Base case
		if (index >= sessionGroupList.size()) {
			// Make a deep copy and add candidate to the schedules
			try {
				Schedule candidate_copy = (Schedule)candidate.clone();
				schedules.add(candidate_copy);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return;
		}
		
		// Traverse each session in the current SessionGroup specified by index
		SessionGroup sessionGroup = sessionGroupList.get(index);
		for (int j = 0; j < sessionGroup.size(); j++) {
			Session session = sessionGroup.getSession(j);
			// Check if this session is in conflict with all previous sessions in candidate
			// If in conflict, move on to next session
			if ((neighborSet != null) && neighborSet.contains(session)) {
				continue;
			}
			// If not in conflict, add to candidate
			//  create a new session group with same signature but containing only this session 
			candidate.createSessionGroup(sessionGroup.getCourseId(), sessionGroup.getCourseName(), sessionGroup.getSessionType());
			candidate.addSession(index, session.getSessionID(), session.getSessionInstructor(), session.getStartTime(), 
								 session.getEndTime(), session.getOnDay(), session.getLocation(), session.isTimeTBA());
			// Obtain the neighbor set of this session and generate the neighbor set for next recursive step
			Set<Session> sessionNeighborSet = Graphs.neighborSetOf(graph, session);
			
			// If current neighborSet is not null, take the union of current neighborSet and sessionNeighborSet
			if (neighborSet != null) {
				sessionNeighborSet.addAll(neighborSet);
			}
			
			// Proceed to next SessionGroup
			backtracking(schedules, candidate, sessionNeighborSet, index + 1);
			
			// After return, remove the the SessionGroup at this level for further backtracking
			candidate.removeSessionGroup(index);
		}
	}
		
	// Return an arraylist of all non-conflicting schedules
	private ArrayList<Schedule> scheduleAll() {
		// Initialize graph
		createGraph();
		
		ArrayList<Schedule> schedules = new ArrayList<Schedule>();
		Schedule candidate = new Schedule();
		// Backtracking algorithm
		backtracking(schedules, candidate, null, 0);
		
		return schedules;
	}
	
	/**
	 * Description: The scheduling algorithm. Generate all non-conflicting schedules
	 * @return return an arraylist of Schedule objects
	 */
	public ArrayList<Schedule> generateAllSchedules() {
		return scheduleAll();
	}
	
	/**
	 * Description: The scheduling algorithm. Generate the first few highest weight non-conflicting schedules
	 * @param num an integer specifying how many schedules to return
	 * @return return an arraylist of Schedule objects
	 */
	public ArrayList<Schedule> generateSomeSchedules(int num) {
		ArrayList<Schedule> schedules = scheduleAll();
		if (num < schedules.size()) {
			schedules.subList(num, schedules.size()).clear();
		}
		return schedules;	
	}
	
}

class SessionGroup{
	private ArrayList<Session> sessionList;
	private String courseId;
	private String courseName;
	private String sessionType;
	
	public SessionGroup(String courseId, String courseName, String sessionType) {
		sessionList = new ArrayList<Session>();
		this.courseId = courseId;
		this.courseName = courseName;
		this.sessionType = sessionType;
	}
	
	public void addSession(Session session) throws DateTimeParseException{
		if (session != null) {
			sessionList.add(session);
		}
	}
	
	public void addSession(String sessionID, String instructor, String startTime, String endTime, boolean [] onDay, String location, boolean isTimeTBA) 
		throws DateTimeParseException{
		// startTime and endTime format: hh:mm
		if ((sessionID == null) || (startTime == null) || (endTime == null) || (onDay == null) 
				|| (onDay.length < 5)|| (location == null)) {
			throw new IllegalArgumentException("Arguments to method SessionGroup.createSession() is/are invalid");
		}
		this.addSession(new Session(courseId, courseName, instructor, sessionType, sessionID, startTime, endTime, onDay, location, isTimeTBA));
	}
	
	public void addSession(String sessionID, String instructor, Instant startTime, Instant endTime, boolean [] onDay, String location, boolean isTimeTBA) 
			throws DateTimeParseException{
			// startTime and endTime format: hh:mm
		if ((sessionID == null) || (startTime == null) || (endTime == null) || (onDay == null) 
				|| (onDay.length < 5)|| (location == null)) {
			throw new IllegalArgumentException("Arguments to method SessionGroup.createSession() is/are invalid");
		}
		this.addSession(new Session(courseId, courseName, instructor, sessionType, sessionID, startTime, endTime, onDay, location, isTimeTBA));
	}
	
	public String getCourseId() {
		return courseId;
	}
	
	public String getCourseName() {
		return courseName;
	}
	
	public String getSessionType() {
		return sessionType;
	}
	
	// get session index by its ID
	public int getSessionIndex(String SessionID) {
		for (int i = 0; i < sessionList.size(); i++) {
			if (sessionList.get(i).getSessionID().equals(SessionID)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Cannot find session: " + SessionID + 
					" in SessionGroup with course name: " + courseName + " and session type: " + sessionType);
	}
	
	// get session by its index
	public Session getSession(int index) {
		return sessionList.get(index);
	}
	
	// Return the size of this group
	public int size() {
		return sessionList.size();
	}
}

