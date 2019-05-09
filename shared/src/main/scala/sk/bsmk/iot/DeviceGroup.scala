package sk.bsmk.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import sk.bsmk.iot.DeviceGroup.DeviceGroupMessage
import sk.bsmk.iot.DeviceManager.{DeviceRegistered, ReplyDeviceList, RequestDeviceList, RequestTrackDevice}

object DeviceGroup {

  def apply(groupId: String): Behavior[DeviceGroupMessage] =
    Behaviors.setup(context => new DeviceGroup(context, groupId))

  trait DeviceGroupMessage

  final case class DeviceTerminated(device: ActorRef[Device.DeviceMessage], groupId: String, deviceId: String)
      extends DeviceGroupMessage

}

class DeviceGroup(context: ActorContext[DeviceGroupMessage], groupId: String)
    extends AbstractBehavior[DeviceGroupMessage] {

  import DeviceGroup._

  private var deviceIdToActor = Map.empty[String, ActorRef[Device.DeviceMessage]]

  context.log.info("DeviceGroup {} started", groupId)

  override def onMessage(msg: DeviceGroupMessage): Behavior[DeviceGroupMessage] = msg match {
    case trackMsg @ RequestTrackDevice(`groupId`, deviceId, replyTo) =>
      deviceIdToActor.get(deviceId) match {
        case Some(deviceActor) =>
          replyTo ! DeviceRegistered(deviceActor)
        case None =>
          context.log.info("Creating device actor for {}", trackMsg.deviceId)
          val deviceActor = context.spawn(Device(groupId, deviceId), s"device-$deviceId")
          context.watchWith(deviceActor, DeviceTerminated(deviceActor, groupId, deviceId))
          deviceIdToActor += deviceId -> deviceActor
          replyTo ! DeviceRegistered(deviceActor)
      }
      this

    case RequestTrackDevice(gId, _, _) =>
      context.log.warning("Ignoring TrackDevice request for {}. This actor is responsible for {}.", gId, groupId)
      this

    case RequestDeviceList(requestId, gId, replyTo) =>
      if (gId == groupId) {
        replyTo ! ReplyDeviceList(requestId, deviceIdToActor.keySet)
        this
      } else
        Behaviors.unhandled

    case DeviceTerminated(_, _, deviceId) =>
      context.log.info("Device actor for {} has been terminated", deviceId)
      deviceIdToActor -= deviceId
      this

  }

  override def onSignal: PartialFunction[Signal, Behavior[DeviceGroupMessage]] = {
    case PostStop =>
      context.log.info("DeviceGroup {} stopped", groupId)
      this
  }

}
