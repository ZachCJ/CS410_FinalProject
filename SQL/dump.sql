-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: final
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Assigned`
--

DROP TABLE IF EXISTS `Assigned`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Assigned` (
  `Student_ID` int NOT NULL,
  `Assignment_ID` int NOT NULL,
  `grade` decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (`Student_ID`,`Assignment_ID`),
  KEY `Assigned_ibfk_1` (`Assignment_ID`),
  CONSTRAINT `Assigned_ibfk_1` FOREIGN KEY (`Assignment_ID`) REFERENCES `Assignment` (`ID`),
  CONSTRAINT `Assigned_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Student` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Assigned`
--

LOCK TABLES `Assigned` WRITE;
/*!40000 ALTER TABLE `Assigned` DISABLE KEYS */;
INSERT INTO `Assigned` VALUES (1013,4,92),(1013,5,100),(1013,6,100),(124325,1,30),(124325,2,80),(124325,3,90),(4789324,7,15),(4789324,9,30),(4789324,10,14),(4789324,11,39),(4789324,12,75),(6413654,1,500),(6413654,4,149),(6413654,5,10),(6413654,6,5),(6413654,7,20),(6413654,9,30),(6413654,11,40),(6413654,12,80),(7589430,9,30),(7589430,11,40),(7589430,12,80),(54903782,1,501),(54903782,2,99),(54903782,3,100);
/*!40000 ALTER TABLE `Assigned` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Assignment`
--

DROP TABLE IF EXISTS `Assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Assignment` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(60) NOT NULL,
  `description` text,
  `point_value` decimal(10,0) DEFAULT NULL,
  `Category_ID` int NOT NULL,
  `Class_ID` int NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `name` (`name`,`Class_ID`),
  KEY `Assignment_ibfk_1` (`Category_ID`),
  KEY `Assignment_ibfk_2` (`Class_ID`),
  CONSTRAINT `Assignment_ibfk_1` FOREIGN KEY (`Category_ID`) REFERENCES `Category` (`ID`),
  CONSTRAINT `Assignment_ibfk_2` FOREIGN KEY (`Class_ID`) REFERENCES `Class` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Assignment`
--

LOCK TABLES `Assignment` WRITE;
/*!40000 ALTER TABLE `Assignment` DISABLE KEYS */;
INSERT INTO `Assignment` VALUES (1,'The Big Exam','Literally the entire class',1000,3,2),(2,'Exam 1','First',100,1,1),(3,'Exam 2','Second',100,1,1),(4,'ThisProject','DB project',150,4,3),(5,'OnePlusOne','yup math',100,6,4),(6,'OneMinusOne','more math',100,7,4),(7,'A1','Something',20,8,5),(8,'OnePlusOnePlusOne','more math',100,6,4),(9,'A2','Something',30,8,5),(10,'A3','Something',15,8,5),(11,'P1','Something',40,10,5),(12,'E1','Something',80,9,5);
/*!40000 ALTER TABLE `Assignment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Category`
--

DROP TABLE IF EXISTS `Category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Category` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Category`
--

LOCK TABLES `Category` WRITE;
/*!40000 ALTER TABLE `Category` DISABLE KEYS */;
INSERT INTO `Category` VALUES (1,'Exams'),(2,'Homework'),(3,'Exam'),(4,'Projects'),(5,'Projects'),(6,'Add'),(7,'Minus'),(8,'Assignment'),(9,'Exams'),(10,'Projects');
/*!40000 ALTER TABLE `Category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Class`
--

DROP TABLE IF EXISTS `Class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Class` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `course_num` varchar(10) DEFAULT NULL,
  `term` varchar(6) DEFAULT NULL,
  `section_num` varchar(3) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Class`
--

LOCK TABLES `Class` WRITE;
/*!40000 ALTER TABLE `Class` DISABLE KEYS */;
INSERT INTO `Class` VALUES (1,'408','SP26','1','WebDev'),(2,'CS404','SP25','1','YOU SHALL NOT PASS'),(3,'CS410','SP26','1','Databases'),(4,'MATH123','SP26','1','Really Basic Math'),(5,'ECE330','FALL24','1','Microprocessors');
/*!40000 ALTER TABLE `Class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ClassHasCategory`
--

DROP TABLE IF EXISTS `ClassHasCategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ClassHasCategory` (
  `Class_ID` int NOT NULL,
  `Category_ID` int NOT NULL,
  `weight` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`Class_ID`,`Category_ID`),
  KEY `ClassHasCategory_ibfk_2` (`Category_ID`),
  CONSTRAINT `ClassHasCategory_ibfk_1` FOREIGN KEY (`Class_ID`) REFERENCES `Class` (`ID`),
  CONSTRAINT `ClassHasCategory_ibfk_2` FOREIGN KEY (`Category_ID`) REFERENCES `Category` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ClassHasCategory`
--

LOCK TABLES `ClassHasCategory` WRITE;
/*!40000 ALTER TABLE `ClassHasCategory` DISABLE KEYS */;
INSERT INTO `ClassHasCategory` VALUES (1,1,1.00),(1,2,0.00),(2,3,1.00),(3,4,0.00),(3,5,1.00),(4,6,0.50),(4,7,0.50),(5,8,0.40),(5,9,0.20),(5,10,0.40);
/*!40000 ALTER TABLE `ClassHasCategory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Enrolled`
--

DROP TABLE IF EXISTS `Enrolled`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Enrolled` (
  `Class_ID` int NOT NULL,
  `Student_ID` int NOT NULL,
  PRIMARY KEY (`Class_ID`,`Student_ID`),
  KEY `Enrolled_ibfk_2` (`Student_ID`),
  CONSTRAINT `Enrolled_ibfk_1` FOREIGN KEY (`Class_ID`) REFERENCES `Class` (`ID`),
  CONSTRAINT `Enrolled_ibfk_2` FOREIGN KEY (`Student_ID`) REFERENCES `Student` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Enrolled`
--

LOCK TABLES `Enrolled` WRITE;
/*!40000 ALTER TABLE `Enrolled` DISABLE KEYS */;
INSERT INTO `Enrolled` VALUES (3,1013),(4,1013),(1,124325),(2,124325),(2,4789324),(5,4789324),(2,6413654),(3,6413654),(4,6413654),(5,6413654),(3,7589430),(5,7589430),(1,54903782),(2,54903782);
/*!40000 ALTER TABLE `Enrolled` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Student`
--

DROP TABLE IF EXISTS `Student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Student` (
  `ID` int NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `username` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Student`
--

LOCK TABLES `Student` WRITE;
/*!40000 ALTER TABLE `Student` DISABLE KEYS */;
INSERT INTO `Student` VALUES (1013,'Bill Nye','billynye'),(124325,'Billy Bob','BillyBob'),(4789324,'Eric Lance','EricLance'),(6413654,'Joe Joe','JoeJoe'),(7589430,'Neil Tyson','NeildeGrasseTyson'),(54903782,'Sarah Shell','SarahShell');
/*!40000 ALTER TABLE `Student` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-29 22:57:37
