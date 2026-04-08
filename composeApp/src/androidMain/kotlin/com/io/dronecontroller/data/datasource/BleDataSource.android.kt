package com.io.dronecontroller.data.datasource

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import com.io.dronecontroller.domain.model.BleConnectionStatus
import com.io.dronecontroller.domain.model.BleControllerState
import com.io.dronecontroller.domain.model.BleDevice
import com.io.dronecontroller.domain.model.RunStatus
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@SuppressLint("MissingPermission")
class BleDataSource(private val context: Context) : BleDataSourceContract {

    private val bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var gatt: BluetoothGatt? = null
    private val _connectionStatus = MutableStateFlow<BleConnectionStatus>(BleConnectionStatus.Disconnected)
    private val _controllerState = MutableStateFlow(BleControllerState())

    companion object {
        private val HID_SERVICE = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb")
        private val HID_REPORT = UUID.fromString("00002a4d-0000-1000-8000-00805f9b34fb")
        private val CCC_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    override fun scanDevices(): Flow<List<BleDevice>> = callbackFlow {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val found = mutableListOf<BleDevice>()
        _connectionStatus.value = BleConnectionStatus.Scanning

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device.name?.takeIf { it.isNotBlank() }
                    ?: "Unknown (${result.device.address.takeLast(5)})"
                val device = BleDevice(name = name, address = result.device.address)
                if (found.none { it.address == device.address }) {
                    found.add(device)
                    trySend(found.toList())
                }
            }
        }

        scanner.startScan(scanCallback)

        awaitClose {
            scanner.stopScan(scanCallback)
            if (_connectionStatus.value is BleConnectionStatus.Scanning) {
                _connectionStatus.value = BleConnectionStatus.Disconnected
            }
        }
    }

    override suspend fun connect(address: String): RunStatus<Unit> {
        val remoteDevice = runCatching { bluetoothAdapter?.getRemoteDevice(address) }.getOrNull()
            ?: return RunStatus.Error("デバイスが見つかりません: $address")

        val bleDevice = BleDevice(
            name = remoteDevice.name?.takeIf { it.isNotBlank() } ?: address,
            address = address
        )
        _connectionStatus.value = BleConnectionStatus.Connecting(bleDevice)

        return suspendCancellableCoroutine { cont ->
            val callback = buildGattCallback(bleDevice) { result ->
                if (cont.isActive) cont.resume(result)
            }

            val newGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                remoteDevice.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            } else {
                remoteDevice.connectGatt(context, false, callback)
            }

            cont.invokeOnCancellation {
                newGatt?.disconnect()
                newGatt?.close()
                gatt = null
                _connectionStatus.value = BleConnectionStatus.Disconnected
            }
        }
    }

    private fun buildGattCallback(
        device: BleDevice,
        onConnectResult: (RunStatus<Unit>) -> Unit
    ) = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    this@BleDataSource.gatt = gatt
                    _connectionStatus.value = BleConnectionStatus.Connected(device)
                    gatt.discoverServices()
                    onConnectResult(RunStatus.Success(Unit))
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionStatus.value = BleConnectionStatus.Disconnected
                    _controllerState.value = BleControllerState()
                    this@BleDataSource.gatt = null
                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableHidNotifications(gatt)
            }
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            parseHidReport(characteristic.value ?: return)
        }
    }

    private fun enableHidNotifications(gatt: BluetoothGatt) {
        val hidService = gatt.getService(HID_SERVICE) ?: return
        hidService.characteristics
            .filter { it.uuid == HID_REPORT }
            .forEach { characteristic ->
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CCC_DESCRIPTOR) ?: return@forEach
                scope.launch {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }
    }

    /**
     * BLE HID 標準ゲームパッドレポートをパース
     * フォーマット: [leftX, leftY, rightX, rightY, ...] (各 0-255, 中心=127)
     */
    private fun parseHidReport(data: ByteArray) {
        if (data.size < 4) return
        fun byteToAxis(b: Byte) = ((b.toInt() and 0xFF) - 127f) / 127f
        _controllerState.value = BleControllerState(
            leftX = byteToAxis(data[0]).coerceIn(-1f, 1f),
            leftY = byteToAxis(data[1]).coerceIn(-1f, 1f),
            rightX = byteToAxis(data[2]).coerceIn(-1f, 1f),
            rightY = byteToAxis(data[3]).coerceIn(-1f, 1f)
        )
    }

    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionStatus.value = BleConnectionStatus.Disconnected
        _controllerState.value = BleControllerState()
    }

    override fun observeConnectionStatus(): Flow<BleConnectionStatus> = _connectionStatus.asStateFlow()
    override fun observeControllerState(): Flow<BleControllerState> = _controllerState.asStateFlow()
}
