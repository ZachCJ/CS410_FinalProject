CREATE table Class (
    ID int not null auto_increment ,
    course_num varchar(5),
    term varchar(4),
    section_num varchar(3),
    description text,
    PRIMARY KEY (`ID`)
);

CREATE table Category (
    ID int not null auto_increment,
    name varchar (30),
    PRIMARY KEY (ID)
);

CREATE TABLE Student (
    ID int not null auto_increment,
    name varchar(45),
    username varchar(60),
    PRIMARY KEY (ID)
);

CREATE TABLE Assignment (
    name varchar(60) not null,
    description text,
    point_value decimal,
    Category_ID int not null,
    Class_ID int not null,
    CONSTRAINT Assignment_ibfk_1 FOREIGN KEY (Category_ID) REFERENCES Category (ID),
    CONSTRAINT Assignment_ibfk_2 FOREIGN KEY (Class_ID) REFERENCES Class (ID),
    PRIMARY KEY (name, Class_ID) -- Gives the Constraint the a class cannot have two assignments of the same name.
);

CREATE TABLE ClassHasCategory (
    Class_ID int not null,
    Category_ID int not null,
    weight decimal,
    CONSTRAINT ClassHasCategory_ibfk_1 FOREIGN KEY (Class_ID) REFERENCES Class (ID),
    CONSTRAINT ClassHasCategory_ibfk_2 FOREIGN KEY (Category_ID) REFERENCES Category (ID),
    PRIMARY KEY (Class_ID, Category_ID)
);

CREATE TABLE Enrolled (
    Class_ID int not null,
    Student_ID int not null,
    CONSTRAINT Enrolled_ibfk_1 FOREIGN KEY (Class_ID) REFERENCES Class (ID),
    CONSTRAINT Enrolled_ibfk_2 FOREIGN KEY (Student_ID) REFERENCES Student (ID),
    PRIMARY KEY (Class_ID, Student_ID)
);

CREATE TABLE Assigned (
    Student_ID int not null,
    Assignment_name varchar(60) not null,
    Class_ID int not null,
    grade decimal,
    CONSTRAINT Assignment_ibfk_1 FOREIGN KEY (Class_ID) REFERENCES Assignment (Class_ID),
    CONSTRAINT Assignment_ibfk_2 FOREIGN KEY (Student_ID) REFERENCES Student (ID),
    CONSTRAINT Assignment_ibfk_3 FOREIGN KEY (Assignment_name) REFERENCES Assignment (name),
    PRIMARY KEY (Student_ID, Assignment_name, Class_ID)
);