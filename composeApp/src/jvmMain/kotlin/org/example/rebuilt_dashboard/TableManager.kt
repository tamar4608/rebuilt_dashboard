package org.example.rebuilt_dashboard

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.StructSubscriber

enum class ConnectionType {
    ROBOT,
    LOCAL
}

const val PUBLISHER_BASE_TOPIC = "/AdvantageKit/DriverDashboard"
val baseTable: NetworkTable = NetworkTableInstance.getDefault().getTable(PUBLISHER_BASE_TOPIC)

val robotPose: StructSubscriber<Pose2d> = NetworkTableInstance.getDefault().getTable("/AdvantageKit/RealOutputs/Odometry").getStructTopic("Robot",
        Pose2d.struct).subscribe(Pose2d())

fun startClient(connectionType: ConnectionType){
    println("Creating Client with connection type $connectionType")
    when (connectionType) {
        ConnectionType.ROBOT -> {
            NetworkTableInstance.getDefault().setServerTeam(5987)
        }
        ConnectionType.LOCAL -> {
            NetworkTableInstance.getDefault().setServer("localhost")
        }
    }
    NetworkTableInstance.getDefault().startClient4("RebuiltDashboard")
}