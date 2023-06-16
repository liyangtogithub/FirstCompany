package com.desay.iwan2.common.server.ble.handler;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.server.ble.OrderQueue;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.Instruction;
import dolphin.tools.ble.InstructionType;

import java.util.UUID;

/**
 * Created by 方奕峰 on 14-7-25.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotifyServer {

    public final static UUID descriptorUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public void turnOnVersionNotify(Context context, BluetoothGatt gatt, boolean enableNotify) {
        BluetoothGattService gattService1 = gatt.getService(BleApi1.VersionApi.UUID_SERVER);
        if (gattService1 == null) return;
        BluetoothGattCharacteristic characteristic1 = gattService1
                .getCharacteristic(BleApi1.VersionApi.UUID_RESPONSE);

        Instruction instruction1 = new Instruction();
        instruction1.type = InstructionType.characteristicNotify;
        instruction1.notificationToggle = enableNotify;
        instruction1.characteristic = characteristic1;
        BleServer.getInstance(context).setCharacteristicNotification(instruction1);

        if (enableNotify) {
            OrderQueue.Cmd cmd = new OrderQueue.Cmd();
            cmd.id = "notify1";
            cmd.instruction = new Instruction();
            cmd.timeout = 10000;
            cmd.retryCount = 50;
            cmd.priority = Integer.MAX_VALUE;
            cmd.instruction.type = InstructionType.descriptorWrite;
            cmd.instruction.descriptor = characteristic1.getDescriptor(descriptorUuid);
            cmd.instruction.descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            OrderQueue.send(context, cmd);
        }
    }

    public void turnOnBizNotify(Context context, BluetoothGatt gatt, boolean enableNotify) {
        BluetoothGattService gattService2 = gatt.getService(BleApi1.BizApi.UUID_SERVER);
        if (gattService2 == null) return;

        BluetoothGattCharacteristic characteristic2 = gattService2
                .getCharacteristic(BleApi1.BizApi.UUID_RESPONSE);

        Instruction instruction2 = new Instruction();
        instruction2.type = InstructionType.characteristicNotify;
        instruction2.notificationToggle = enableNotify;
        instruction2.characteristic = characteristic2;
        BleServer.getInstance(context).setCharacteristicNotification(instruction2);

        if (enableNotify) {
            OrderQueue.Cmd cmd = new OrderQueue.Cmd();
            cmd.id = "notify2";
            cmd.instruction = new Instruction();
            cmd.timeout = 10000;
            cmd.retryCount = 50;
            cmd.priority = Integer.MAX_VALUE;
            cmd.instruction.type = InstructionType.descriptorWrite;
            cmd.instruction.descriptor = characteristic2.getDescriptor(descriptorUuid);
            cmd.instruction.descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            OrderQueue.send(context, cmd);
        }
    }
}
