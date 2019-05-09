package sk.bsmk.iot

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.WordSpecLike
import sk.bsmk.iot.Device.DeviceMessage
import sk.bsmk.iot.DeviceGroup.{
  DeviceNotAvailable,
  DeviceTimedOut,
  RespondAllTemperatures,
  Temperature,
  TemperatureNotAvailable
}
import sk.bsmk.iot.DeviceGroupQuery.WrappedRespondTemperature

import scala.concurrent.duration._

class DeviceGroupQuerySpec extends ScalaTestWithActorTestKit with WordSpecLike {

  "DeviceGroupQuery actor" must {

    "return temperature value for working devices" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[DeviceMessage]()
      val device2 = createTestProbe[DeviceMessage]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))
        )
      )
    }

    "return TemperatureNotAvailable for devices with no readings" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[DeviceMessage]()
      val device2 = createTestProbe[DeviceMessage]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", None))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> TemperatureNotAvailable, "device2" -> Temperature(2.0))
        )
      )
    }

    "return DeviceNotAvailable if device stops before answering" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[DeviceMessage]()
      val device2 = createTestProbe[DeviceMessage]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(2.0)))

      device2.stop()

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(2.0), "device2" -> DeviceNotAvailable)
        )
      )
    }

    "return temperature reading even if device stops after answering" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[DeviceMessage]()
      val device2 = createTestProbe[DeviceMessage]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

      device2.stop()

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))
        )
      )
    }

    "return DeviceTimedOut if device does not answer in time" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[DeviceMessage]()
      val device2 = createTestProbe[DeviceMessage]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 200.millis))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))

      // no reply from device2

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> DeviceTimedOut)
        )
      )
    }

  }

}
