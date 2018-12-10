# Schedule Optimizer Backend - ScheduleMaker

## How to run
1. Deploy war file on Server

OR (1. doesn't work)

1. Clone into Eclipse workspace
2. Right click on workspace and select import
	1. under General select Projects from Folder or Archive
		1. Select Directory
		2. Select ScheduleMaker
	2. Finish
3. Right click ScheduleMaker_ScheduleMaker (Folder with M on it)
	1. Select Run As: Run On Server
		* Select Finish if there is an existing server
		* In case it can not run on an existing Server
			1. Select Manually define a new server
			2. Under Apache, select Tomcat v9.0 Server
			3. Next
			4. Under Tomcat installation directory add Tomcat server location
				* Probably under /usr/local
			5. Finish





### POST: Register user example post request:
### http://localhost:8080/ScheduleMaker/register
#### Example post request
`{
	"email":"justinkimsemail@gmail.com",
	"user": {
		"friends":[],
		"name": "justin kim",
		"savedSchedules":[]
	}
}`

#### No response

### POST: Query possible courses:
### http://localhost:8080/ScheduleMaker/query
#### Example post request
`[
	"CSCI-104L",
	"CSCI-170L",
	"CSCI-109"
]`

#### Example response:
`[
    [
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'Aaron Cote', 'sessionType' : 'Lecture', 'sessionID' : '29903R', 'startTime' : '11:00 AM', 'endTime': '12:20 PM', 'onDay': [false, false, false, false, false], 'location': 'GFS101 '}",
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'No Information', 'sessionType' : 'Lab', 'sessionID' : '29905R', 'startTime' : '03:00 PM', 'endTime': '03:50 PM', 'onDay': [false, true, false, false, false], 'location': 'SAL109 '}",
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'No Information', 'sessionType' : 'Quiz', 'sessionID' : '30025R', 'startTime' : '08:00 PM', 'endTime': '08:50 PM', 'onDay': [false, false, false, false, true], 'location': 'TBA '}",
        "{'courseName' : 'Introduction to Computer Science', 'instructor' : 'Andrew Goodney', 'sessionType' : 'Lecture', 'sessionID' : '29901D', 'startTime' : '01:00 PM', 'endTime': '01:50 PM', 'onDay': [true, false, false, false, false], 'location': 'SGM123 '}"
    ],
    [
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'Aaron Cote', 'sessionType' : 'Lecture', 'sessionID' : '29910R', 'startTime' : '03:00 PM', 'endTime': '03:20 PM', 'onDay': [false, false, false, false, false], 'location': 'GFS101 '}",
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'No Information', 'sessionType' : 'Lab', 'sessionID' : '29932R', 'startTime' : '01:00 PM', 'endTime': '01:50 PM', 'onDay': [false, false, false, false, true], 'location': 'SAL109 '}",
        "{'courseName' : 'Data Structures and Object Oriented Design', 'instructor' : 'No Information', 'sessionType' : 'Quiz', 'sessionID' : '30025R', 'startTime' : '08:00 PM', 'endTime': '08:50 PM', 'onDay': [false, false, false, false, true], 'location': 'TBA '}",
        "{'courseName' : 'Introduction to Computer Science', 'instructor' : 'Andrew Goodney', 'sessionType' : 'Lecture', 'sessionID' : '29901D', 'startTime' : '01:00 PM', 'endTime': '01:50 PM', 'onDay': [true, false, false, false, false], 'location': 'SGM123 '}"
    ]
]`


### POST: Save Schedule:
### http://localhost:8080/ScheduleMaker/save
#### Example post request
`{
	"email":"justinkimsemail@gmail.com",
	"schedule":
	[
    	"CSCI-356 30126R",
    	"CSCI-360 29983R",
    	"CSCI-360 30031D",
    	"CSCI-380 31872D",
    	"CSCI-401 30227R"
	]
}`

#### No response

### GET: Get Saved Schedule:
### http://localhost:8080/ScheduleMaker/saved-schedules
#### Parameters 
* email - email that we want saved schedules for.

#### Example request: http://localhost:8080/ScheduleMaker/saved-schedules?email=jeffmiller@gmail.com
