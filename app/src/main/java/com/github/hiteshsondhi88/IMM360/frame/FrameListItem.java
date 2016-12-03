package com.github.hiteshsondhi88.IMM360.frame;

import android.graphics.drawable.Drawable;

/**
 * 데이터를 담고 있을 아이템 정의
 *
 * @author Mike
 *
 */
public class FrameListItem {

	/**
	 * Icon
	 */
	private Drawable mIcon;

	/**
	 * Data array
	 */
	private int[] mData;

	/**
	 * True if this item is selectable
	 */
	private boolean mSelectable = true;

	/**
	 * Initialize with icon and data array
	 *
	 * @param icon
	 * @param obj
	 */
//   public FrameListItem(Drawable icon, String[] obj) {
//      mIcon = icon;
//      mData = obj;
//   }

	/**
	 * Initialize with icon and strings
	 *
	 * @param icon
	 * @param obj01
	 * @param obj02
	 */


	public FrameListItem(Drawable icon, int obj01, int obj02) {
		mIcon = icon;

		mData = new int[2];
		mData[0] = obj01;
		mData[1] = obj02;
	}

	/**
	 * True if this item is selectable
	 */
	public boolean isSelectable() {
		return mSelectable;
	}

	/**
	 * Set selectable flag
	 */
	public void setSelectable(boolean selectable) {
		mSelectable = selectable;
	}

	/**
	 * Get data array
	 *
	 * @return
	 */
	public int[] getData() {
		return mData;
	}

	/**
	 * Get data
	 */
	public int getData(int index) {
		if (mData == null || index >= mData.length) {
			return 0;
		}

		return mData[index];
	}

	/**
	 * Set data array
	 *
	 * @param obj
	 */
//   public void setData(String[] obj) {
//      mData = obj;
//   }
	public void setIndex(int index)
	{
		mData[0] = index;
	}


	/**
	 * Set icon
	 *
	 * @param icon
	 */
	public void setIcon(Drawable icon) {
		mIcon = icon;
	}

	/**
	 * Get icon
	 *
	 * @return
	 */
	public Drawable getIcon() {
		return mIcon;
	}

	/**
	 * Compare with the input object
	 *
	 * @param other
	 * @return
	 */
//   public int compareTo(FrameListItem other) {
//      if (mData != null) {
//         String[] otherData = other.getData();
//         if (mData.length == otherData.length) {
//            for (int i = 0; i < mData.length; i++) {
//               if (!mData[i].equals(otherData[i])) {
//                  return -1;
//               }
//            }
//         } else {
//            return -1;
//         }
//      } else {
//         throw new IllegalArgumentException();
//      }
//
//      return 0;
//   }

}