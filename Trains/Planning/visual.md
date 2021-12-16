# Visual Plan

The visualization program takes in a ```Map``` object and creates a drawing. 

We assume that objects drawn to the screen later will show up on top of previously drawn objects.

It should first draw a black background with the ```Map```'s specified dimensions in pixels.

It should then draw all the given ```Connection```s as lines between the ```posn```s stored within the two ```Place```s 
of the ```Connection```'s ```destination``` field. 
The lines should be drawn in the ```color``` specified in the ```Connection```, several pixels thick.
Each line should be divided into a number of segments equal to the ```Connection```'s specified ```length```.
The segments should be equal in length and separated by small gaps of several pixels.


It should finally draw a small red circle (radius of approximately 10 pixels) for each ```Place``` at its 
given ```posn``` along with its given ```name``` written in white. 