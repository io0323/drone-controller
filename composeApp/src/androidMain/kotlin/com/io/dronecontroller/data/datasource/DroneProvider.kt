package com.io.dronecontroller.data.datasource

import io.mavsdk.System as MavsdkSystem

// MavlinkDataSource と MissionDataSource が同一の MavsdkSystem インスタンスを共有するためのホルダー
class DroneProvider {
    var drone: MavsdkSystem? = null
}
