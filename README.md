# WebServer
A small Java application to access your PCs file system from any end terminal using a simple browser.
I designed it with the application Airdroid in mind, available for Android, but this time the other way around, to access PC files on my smartphone (and other PCs).
To understand more about server communication and HTTP traffic, as well as HTML, JavaScript and CSS, this project has been developed from scratch; Frontend as well as backend.
Communication is done via Ajax, so after the initial UI is sent as a webpage, every new content will be directly inquired to the server and the UI updated accordingly on the fly.

## Install:
1. Clone repository or download the precompiled jar file
2. Make sure that you have the “KWS” folder at the same directory of your jar file or src folder
3. Run the jar file or compile the src code
4. In the GUI choose a port or leave it at 80 (default)
5. Afterwards in your browser type the corresponding local IP or even IP4/6 with the port that was set. E.g. "192.168.1.112:8080" when server is running on port is 8080
6. Various settings like default port and KWS folder location can be adjusted in the settings.properties file

## Features:
You are able to browse the complete file directory of your PC and all hard drives as long as you have the necessary permissions.

You can move, copy, cut, paste files inside the browser window and the effects commands will be immediately translated and executed by the java server to the hosts file system. Delete is also possible and will move files to the trash bin if possible. If not will create a "Trash" folder at your jar location and move the files there. This is to prevent accidentally deleting important files.

Uploads as well as downloads of files is possible. Download of whole folders as zip files is also possible.

Usage of standard keys and combinations like DEL, CTRL+C, CTRL+V, CTRL+X.

## ToDo:
Enable sorting by various properties like size, file type…

Add a select all button (especially for mobile users 
