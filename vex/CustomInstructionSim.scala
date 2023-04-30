package vexriscv.demo

import vexriscv.plugin._
import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import spinal.core._

object SimCustomInst extends App {
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(
        new SimdAddPlugin,
        new IBusSimplePlugin(
          resetVector = 0x80000000l,
          cmdForkOnSecondStage = false,
          cmdForkPersistence = false,
          prediction = NONE,
          catchAccessFault = false,
          compressedGen = false
        ),
        new DBusSimplePlugin(
          catchAddressMisaligned = false,
          catchAccessFault = false
        ),
        new CsrPlugin(CsrPluginConfig.smallest),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = false
        ),
        new LightShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = false,
          bypassMemory            = false,
          bypassWriteBack         = false,
          bypassWriteBackBuffer   = false,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = false
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
  )

  import spinal.core.sim._
  import java.io._
  SimConfig.withWave.doSim(cpu()){dut =>
    dut.clockDomain.forkStimulus(10)

    val file = new File("forvex.bin")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    in.close()

    val inst = bytes.grouped(4)
      .map{case x =>
        x.reverse
          .map(x => BigInt(x))
          .map(x => if(x>=0) x else x+256)
          .reduce((a,b) => (a<<8)+b)
      }
      .toList

    var iBus : IBusSimpleBus = null
    var dBus : DBusSimpleBus = null
    dut.plugins.foreach{
      case p : IBusSimplePlugin =>
        iBus = p.iBus
      case p : DBusSimplePlugin =>
        dBus = p.dBus
      case _ =>
    }

    iBus.cmd.ready #= true
    dBus.cmd.ready #= true
    iBus.rsp.valid #= false
    dBus.rsp.ready #= false
    
    for(idx <- 0 until 1000) {

      if(iBus.cmd.valid.toBoolean) {
        iBus.rsp.valid #= true
        val inst_addr = iBus.cmd.payload.pc.toBigInt - 0x80000000l + 0x00000264l//this 264 is the start line of main function in forvex.bin
        val i = inst(inst_addr.toInt/4)
        iBus.rsp.payload.inst #= (if(i>=0) i else i+(BigInt(1) << 32))
      }else {
        iBus.rsp.valid #= false
      }

      if(dBus.cmd.valid.toBoolean && !dBus.cmd.payload.wr.toBoolean) {
        dBus.rsp.ready #= true
        dBus.rsp.data #= idx << 4
      }else {
        dBus.rsp.ready #= false
      }

      dut.clockDomain.waitSampling()
    }
  }
}
