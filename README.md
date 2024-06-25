## CS 122B FabFlix

### Demo: 
1. Video URL: https://screenrec.com/share/Vtx68e7fQk

### Instructions to deploy Fablix: 

#### Deploy FabFlix (Remotely)
Git clone repository: git clone https://github.com/maitreyyi/FabFlix.git

Change directory into repo: cd cs122b-spring20-team-46

Build war file: mvn package

Copy the war file into tomcat: cp ./target/*.war /home/ubuntu/tomcat/webapps

Open Tomcat Domain at <your-amazon-instance-domain>:8080

Go to Manager Apps > Click FabFlix

You should now be on the movie list page.

#### Deploy FabFlix (Locally on Development Machine)
Git clone repository
git clone https://github.com/maitreyyi/FabFlix.git

IntelliJ Configuration
Import Project from External Model > Choose Maven

To Connect Tomcat
1. Click Add Configurations / Edit Configurations
2. Fix button should appear at bottom right screen
3. Click cs-122b-team-cs:war
4. Apply changes and click OK
5. Click Run application to build, connect server and launch Tomcat.

You are now all set up! Visit FabFlix on at http://localhost:8080/cs-122b-team-cs.

### Group Contributions
1. Worked with Sharon Leo on the project to complete all the tasks

### JMeter test values:
1. 3 workers + 2 pods: 5,248.998/minute
2. 4 workers + 3 pods: 5,290.076/minute
