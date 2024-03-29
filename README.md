# Stop Gap

![banner](https://www.yull.ca/stopgap/images/stopgapbanner1.png)

## Links:
[Michael's Website (yull.ca)](https://www.yull.ca)

[Michael's email (yull.michael14@gmail.com)](mailto:yull.michael14@gmail.com)

## Description:

Stop Gap is a java application created in order to combine multiple working directories temporarily in to one. The need initially arose while working with a node server that hosted multiple web applications.

I desired to have isolated project directories during production. In this case, I would have to duplicate the project to the node server directory when testing node functionality. I was left with copying and pasting, or multiple copies of the same project, neither of which are efficient; thus, Stop Gap was born. 

Stop Gap has the ability to take multiple directories and combine them in a host directory for the length of a desired session. While a session is active any changes made to the originals are reflected in the copies. Once a session is terminated the copies are instantly removed. Session settings can be saved for easy setup and your last configuration (if saved) will be loaded when you next use the application. 

*Knowledge Areas:*
* Java
* JavaFX
* UI Design
* File Management

## Functionality: 

Stop Gap is very simple to use. All you need to do is select a host directory then add any directories you would like copied over.

![directories](https://www.yull.ca/stopgap/images/stopgap2.png)

Directories can be typed in, or if you hit the button you will be able to select a folder from the file explorer. With the "Copy as Folder" option, you can choose whether you would like the directory to copy as a folder, or have contents added to the host folder directly. Directories can easily be removed by hitting the "X". If a configuration file has been selected any changes will automatically be saved.

![Buttons](https://www.yull.ca/stopgap/images/stopgap3.png)

The bottom bar holds the buttons for the application's base functions. Clicking the "+ Add Directory" will add another selector to the list of directories. The "Save" and "Load" buttons are used for storing and restoring configurations between uses. If a configuration has been saved, it will automatically be loaded on next use. The "Clear" button removes all settings to give the user a fresh start. Lastly, the "ACTIVATE" button freezes the controls and starts the session. Each folder is checked to be valid and if so, the folders are copied and maintained.

Any errors are easily handled and presented to the user as directly as possible. 

![Alert](https://www.yull.ca/stopgap/images/stopgap4.png)

## Thoughts:

Stop Gap has cleaned up my workflow, as intended, so I would consider it a successful project. The project also proved to be quite the learning process, especially when it came to dynamically adding new directories while the session is running. Most of my visual work is done for the web so it was pretty cool to make a standalone application. 

Cheers,

*Author: Michael Yull | 2017* 
