Origami Rendering module
---

Transforms a figure into a set of deformed (or non-planar) one sided polygons that represent the
each side of the faces.

There is a representation for the flat version of the figure and another for the 3D version.
The polygons for the flat rendered figure are flat or planar (not deformed) while some of the polygons
of the 3D rendered figure might be deformed.

This output of one sided polygons is very low level with the purpose of needing very little processing in the
javascript Front End that I'm using at the moment. However it can't be said that
the choice of data structure in the output of the module is tightly coupled to any
particular library or technology and, in fact, some further processing is done in the front end to create the final
data structures.

Having one sided polygons for either the flat representation (with actual one-sided concave polygons that don't
necessarily match the polygons at the other side) or for full two sided faces in the 3D representation (that are two
one-sided faces in the sense that they may have different colors) is generic enough that it serves as a valid interface
for any kind of display module.

This module also calculates the intermediate fold and translation to the center "snapshots".

Important:
Bear in mind that the purpose of making slightly deformed faces is to simply separate the vertices and faces enough
so that the eye will recognize there are two faces or two vertices that are not joined together. For that reason,
and assuming that the rest of 3D-aspect algorithms don't attempt to open the figure too much (and even if they do, but
there is no reason to believe they ever will, unless it's a totally different algorithm and not just one to give it
a 3D look) it is, in general, a minimal amount of vertex shifting that needs to be done, as I said before, for
the eye to pick on it.

It is possible that attempting to deform the faces on a particular area (like a corner) compounds with another
deformation of another face and they intersect each other in an area not supposed to be deformed. The fix for this
is fortunately simple, just restrict the area where the deformation happens, add a couple of coplanar extra points
around the shifted, non-coplanar point.

Loosen vs Rendering module
---

It may be confusing to attempt some tweaks in this module to make the figure more three dimensional.
It should be explained that the loosening module produces a valid figure with integral structure (bundles,
relative face position, no duplication of vertices, straight faces, etc).

While this module does some tweaks that have to necessarily be final, since it breaks the strucute of the figure to
just make a bunch of faces that cannot be further reasoned upon to keep opening it for a more realistic look.

Deformed faces in the 3D rendering:
---

The algorithm to create a three dimensional rendering may produce deformed faces (obviously intentionally).

An example is the corner pulling algorithm. The opened corner is not on the same plane as the rest of the vertices.

Corner pulling is done when the there are two free edges in a face, the common point is then slightly pulled outwards.

Another deformation is done with a free edge that is adjacent to two connected edges, in that case the free edge is made
slightly convex by adding another point in the middle of the free edge.

We choose to create such a deformed polygon because it works on the current front end application. Bear in mind
 that if you decide to use a different rendering library, a different result, such as turning the opened corner
 into a triangle polygon, might be preferable.

It's certainly unrealistic to expect any given library to display deformed faces.

Take the case of the piece of shit babylon js. It is supposed to be a 3D rendering library and it cannot fucking display
 a fucking concave polygon. Such is the state of open source software today.

However it is fair to say that deformed polygons are a good choice on itself even if they require further processing
(such as triangulation) before some graphical library can correctly render them.

Note that these deformations are made for single faces instead of bundles. It would be more correct (and much much
more difficult to implement in the most general case) to consider three dimensional adjustments for whole bundles
that may be made of more than one face folded together.

This is however not such a big issue. Consider that a simple figure, made of a handful of folds - think of an origami
base -, is in general more
"open" than a more complex figure, and it just looks bad if it doesn't look three dimensional. A more complex figure
that is rendered flatter than it it does not usually look too bad, since it's the shape of the figure that matters.
Still this is a problem that should be tackled.

====
IMPORTANT: Limitations of 3-D tweaking:

At the moment I'm opening triangles when they have exactly one free edge, and making that edge convex by adding exactly
one new vertex in the middle of the free edge.

Well, in order to be displayed correctly by babylon I have to ensure the new vertex is in the second position of the
list of vertices of both the front and back faces.

This is kind of brittle of course (I might break it inadvertently in the future) but I have no desire to spend time
making the solution more elegant. At least I'm documenting here what's going on.

Unfortunately the design of this module is not as generic as it could be due to the library babylon js being a huge
piece of shit (I can't say this enough).

And the choice of graphical library has definitely leaked downwards into this module. As stated before, at least I'm
documenting it.

This is a list of adaptations for babylon's sake:

- A face could theoretically have any number of "convexable" edges. Because it's hard to correctly display an arbitrary
deformed face in babylon, we just make a single convexable edge, even when there is more than one candidate.
The rendering by babylon is also sensitive to the order of the vertices, so that the non-planar vertex is always returned
in the second position (this is irrelevant to other rendering libraries unless they were also sensitive to the order
of vertices).

- Even in the flat rendering, for partially hidden faces, the faces are rendered whole. This is due to the fact
that a fucking graphical library (babylon) cannot fucking correctly display concave faces!!

This is a blessing in disguise, though, because that shortcoming can be bypassed by doing a "layered" rendering,
which is a compromise rendering that doesn't show a perfectly an ideal flat figure, but a slightly three dimensional
one on close inspection and it still allows precission when defining the folds. Remember the reason we cannot just
use three dimensional rendering everywhere is because it makes it harder to define folds.

I should end by thanking the gods that this fucking piece of shit has allowed me so far to find a barely decent way to
bypass its shortcomings and make something usable.

Look at the FaceTweaker class for more info.

Flat Rendering
==============
Here is the flat rendering algorithm in case I ever want to use it.

//   @JvmStatic fun flatRendering(figure: Figure): DisplayableFigure =
//           flatRendering(figure, false, 0.0)
//   @JvmStatic fun flatRendering(figure: Figure, pushLayersApart: Double): DisplayableFigure =
//           flatRendering(figure, true, pushLayersApart)
//	private fun flatRendering(figure: Figure, shouldPushLayers: Boolean, pushLayersApart: Double): DisplayableFigure {
//		val polygons: MutableMap<Color, MutableSet<OneSidedPolygon>> = mutableMapOf()
//		for(bundle in figure.bundles){
//         val comingFromTheTop = bundle.normal
//         val visibleBundle = VisibleBundle(bundle)
//         visibleBundle.visiblePolygonsFromTop.forEach {
//            val visiblePolygon =
//              if(shouldPushLayers)
//                 push(it, comingFromTheTop, pushLayersApart)
//               else it
//            polygons.putIfAbsent(it.color, mutableSetOf())
//            polygons[it.color]!!.add(visiblePolygon)
//         }
//         val comingFromTheBottom = bundle.normal.negate()
//         visibleBundle.visiblePolygonsFromBottom.forEach {
//            val visiblePolygon =
//                    if(shouldPushLayers)
//                       push(it, comingFromTheBottom, pushLayersApart)
//                    else it
//            polygons.putIfAbsent(it.color, mutableSetOf())
//            polygons[it.color]!!.add(visiblePolygon)
//         }
//      }
//      return makeDisplayableFigure(polygons)
//	}