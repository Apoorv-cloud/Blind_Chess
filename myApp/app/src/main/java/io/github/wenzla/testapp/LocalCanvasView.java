package io.github.wenzla.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

// This is just the view to display the chessboard
public class LocalCanvasView extends View {
    Bitmap mBitmap;
    private Canvas mCanvas;
    private Piece target;
    private Path mPath;
    Context context;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    private int tileSize;
    Location to;
    private int[] from = new int[2];
    private Board b = new Board();
    private Tile[][] tiles = new Tile[8][8];
    boolean willMove = false;
    String statusString;
    TextView tv = (TextView) findViewById(R.id.status);
    char[] chessNotationX = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    char[] chessNotationY = {'8', '7', '6', '5', '4', '3', '2', '1'};
    private int myColor = Color.WHITE;

    LocalCanvasViewListener localCanvasViewListener;

    // I only did this since you apparently can't set text to a text view from a view class
    public LocalCanvasViewListener getCanvasViewListener() {
        return localCanvasViewListener;
    }

    public void setCustomViewListener(LocalCanvasViewListener canvasViewListener) {
        this.localCanvasViewListener = canvasViewListener;
    }

    public interface LocalCanvasViewListener {
        void onUpdateValue(String StatusString);
    }


    public LocalCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        tileSize = metrics.widthPixels / 10;

        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                int color;
                if ((i+j) %2 == 0){
                    color = Color.LTGRAY;
                } else {
                    color = Color.GRAY;
                }
                tiles[i][j] = new Tile(color, tileSize*(i+1),tileSize*(j+1),(tileSize * 2)+(tileSize*(i)),(tileSize*2)+(tileSize*(j)));
            }
        }

        clearCanvas();
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                canvas.drawRect(tiles[i][j].left,tiles[i][j].top,tiles[i][j].right,tiles[i][j].bottom,tiles[i][j].paint);
            }
        }

        for (Piece piece : b.getPieces())
        {
            String x = piece.getSymbol() + "";
            if(b.getTurns()%2 == 0){
                if(piece.color == Color.WHITE){
                    canvas.drawText(x, ((piece.getLocation().rank() + 1) *tileSize) + (tileSize/2), ((piece.getLocation().file() + 1)) *tileSize + (tileSize/2), piece.getPaint());
                }
            } else {
                if(piece.color == Color.BLACK){
                    canvas.drawText(x, ((piece.getLocation().rank() + 1) *tileSize) + (tileSize/2), ((piece.getLocation().file() + 1)) *tileSize + (tileSize/2), piece.getPaint());
                }
            }
        }
        canvas.drawPath(mPath, mPaint);
        // Changes status text
        if (getCanvasViewListener() != null) {
            getCanvasViewListener().onUpdateValue(statusString);
        }

    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        statusString = "";
        this.from = findLocation(x,y);
        if (!(this.from[0] < 0 || this.from[1] < 0)){
            willMove = true;
        }
        //statusString = "From: " + this.from[0] + ", " + this.from[1];
    }


    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // The clear button
    public void clearCanvas() {
        b = new Board();
        upTouch();
        //statusString = "White's Move";
    }

    // when ACTION_UP stop touch
    private void upTouch() {
        mPath.lineTo(mX, mY);
        mPath.reset();
        //invalidate();
        int[] locations = findLocation(mX,mY);
        boolean showTurn = true;
        // This will decide if the piece is allowed to move (non chess piece rules such as
        // is the user telling me to move off the board, is black moving during white turn, etc.)
        if (willMove && !(locations[0] < 0 || locations[1] < 0)){
            for (Piece piece : b.getPieces()){
                    if (piece.getLocation().equals(new Location(from[0], from[1])))
                    {
                        target = piece;
                        break;
                    }
            }
            // detects which piece is allowed to move
            if (b.getTurns() % 2 == 1){
                if (target.color != Color.BLACK){
                    willMove = false;
                }
            } else {
                if (target.color != Color.WHITE){
                    willMove = false;
                }
            }
            // if the piece can move, detects if it is allowed to move to the certain spot.
            if (willMove){
                to = new Location(locations[0], locations[1]);
                b.moveAndSend(target, new Location(from[0], from[1]), to);
                if (b.moves.size() > 0){
                    Piece removed = b.moves.peek().getRemovedPiece();
                    if (removed != null ){
                        if (removed.color != Color.GRAY){
                            // TODO: its not working
                            String s =  target.getType();
                            if (removed.getType().equals("WHITE_KING") || removed.getType().equals("BLACK_KING")){
                                statusString = removed.getType() + " was removed from game. The game is over!";
                                clearCanvas();
                            } else {
                                statusString = removed.getType() + " was removed by " + s + " at " + chessNotationX[to.rank()] + chessNotationY[to.file()] + '\n' + getTurnColor(b.getTurns());
                                Move filler = new Move(new Piece("WHITE_PAWN", new Location(7, 6), Color.GRAY), new Location(0,0), new Location(0,0));
                                b.moves.push(filler);
                            }
                            showTurn = false;
                        }
                    }
                }
                from = new int[2];
                if(showTurn){
                    // statusString = getTurnColor(b.getTurns());
                }

            }
        }
        willMove = false;

    }

    // Gets turn color and returns the string displaying what turn it is.
    private String getTurnColor(int turns) {
        if (turns % 2 == 1)
        {
            return " Black's Move";
        }
        else
        {
            return " White's Move";
        }
    }


    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }

    // Gets the location on screen where the user pressed down.
    private int[] findLocation(float x, float y) {

        int i1 = -1;
        int i2 = -1;

        if(x >tileSize && x < 2*tileSize){
            i1 = 0;
        } else if(x > 2*tileSize && x < 3*tileSize){
            i1 = 1;
        } else if(x > 3*tileSize && x < 4*tileSize){
            i1 = 2;
        } else if(x > 4*tileSize && x < 5*tileSize){
            i1 = 3;
        } else if(x > 5*tileSize && x < 6*tileSize){
            i1 = 4;
        } else if(x > 6*tileSize && x < 7*tileSize){
            i1 = 5;
        } else if(x > 7*tileSize && x <  8*tileSize){
            i1 = 6;
        } else if(x > 8*tileSize && x < 9*tileSize){
            i1 = 7;
        }

        if(y >tileSize && y < 2*tileSize){
            i2 = 0;
        } else if(y > 2*tileSize && y < 3*tileSize){
            i2 = 1;
        } else if(y > 3*tileSize && y < 4*tileSize){
            i2 = 2;
        } else if(y > 4*tileSize && y < 5*tileSize){
            i2 = 3;
        } else if(y > 5*tileSize && y < 6*tileSize){
            i2 = 4;
        } else if(y > 6*tileSize && y < 7*tileSize){
            i2 = 5;
        } else if(y > 7*tileSize && y < 8*tileSize){
            i2 = 6;
        } else if(y >  8*tileSize && y < 9*tileSize){
            i2 = 7;
        }

        return new int[]{i1,i2};
    }

    public Board getBoard() {
        return b;
    }

    public void setMyColor(int c) {
        myColor = c;
    }

}
