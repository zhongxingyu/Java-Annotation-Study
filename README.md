# Java-Annotation-Study

## What is this repository? 

Java annotations have been widely used by the community for purposes such as compiler guidance and runtime processing. Despite the
ever-growing use, there is still limited empirical knowledge about the actual usage of annotations in practice, the changes made to 
annotations during software evolution, and the potential impact of annotations on code quality. To fill this gap, we perform the first
large-scale empirical study about Java annotations on 1,094 open-source projects hosted on GitHub. Our study systematically 
investigates annotation usage, annotation evolution, and annotation impact, and generates 10 interesting findings that have important 
implications for developers, researchers, tool builders, and language or library designers. 

This repository contains our code for studying Java annotation and its evolution, the collected large scale data about evolution of
annotations in three years for each project, and our manual analysis of the characteristics of annotation evolution.  

## Structure of this repository

The repository mainly contains three folders: src, annotation-evolution-data, and Sampled-data.

### Folder src
The src folder contains the code for conducting our study. In particular, it contains two sub-folders: repositoryPrepare and annotationRetriver.
The repositoryPrepare folder contains code related for selecting large and popular projects according to stargazer counts, commit numbers,
and contributor numbers, which finally results in the 1,094 projects used in this study. For the 1,094 projects specified in file
"download-repository.sh", the code in the annotationRetriver folder downloads these projects to local machine, retrives the evolution 
history for these projects, and finally extracts different kinds of annotation change behaviors. The entry class is AnnotationEvolutionGenerator 
and will generate a csv file that conatins the evolution of annotations in the past three years for each project. 

### Folder annotation-evolution-data
The annotation-evolution-data folder conatins the generated data about evolution of annotations for three years. For each project, there is 
is a csv file named "info_for_all_commits_use" which has 22 columns. Some columns contain annotation unrelated info. The column "pName" represents the name of the project, "ID" means the 
commit number, "PE" means whether we have encountered parse error when we used Spoon (https://github.com/INRIA/spoon) to parse the file, 
"bType1" and "bType2" mean whether the commit is a bug-fixing commit (we use two different sets of error-related key words to estimate 
whether the commit is a bug-fixing commit, "bType1" is the result of using less words and "bType2" is the result of using more words, we 
finally adop "bType2" as we find it more accurate), "cName" means the name of the changed class, "cType" means the change type of the changed
class ("M" stands for modification, "A" stands for addition, and "D" stands for deletion), "Size" means the lines of code (LOC) for the
changed class, "nbCC" means code churn for the changed class (code lines added, modified, or deleted), "aName" means the author name for
the commit, and finally "Date" means the date for the commit. 

Other columns contain info about annotaitons. "nbUse" means the number of annotations in the changed class, "nbOver" means the number of 
@Override annotation in the changed class, "nbCS" means the number of special annotations (@Override,@SuppressWarnings, @Deprecated, @SafeVarargs, 
@FunctionalInterface) that are changed in the commit, "nbUseAE" means the number of adding annotation changes for which the annotated program 
elements already exist in the previous file version, "nbUseAN" means the number of adding annotation changes for which the annotated program elements 
do not exist in the previous file version, "nbUseDE" means the number of deleting annotation changes for which the annotated program elements are not
deleted, "nbUseDN" means the number of deleting annotation changes for which the annotated program elements are also deleted, "nbUseC" means the number
of annotation replacement changes, "nbUseU" means the number of annotation value update changes, "nbUpAnRe" means the number of annotation changes for
which the annotated program elements are updated or moved, and finally "nbAnUnEd" means the number of code line changes that are 
unrelated with annotations.

### Folder Sampled-data
The Sampled-data folder contains our manual analysis results. Basically, it contains two kinds of reults. First, we separate code independent 
annotation changes (annotation changes that are not related with other code changes) form code consistent annotation changes (annotation
changes that are related with other code changes). Code independent annotation changes are are likely to reflect more directly developers’ concerns 
over the annotations (missing annotations or problems with existing annotations). Change instances correspond to "nbUseAE", "nbUseDE", "nbUseC",
and "nbUseU" are likely to be code independent annotation changesare, and we further use some heuristics to eliminate potential code consistent 
annotation changes among them. After that, we sample 384 instances for each of them and study the characteristics of them, such as the reason
behind the changes and what in detail are the changes. The results are summaried in csv files ANNOTATION-ADD-SAMPLE, ANNOTATION-CHANGE-SAMPLE,
ANNOTATION-DELETE-SAMPLE, and ANNOTATION-UPDATE-SAMPLE. These files contain columns like "Repository Name", "Commit Link", "File Name",
"Involved Annotation", "Reason", "Behavior", and "Remark". 

Second, we find that nearly three-quarter of the assigned annotation values are typed as
String, and the String content could actually be better typed (for instance, as Class or Primitive) in some cases. We doubt types of some 
annotation members are wrongly designed as String. We sample some String type annotation members for which the assigned annotation values match
with Primitive and class name, and manually check them. We find that there are truly many wrongly designed String types. The results are
summarized in csv files String-Type-Annotation-Member-Check (Assigned Value Matches with Class Name) and String-Type-Annotation-Member-Check
(Assigned Value Matches with Primitive). These files contain columns like "Repository Name", "Annotation Name", "Annotation Member Name", 
"Assigned Value (in Our Sample)", and "Wrongly Designed as String Type?".


