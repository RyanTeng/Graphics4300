1. Which parts of the design were changed to support animation, and why?
	We did not change the design very much. To support animation, because it was very trivial in this assignment, spinning propellers only needed to be rotated using a variable that could be changed every draw loop.
2. What are the limitations/compromises in your design?
	Limitations: each additional animation would have to be individual done based on its requirements (complex ones, like a swinging light wouldn't be doable with an iterated variable).
3. If another developer uses your code to animate a different scene, how would they go about it? Be specific:
which parts of the code would be changed for which purpose.
	Depending on how it would be animated, they would need to manually animate their animatable parts themselves - like in the example above. Mainly, the parts in the draw loop that iterate the 'spin' variable may need to change, or additional variables may need to be added and used in place of static values in the transformation matrices.
