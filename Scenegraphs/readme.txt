Part 1:
We ended up implementing every part of this assignment. The camera movement and rotation controls are all located within "View" in the draw function.
These were achieved by using variables within the "lookat" function to change the camera's position/point of focus/up vector.

The picture-in-picture was done by adding an additional glViewport after the
first flush and flushing that out as well. The toggling of the cameras was
done by incrementing a flag every time space is entered and detecting if that
flag is odd or even.
