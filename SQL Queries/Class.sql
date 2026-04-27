CREATE table Class (
    ID int not null auto_increment ,
    course_num varchar(5),
    term varchar(4),
    section_num varchar(3),
    description text,
    PRIMARY KEY (`ID`)
);
