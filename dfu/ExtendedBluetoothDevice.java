/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA. Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. This heading must NOT be removed from the file.
 ******************************************************************************/
package com.desay.iwan2.module.dfu;

import android.bluetooth.BluetoothDevice;

public class ExtendedBluetoothDevice
{
	public BluetoothDevice device;
	public int rssi;
	public boolean isBonded;

	public ExtendedBluetoothDevice(BluetoothDevice device, int rssi,boolean isBonded)
	{
		this.device = device;
		this.rssi = rssi;
		this.isBonded = isBonded;
	}

	/**
	 *判断两个ExtendedBluetoothDevice的类对象的成员device的getAddress()的内容是否相同。
	 *两个对象互相对比，对象2.equals(对象1)时会调用该equals方法
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ExtendedBluetoothDevice)
		{
			final ExtendedBluetoothDevice that = (ExtendedBluetoothDevice) o;
			return device.getAddress().equals(that.device.getAddress());
		}
		return super.equals(o);
	}

	/**
	 * Class used as a temporary comparator to find the device in the List of
	 * {@link ExtendedBluetoothDevice}s. This must be done this way, because
	 * List#indexOf and List#contains use the parameter's equals method, not the
	 * object's from list. See
	 * {@link DeviceListAdapter#updateRssiOfBondedDevice(String, int)} for
	 * example
	 */
	public static class AddressComparator
	{
		public String address;

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof ExtendedBluetoothDevice)
			{
				final ExtendedBluetoothDevice that = (ExtendedBluetoothDevice) o;
				return address.equals(that.device.getAddress());
			}
			return super.equals(o);
		}
	}
}
