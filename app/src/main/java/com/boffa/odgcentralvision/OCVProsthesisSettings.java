package com.boffa.odgcentralvision;

/**
 * Created by Jack on 5/15/2018.
 */

public class OCVProsthesisSettings
{

    public int sizeDegrees;
    public int pixelSizeMicrons;
    public int grayLevels;
    public int fullBlack;
    public int fullBlack_actual;
    public int fullWhite;
    public int fullWhite_actual;
    public boolean changed;

    public OCVProsthesisSettings()
    {
        sizeDegrees = 10;
        pixelSizeMicrons = 30;
        grayLevels = 8;
        fullBlack = 0;
        fullBlack_actual = 0;
        fullWhite = 15;
        fullWhite_actual = 255;
        changed = false;
    }

    public void setFullBlack(int black)
    {
        fullBlack = black;
        fullBlack_actual = black * 17;
    }

    public void setFullWhite(int white)
    {
        fullWhite = white;
        fullWhite_actual = white * 17;
    }
}
