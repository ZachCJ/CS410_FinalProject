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
    CONSTRAINT Assignment_ibfk_1 FOREIGN KEY (Category_ID) REFERENCES Category (ID),
    PRIMARY KEY (name, Category_ID)
);

CREATE TABLE ClassHasCategory (
    Class_ID int not null,
    Category_ID int not null,
    weight decimal,
    CONSTRAINT ClassHasCategory_ibfk_1 FOREIGN KEY (Class_ID) REFERENCES Class (ID),
    CONSTRAINT ClassHasCategory_ibfk_2 FOREIGN KEY (Category_ID) REFERENCES Category (ID),
    PRIMARY KEY (Class_ID, Category_ID)
);