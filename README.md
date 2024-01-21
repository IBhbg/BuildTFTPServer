# Submission by Ivan Huusein
To compile all the Java files in the project, use the following command in the terminal:
'''
find . -name "*.java" > sources.txt && javac @sources.txt
'''

## Run:
To run the server, use the following command in the terminal:
'''
java server TFTPPORT READ_From_Folder
'''

## Example:
'''
java server 4970 /home/t-rex/Skrivbord/
'''

This will start the server on port 4970 and it will serve files from the /home/t-rex/Skrivbord/ directory.
