package com.adaskin.android.watcher8.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.adaskin.android.watcher8.utilities.Constants;

import android.os.Parcel;
import android.os.Parcelable;

public class BuyBlock implements Parcelable {

    // -------- Fields --------
    // Raw fields
    public Date mBuyDate;
    public float mNumShares;
    public float mBuyCostBasis;
    public int mAccountColor;

    // Calculated fields
    private float mTotalDividend;
    public float mEffDivYield;
    public float mPctChangeSinceBuy;

    // -------- Constructor --------
    public BuyBlock(Date buyDate,
                    float numShares,
                    float buyPrice,
                    float divPS,
                    int accountColor) {
        mBuyDate = buyDate;
        mNumShares = numShares;
        mBuyCostBasis = buyPrice;
        mEffDivYield = divPS/mBuyCostBasis * 100.0f;
        mAccountColor = accountColor;
    }

    // -------- Methods --------
    // Public Methods
    public void computeChange(float currentPPS, float currentDiv){
        // Assuming that that the sell commission per shares is same as buy.
        mPctChangeSinceBuy = ((currentPPS - mBuyCostBasis)/mBuyCostBasis)*100.0f;
        mEffDivYield = currentDiv/mBuyCostBasis * 100f;
    }

    // -------- Implementation of Parcelable Interface --------
    // Constructor used when re-constructing object from a parcel
    private BuyBlock(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        String dateString = sdf.format(mBuyDate);
        dest.writeString(dateString);
        dest.writeFloat(mNumShares);
        dest.writeFloat(mBuyCostBasis);
        dest.writeFloat(mTotalDividend);
        dest.writeFloat(mEffDivYield);
        dest.writeFloat(mPctChangeSinceBuy);
        dest.writeInt(mAccountColor);
    }

    private void readFromParcel(Parcel in) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        String dateString = in.readString();
        try {
            mBuyDate= sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mNumShares = in.readFloat();
        mBuyCostBasis = in.readFloat();
        mTotalDividend = in.readFloat();
        mEffDivYield = in.readFloat();
        mPctChangeSinceBuy = in.readFloat();
        mAccountColor = in.readInt();
    }

    public static final Parcelable.Creator<BuyBlock> CREATOR = new Parcelable.Creator<BuyBlock>() {
        public BuyBlock createFromParcel(Parcel in) {
            return new BuyBlock(in);
        }

        public BuyBlock[] newArray(int size) {
            return new BuyBlock[size];
        }
    };

}
