ILogos testing task version 1.0 03/22/2017

Program that periodically monitors the specified directory for the presence of new files of a specific XML-format.
When a file of the appropriate format is founded, the application saves its contents to the similar in structure
to the table in PostgreSQL and moves the file to a processed files folder, and which was failed to read then
moves to another folder.

Steps to run the program:
1) Unpack the archive in a convenient place for on the file system;
2) Create a database in PostgreSQL;
3) Execute the 'table.sql' script to create the required table structure;
4) Using the 'config.properties' file, configure the connection parameters for the database and the monitoring period;
5) From this directory, run a command prompt and run 'bin\xmlmonitor -c config.properties'.


<<<Supporting XML file format>>>

<Entry>
<!--string up to 1024 characters long-->
<Content> Record Content </ content>
<!--date of record creation-->
<creationDate>2014-01-01 00:00:00</creationDate>
</Entry>