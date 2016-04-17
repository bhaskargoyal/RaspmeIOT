## RaspmeIOT
An Internet of Things app for controlling all household eletrical devices.<br>
Thus, Enabling wireless mobile based control of appliances in a domestic setting.<br>

The code in this repository is only of Android and not the backend.<br>
Backend consists of  a raspberry pi running Django server, and a main server also running Django server.<br>

This can be understood using, the two models 
<ul>
  <li>Local Server Model</li>
  <img src="https://github.com/bhaskargoyal/RaspmeIOT/blob/master/localserver.png">
  <li>Big Model</li>
  <img src="https://github.com/bhaskargoyal/RaspmeIOT/blob/master/bigmodel.png">
</ul>

Their are 4 java activity files
<ul>
<li>HomeActivity.java</li>
<li>SignUpActivity.java</li>
<li>SignInActivity.java</li>
<li>MainActivity.java</li>
</ul>
and their are 4 corresponding xml files.

Java files uses REST APIs to communicate with the Django server.
