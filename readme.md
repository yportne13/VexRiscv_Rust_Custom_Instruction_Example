# VexRiscv_Rust_Custom_Instruction_Example

It's super easy to create a risc-v cpu with your custom instruction by this amaziong project: [`VexRiscv`](https://github.com/SpinalHDL/VexRiscv). VexRiscv has give a small [`demo`](https://github.com/SpinalHDL/VexRiscv/blob/master/src/main/scala/vexriscv/demo/CustomInstruction.scala) on how to create your own instruction. But a lot of people don't know how to start a simulation. So I write this small demo to show a easy way to sim on this simd_add inst. I assume that you have a basic knowledge about SpinalHDL(you can write a small SpinalHDL project and sim on it).

## usage

if you don't want to install rust, you only need to do step 1 and step 4

1. clone [`VexRiscv`](https://github.com/SpinalHDL/VexRiscv), copy [`vex/CustomInstructionSim.scala`](vex/CustomInstructionSim.scala) to `src/main/scala/vexriscv/demo`.

2. [install rust](https://www.rust-lang.org/tools/install), install target rv32i:

```
rustup target add riscv32i-unknown-none-elf
```

3. in this project:

```
cargo objcopy --release -- -O binary forvex.bin
```

and you will get `forvex.bin`. If you want to see the asm code, you can run:

```
cargo objdump --bin riscv --release -- -d
```

there will be a lot of things, but you only need to care about main function

4. copy `forvex.bin` to the vexriscv project, run the sim(that you copied in step 1), and you will get the `.vcd` file.

## what this demo does

reads two int from 0x4001_0600 and 0x4001_0700. use simd_add then write back to 0x4001_0800, and use a normal add then write back to 0x4001_0900. When 0x4001_0600 is 0x0000_00A0 and 0x4001_0700 is 0x0000_00B0, normal add will get 0x0000_0150, and simd_add will get 0x0000_0050
