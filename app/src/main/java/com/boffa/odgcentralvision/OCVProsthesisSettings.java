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
    public int fullWhite;

    public OCVProsthesisSettings()
    {
        sizeDegrees = 10;
        pixelSizeMicrons = 30;
        grayLevels = 8;
        fullBlack = 0;
        fullWhite = 255;
    }

}
