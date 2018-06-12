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
        do {
            mSelection = (mSelection + 1) % mControls.size();
        } while (!mControls.get(mSelection).isControlEnabled());

        updateSelect();
    }

    public void selectPrevious()
    {
        do {
            mSelection--;
            if (mSelection < 0) mSelection = mControls.size() - 1;
        } while (!mControls.get(mSelection).isControlEnabled());

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
                value = Math.max(5, Math.min(value, 15));
                mSettings.sizeDegrees = value;
                mControls.get(index).setControlText("" + value + "°");
                break;
            case 1:
                value = Math.max(20, Math.min(value, 100));
                mSettings.pixelSizeMicrons = value;
                mControls.get(index).setControlText("" + value);
                break;
            case 2:
                setValueForGrayLevels(value);
                break;
            case 3:
                value = Math.max(0, Math.min(value, 15));
                mSettings.setFullBlack(value);
                mControls.get(index).setControlText("" + value);
                break;
            case 4:
                value = Math.max(0, Math.min(value, 15));
                mSettings.setFullWhite(value);
                mControls.get(index).setControlText("" + value);
                break;
        }

        mSettings.changed = true;
    }

    private void setValueForGrayLevels(int value)
    {
        value = Math.max(2, Math.min(value, 12));
        mSettings.grayLevels = value;

        if (value == 12)
        {
            mControls.get(2).setControlText("∞");
            mControls.get(3).setControlEnabled(false);
            mControls.get(4).setControlEnabled(false);
        }
        else
        {
            mControls.get(2).setControlText("" + value);
            mControls.get(3).setControlEnabled(true);
            mControls.get(4).setControlEnabled(true);
        }
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

    public void incrementSelected(int multiplier)
    {
        int value = getValueForControl(mSelection);

        switch (mSelection)
        {
            case 0:
                setValueForControl(mSelection, value + 1 * multiplier);
                break;
            case 1:
                setValueForControl(mSelection, value + 10 * multiplier);
                break;
            case 2:
                setValueForControl(mSelection, value + 2 * multiplier);
                break;
            case 3:
            case 4:
                setValueForControl(mSelection, value + 1 * multiplier);
                break;
        }
    }
}
