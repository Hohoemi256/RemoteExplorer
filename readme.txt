With this program it is basically possible to perform sth. like an airdroid webserver and communication between the server and the client is only via a regular browser.
To adress the web server let it run by executing the program and hit launch. Afterwards in your browser type the corresponding local IP or even IP4/6 with the port that was set, by default 8080.
An example would be to type "192.168.1.112:8080" to adress the webserver on the PC with the IP 192.168.1.112 and runs on port 8080.
Various settings like port and KWS folder can be adjusted in the settings file.

This program is desinged to work with the html,css,javascript parts designed in the KWS folder.
The script which is transferred and present in the KWS folder is designed in the "workspace JS - KWS" folder.

DELETING ITEMS
When deleting stuff via the browser, the files will not really be deleted, but put on the roots in the folder deleted with their original paths.
This is due to the fact, that it is not possible to move stuff to the trash bin, but only instantly delete it from the disk which I want to avoid to risk accidential deletion (especially maybe due to 3rd Party fiddeling on your machine).
E.g. you delete something on "C:/myFiles/pictures/Butterfly.png", then you will find the item at "C:/TRASH/C:/myFiles/pictures/Butterfly.png".