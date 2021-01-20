package io.github.wenzla.testapp;

import android.graphics.Paint;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains data about a piece.
 */
public class Piece
{
    private String type;
    private Location location;
    private Paint paint;
    public int color;

    public Piece(String type, Location location, int color)
    {
        this.type = type;
        this.location = location;
        this.color = color;
        paint = new Paint(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(32f);
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location newLocation)
    {
        location = newLocation;
    }

    public Paint getPaint(){
        return paint;
    }

    // The image representing this piece
    public char getSymbol()
    {
        switch (type)
        {
            case "WHITE_KING":
                return (char)0x2654;
            case "WHITE_QUEEN":
                return (char)0x2655;
            case "WHITE_BISHOP":
                return (char)0x2657;
            case "WHITE_KNIGHT":
                return (char)0x2658;
            case "WHITE_ROOK":
                return (char)0x2656;
            case "WHITE_PAWN":
                return (char)0x2659;
            case "BLACK_KING":
                return (char)0x265A;
            case "BLACK_QUEEN":
                return (char)0x265B;
            case "BLACK_BISHOP":
                return (char)0x265D;
            case "BLACK_KNIGHT":
                return (char)0x265E;
            case "BLACK_ROOK":
                return (char)0x265C;
            case "BLACK_PAWN":
                return (char)0x265F;
        }

        return 0;
    }

    // Type of piece
    public String getType()
    {
        return type;

    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("type", getType());
            jsonObject.put("location", getLocation().toJSON());
            jsonObject.put("color", color);

            return jsonObject;
        } catch (JSONException e) {

            return null;
        }

    }

}
