# Meglofriend's Stop Gap

## Stop Gap
![banner](https://www.meglobot.com/blogimages/stopgapbanner1.png)

## Description:

Stop Gap is a java application created in-order to combine multiple working directories temporarily in to one. The need initially arose while working with a node server that hosted multiple web applications.

I found the desire to have isolated project directories during production. In this case I would have to duplicate the project to the node server directory when testing node functionality. I was left with copying and pasting or multiple copies of the same project, neither of which are efficient thus Stop Gap was born. 

Stop Gap has to ability to take multiple directories and combine them on to a host for the length of a desired session. While a session is active any changes made to the originals are reflected on the copies. Once a session is terminated the copies are instantly removed. Session settings can be saved for easy setup and your last configuration (if saved) will be loaded when you next use the application. 

*Author: Michael Yull | 2017* 