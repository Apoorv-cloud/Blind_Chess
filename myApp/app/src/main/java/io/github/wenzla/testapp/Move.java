package io.github.wenzla.testapp;

// Represents a move as two Locations and the piece that is moved

public class Move
{

    private Location from;
    private Location to;
    private Piece    piece;
    private Piece    removed;


    public Move(Piece piece, Location from, Location to)
    {
        this.piece = piece;
        this.from = from;
        this.to = to;
    }

    public Move(Piece piece, Location from, Location to, Piece removed)
    {
        this(piece, from, to);

        this.removed = removed;

    }

    // Previous location
    public Location from()
    {
        return from;
    }


    // Final location
    public Location to()
    {
        return to;
    }


    // Moved piece
    public Piece piece()
    {
        return piece;
    }


    // Removed piece
    public Piece getRemovedPiece()
    {
        return removed;
    }
}
