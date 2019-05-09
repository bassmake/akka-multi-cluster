package sk.bsmk.iot

import akka.actor.typed.ActorRef
import sk.bsmk.iot.DeviceGroup.DeviceGroupMessage

object DeviceManager {

  trait DeviceManagerMessage

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
      extends DeviceManagerMessage
      with DeviceGroupMessage

  final case class DeviceRegistered(device: ActorRef[Device.DeviceMessage])

  final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList])
      extends DeviceManagerMessage
      with DeviceGroupMessage

  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

}
