package com.boffa.odgcentralvision;

import java.util.ArrayList;
import com.osterhoutgroup.api.ext.DmpSensorExt;
/**
 * Created by Jack on 5/23/2018.
 */

public class SideControlBank
{
    private final ArrayList<SideControlView> mControls;
    private int mSelection;
    private OCVProsthesisSettings mSettings;

    public SideControlBank()
    {
        mControls = new ArrayList<>();
        mSelection = -1;
    }

    public void addControl(SideControlView v)
    {
        mControls.add(v);
    }

    public void start(OCVProsthesisSettings settings)
    {
        mSettings = settings;
        select(0);
    }

    public void select(int i)
    {
        mSelection = i;
        updateSelect();
    }

    public void selectNext()
    {
        mSelection = (mSelection + 1) % mControls.size();
        updateSelect();
    }

    public void selectPrevious()
    {
        mSelection--;
        if (mSelection < 0) mSelection = mControls.size() - 1;
        updateSelect();
    }

    private void updateSelect()
    {
        for (int i = 0; i < mControls.size(); i++)
        {
            mControls.get(i).setControlSelected(i == mSelection);
        }
    }

    public void setValueForControl(int index, int value)
    {
        switch (index)
        {
            case 0:
                value = Math.max(0, Math.min(value, 15));
                mSettings.sizeDegrees = value;
                break;
            case 1:
                value = Math.max(10, Math.min(value, 100));
                mSettings.pixelSizeMicrons = value;
                break;
            case 2:
                value = Math.max(1, Math.min(value, 20));
                mSettings.grayLevels = value;
                break;
            case 3:
                value = Math.max(0, Math.min(value, 100));
                mSettings.fullBlack = value;
                break;
            case 4:
                value = Math.max(0, Math.min(value, 100));
                mSettings.fullWhite = value;
                break;
        }

        mControls.get(index).setControlText("" + value);
    }

    public int getValueForControl(int index)
    {
        switch (index)
        {
            case 0:
                return mSettings.sizeDegrees;
            case 1:
                return mSettings.pixelSizeMicrons;
            case 2:
                return mSettings.grayLevels;
            case 3:
                return mSettings.fullBlack;
            case 4:
                return mSettings.fullWhite;
        }

        return 0;
    }

    public void incrementSelected()
    {
        int value = getValueForControl(mSelection);
        setValueForControl(mSelection, value + 1);
    }

    public void decrementSelected()
    {
        int value = getValueForControl(mSelection);
        setValueForControl(mSelection, value - 1);
    }
}
