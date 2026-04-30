# Final Project

* Authors: Kaleb VanderSys, Zach Johnston
* Class: CS410 Section #001
* Semester: Spring 2026

## Overview

This is a command line grade manager for users on a locally hosted database.

## Reflection

Kaleb:
An interesting project. I primarily worked on the backend Java code, and I was surprised at how complicated it got. Some of 
the SQL queries got very complicated. I am grateful we could use AI as it helped with the documentation and SQL queries for 
our specific methods. I also used it to generate tests. I learned how to write more complex SQL queries and how to connect 
to a database via preferred statement. Also feel more confident in writing command line applications.

Zach:

I didn't use AI I made the ER model, schema, the connection to the database. I framed out the database class 
and worked on bug fixing once I finally got the connection working. I spent unreasonably long trying to get the connector
to work. As always with programming I get the point that you should read things carefully stressed particularly because it
me an hour to notice the URL targeting a specific schema. It makes sense in hindsight. If I had to do it again I wish we
started earlier because it was a mad dash at the end due to the both of us being busy up until a day before the due date.
We managed to get a lot done, but some of our mistakes when adding data really stressed that it is very useful to have a way
to delete data.

## Compiling and Using
To compile:
```javac *.java```

To run: 
```java GradeManager```

## Testing

Mostly just user testing, but one Junit test file, GradeManagerTest
