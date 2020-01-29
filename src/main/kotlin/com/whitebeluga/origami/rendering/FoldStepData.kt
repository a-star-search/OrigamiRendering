/*
 *    This file is part of "Origami".
 *
 *     Origami is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Origami is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Origami.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.whitebeluga.origami.rendering

import com.whitebeluga.origami.figure.Figure
import com.whitebeluga.origami.figure.folding.linefolding.LineFoldParameters
import com.whitebeluga.origami.rendering.Frames.amapLineFoldFrames
import com.whitebeluga.origami.rendering.Frames.ffLineFoldFrames
import com.whitebeluga.origami.rendering.Frames.translationFrames

class FoldStepData(
        val initialFigure: Figure,
        val foldedFigure: Figure,
        val foldFrames: List<Figure>,
        val translationFrames: List<Figure>) {

   companion object {
      @JvmStatic
      fun amapLineFold(figure: Figure, foldParameters: LineFoldParameters): FoldStepData {
         val afterFigure = figure.doAMAPLineFold(foldParameters)
         return FoldStepData(
                 figure,
                 afterFigure,
                 amapLineFoldFrames(figure, foldParameters),
                 translationFrames(afterFigure))
      }
      @JvmStatic
      fun ffLineFold(figure: Figure, foldParameters: LineFoldParameters): FoldStepData {
         val afterFigure = figure.doFirstFlapLineFold(foldParameters)
         return FoldStepData(
                 figure,
                 afterFigure,
                 ffLineFoldFrames(figure, foldParameters),
                 translationFrames(afterFigure))
      }
   }
}