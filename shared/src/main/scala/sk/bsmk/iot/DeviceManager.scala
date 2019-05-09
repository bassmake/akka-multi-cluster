package sk.bsmk.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import sk.bsmk.iot.DeviceGroup.DeviceGroupMessage
import sk.bsmk.iot.DeviceManager.{
  DeviceGroupTerminated,
  DeviceManagerMessage,
  ReplyDeviceList,
  RequestDeviceList,
  RequestTrackDevice
}

object DeviceManager {

  def apply(): Behavior[DeviceManagerMessage] = Behaviors.setup(context => new DeviceManager(context))

  trait DeviceManagerMessage

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
      extends DeviceManagerMessage
      with DeviceGroupMessage

  final case class DeviceRegistered(device: ActorRef[Device.DeviceMessage])

  final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList])
      extends DeviceManagerMessage
      with DeviceGroupMessage

  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  private final case class DeviceGroupTerminated(groupId: String) extends DeviceManagerMessage

}

class DeviceManager(context: ActorContext[DeviceManager.DeviceManagerMessage])
    extends AbstractBehavior[DeviceManager.DeviceManagerMessage] {

  var groupIdToActor = Map.empty[String, ActorRef[DeviceGroupMessage]]

  context.log.info("DeviceManager started")

  override def onMessage(msg: DeviceManager.DeviceManagerMessage): Behavior[DeviceManager.DeviceManagerMessage] =
    msg match {
      case trackMsg @ RequestTrackDevice(groupId, _, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(ref) =>
            ref ! trackMsg
          case None =>
            context.log.info("Creating device group actor for {}", groupId)
            val groupActor = context.spawn(DeviceGroup(groupId), "group-" + groupId)
            context.watchWith(groupActor, DeviceGroupTerminated(groupId))
            groupActor ! trackMsg
            groupIdToActor += groupId -> groupActor
        }
        this

      case req @ RequestDeviceList(requestId, groupId, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(ref) =>
            ref ! req
          case None =>
            replyTo ! ReplyDeviceList(requestId, Set.empty)
        }
        this

      case DeviceGroupTerminated(groupId) =>
        context.log.info("Device group actor for {} has been terminated", groupId)
        groupIdToActor -= groupId
        this
    }

  override def onSignal: PartialFunction[Signal, Behavior[DeviceManagerMessage]] = {
    case PostStop =>
      context.log.info("DeviceManager stopped")
      this
  }

}
