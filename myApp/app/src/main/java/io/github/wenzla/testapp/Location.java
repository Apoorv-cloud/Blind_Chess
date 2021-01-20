package io.github.wenzla.testapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.IllegalStateException;

/**
 * Represents locations and performs location-based functions.
 */
public class Location
{
    private int rank;
    private int file;

    // rank: x-coordinate, file: y-coordinate
    public Location(int rank, int file)
    {
        if (rank > 7 || rank < 0 || file > 7 || file < 0)
        {
            throw new IllegalStateException("The location is invalid!");
        }
        this.rank = rank;
        this.file = file;
    }

    public int rank()
    {
        return rank;
    }

    public int file()
    {
        return file;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Location))
        {
            return false;
        }

        Location otherLocation = (Location)obj;

        return ((rank == otherLocation.rank()) && (file == otherLocation.file()));
    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("rank", rank());
            jsonObject.put("file", file());

            return jsonObject;
        } catch (JSONException e) {
            return null;
        }

    }

}
