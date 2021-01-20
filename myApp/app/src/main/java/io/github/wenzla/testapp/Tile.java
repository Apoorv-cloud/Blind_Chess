package io.github.wenzla.testapp;

import android.graphics.Paint;

/**
 * This is just a class to help create the tiles of the chessboard with the canvas.
 */

public class Tile {

    Paint paint;
    int left;
    int top;
    int right;
    int bottom;


    public Tile (int Color, int x, int y, int z, int a){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color);
        left = x;
        top = y;
        right = z;
        bottom = a;

    }
}
